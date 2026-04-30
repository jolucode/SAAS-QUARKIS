package service.cloud.request.clientRequest.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MongoClientProducer {

    @Produces
    @ApplicationScoped
    public MongoClient mongoClient(@ConfigProperty(name = "quarkus.mongodb.connection-string") String uri) {
        return MongoClients.create(uri);
    }
}
