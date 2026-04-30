package service.cloud.request.clientRequest.notification.service;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.notification.adapter.EmailProvider;
import service.cloud.request.clientRequest.notification.config.NotificationProperties;
import service.cloud.request.clientRequest.notification.dto.EmailMessage;
import service.cloud.request.clientRequest.notification.model.*;
import service.cloud.request.clientRequest.notification.repo.EmailDeliveryRepository;
import service.cloud.request.clientRequest.notification.repo.EmailJobRepository;

import java.time.LocalDateTime;
import java.util.Map;

@ApplicationScoped
public class NotificationManager {

    private static final Logger log = LoggerFactory.getLogger(NotificationManager.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final int[] RETRY_MINUTES = {5, 30, 120};
    private static final int BATCH_SIZE = 20;
    private static final int WORKER_PARALLELISM = 5;
    private static final int STUCK_JOB_MINUTES = 10;

    @Inject
    private EmailJobRepository jobRepo;

    @Inject
    private EmailDeliveryRepository deliveryRepo;

    @Inject
    private EmailValidatorService validator;

    @Inject
    private EmailSuppressionService suppressionService;

    @Inject
    private EmailTemplateService templateService;

    @Inject
    private NotificationProperties notificationProperties;

    @Inject
    private ClientProperties clientProperties;

    @Inject
    @Named("sesProvider")
    private EmailProvider emailProvider;

    public void enqueue(TransacctionDTO tc, TransaccionRespuesta tr) {
        try {
            Client client = clientProperties.listaClientesOf(tc.getDocIdentidad_Nro());

            if (client.getEmail() == null || !client.getEmail().isEnabled()) {
                log.debug("Notificación de email deshabilitada para RUC {}", tc.getDocIdentidad_Nro());
                return;
            }

            String email = validator.resolveRecipientEmail(tc.getSN_EMail(), tc.getSN_EMail_Secundario());

            if (email == null) {
                log.warn("Sin email válido para receptor del doc {} | RUC emisor {}", tc.getDOC_Id(), tc.getDocIdentidad_Nro());
                saveInvalidJob(tc, client).subscribe().with(v -> {}, e -> log.error("Error guardando job inválido: {}", e.getMessage()));
                return;
            }

            suppressionService.isSuppressed(email)
                    .chain(suppressed -> {
                        if (suppressed) {
                            return saveSupressedJob(tc, client, email);
                        }
                        return saveNewJob(tc, client, email);
                    })
                    .onFailure().invoke(e -> log.error("Error encolando email para {}: {}", email, e.getMessage()))
                    .subscribe().with(v -> {}, e -> {});

        } catch (Exception e) {
            log.error("Error en enqueue de notificación: {}", e.getMessage());
        }
    }

    public void resend(String jobId) {
        jobRepo.findById(jobId)
                .chain(job -> {
                    job.setStatus(EmailJobStatus.PENDING);
                    job.setAttempts(0);
                    job.setNextRetryAt(LocalDateTime.now());
                    job.setLastError(null);
                    return jobRepo.save(job);
                })
                .invoke(j -> log.info("Job {} marcado para reenvío", jobId))
                .onFailure().invoke(e -> log.error("Error al marcar reenvío {}: {}", jobId, e.getMessage()))
                .subscribe().with(v -> {}, e -> {});
    }

    @Scheduled(every = "15s")
    public void processEmailQueue() {
        recoverStuckJobs();

        jobRepo.findByStatusAndNextRetryAtLessThanEqual(EmailJobStatus.PENDING, LocalDateTime.now())
                .select().first(BATCH_SIZE)
                .onItem().transformToUniAndMerge(job -> processJob(job).onFailure().recoverWithNull())
                .onFailure().invoke(e -> log.error("Error en worker de email: {}", e.getMessage()))
                .subscribe().with(v -> {}, e -> {});
    }

    private void recoverStuckJobs() {
        LocalDateTime stuckThreshold = LocalDateTime.now().minusMinutes(STUCK_JOB_MINUTES);
        jobRepo.findByStatusAndLockedAtLessThan(EmailJobStatus.IN_PROGRESS, stuckThreshold)
                .onItem().transformToUniAndMerge(job -> {
                    log.warn("Job stuck recuperado: {}", job.getId());
                    job.setStatus(EmailJobStatus.PENDING);
                    job.setLockedAt(null);
                    job.setNextRetryAt(LocalDateTime.now());
                    return jobRepo.save(job).onFailure().recoverWithNull();
                })
                .subscribe().with(v -> {}, e -> {});
    }

    private Uni<Void> processJob(EmailJob job) {
        job.setStatus(EmailJobStatus.IN_PROGRESS);
        job.setLockedAt(LocalDateTime.now());

        return jobRepo.save(job)
                .chain(saved -> Uni.createFrom().item(Unchecked.supplier(() -> doSend(saved)))
                        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                        .chain(msgId -> onSuccess(saved, msgId))
                        .onFailure().recoverWithUni(e -> onFailure(saved, e))
                );
    }

    private String doSend(EmailJob job) throws Exception {
        EmailMessage message = buildMessage(job);
        log.debug("Enviando via {}", emailProvider.providerName());
        return emailProvider.send(message);
    }

    private EmailMessage buildMessage(EmailJob job) {
        Client client = clientProperties.listaClientesOf(job.getRuc());
        String templateName = client.getEmail() != null ? client.getEmail().getTemplateName() : null;
        String replyTo = client.getEmail() != null ? client.getEmail().getReplyTo() : null;

        Map<String, String> tokens = templateService.buildTokens(
                job.getRecipientName(), job.getEmisorName(),
                job.getDocumentType(), job.getDocumentSeries(),
                job.getAmount(), job.getCurrency(), job.getPortalUrl()
        );

        String subject = templateService.buildSubject(job.getEmisorName(), job.getDocumentType(), job.getDocumentSeries());
        String body = templateService.buildBody(job.getRuc(), templateName, tokens);

        return new EmailMessage(job.getRecipientEmail(), job.getRecipientName(), replyTo, subject, body);
    }

    private Uni<Void> onSuccess(EmailJob job, String messageId) {
        EmailDelivery delivery = new EmailDelivery();
        delivery.setJobId(job.getId());
        delivery.setMessageId(messageId);
        delivery.setStatus(EmailDeliveryStatus.SENT);
        delivery.setSentAt(LocalDateTime.now());
        delivery.setProvider("SMTP/RESEND");

        job.setStatus(EmailJobStatus.SENT);
        job.setLockedAt(null);

        return deliveryRepo.save(delivery)
                .chain(d -> jobRepo.save(job))
                .invoke(j -> log.info("Email enviado OK: {} → {}", j.getDocumentSeries(), j.getRecipientEmail()))
                .replaceWithVoid();
    }

    private Uni<Void> onFailure(EmailJob job, Throwable error) {
        log.error("Fallo al enviar email a {}: {}", job.getRecipientEmail(), error.getMessage());

        EmailDelivery delivery = new EmailDelivery();
        delivery.setJobId(job.getId());
        delivery.setStatus(EmailDeliveryStatus.FAILED);
        delivery.setSentAt(LocalDateTime.now());
        delivery.setError(error.getMessage());

        job.setAttempts(job.getAttempts() + 1);
        job.setLastError(error.getMessage());
        job.setLockedAt(null);

        if (job.getAttempts() >= MAX_ATTEMPTS) {
            job.setStatus(EmailJobStatus.DEAD_LETTER);
            log.error("Job {} llegó a DEAD_LETTER tras {} intentos", job.getId(), job.getAttempts());
        } else {
            int delayMinutes = RETRY_MINUTES[Math.min(job.getAttempts() - 1, RETRY_MINUTES.length - 1)];
            job.setStatus(EmailJobStatus.PENDING);
            job.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            log.warn("Job {} programado para reintento #{} en {} minutos", job.getId(), job.getAttempts(), delayMinutes);
        }

        return deliveryRepo.save(delivery)
                .chain(d -> jobRepo.save(job))
                .replaceWithVoid();
    }

    private Uni<EmailJob> saveNewJob(TransacctionDTO tc, Client client, String email) {
        EmailJob job = buildJobBase(tc, client, email);
        job.setStatus(EmailJobStatus.PENDING);
        return jobRepo.save(job)
                .invoke(j -> log.info("Email encolado [{}] → {}", j.getDocumentSeries(), email));
    }

    private Uni<EmailJob> saveSupressedJob(TransacctionDTO tc, Client client, String email) {
        EmailJob job = buildJobBase(tc, client, email);
        job.setStatus(EmailJobStatus.SUPPRESSED);
        return jobRepo.save(job)
                .invoke(j -> log.warn("Email suprimido para {}", email));
    }

    private Uni<EmailJob> saveInvalidJob(TransacctionDTO tc, Client client) {
        EmailJob job = buildJobBase(tc, client, null);
        job.setStatus(EmailJobStatus.INVALID_EMAIL);
        return jobRepo.save(job);
    }

    private EmailJob buildJobBase(TransacctionDTO tc, Client client, String email) {
        EmailJob job = new EmailJob();
        job.setRuc(tc.getDocIdentidad_Nro());
        job.setEmisorName(client.getRazonSocial());
        job.setRecipientEmail(email);
        job.setRecipientName(tc.getSN_RazonSocial());
        job.setDocumentSeries(tc.getDOC_Id());
        job.setDocumentType(tc.getDOC_Codigo());
        job.setAmount(tc.getDOC_ImporteTotal());
        job.setPortalUrl(notificationProperties.getPortalBaseUrl());
        job.setAttempts(0);
        job.setCreatedAt(LocalDateTime.now());
        job.setNextRetryAt(LocalDateTime.now());
        return job;
    }
}
