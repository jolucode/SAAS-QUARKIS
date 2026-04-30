package service.cloud.request.clientRequest.model;

public enum ErrorCategory {
    INVALID_REQUEST("invalid-request", 400),
    ARGUMENT_MISMATCH("argument-mismatch", 400),
    UNAUTHORIZED("unauthorized", 401),
    FORBIDDEN("forbidden", 403),
    RESOURCE_NOT_FOUND("resource-not-found", 404),
    CONFLICT("conflict", 409),
    PRECONDITION_FAILED("precondition-failed", 412),
    EXTERNAL_ERROR("external-error", 500),
    HOST_NOT_FOUND("host-not-found", 500),
    UNEXPECTED("unexpected", 500),
    NOT_IMPLEMENTED("not-implemented", 501),
    SERVICE_UNAVAILABLE("service-unavailable", 503),
    EXTERNAL_TIMEOUT("external-timeout", 503);

    private static final String PROPERTY_PREFIX = "application.atlas.error-code.";
    private final String property;
    private final int httpStatus;

    ErrorCategory(String property, int httpStatus) {
        this.property = "application.atlas.error-code.".concat(property);
        this.httpStatus = httpStatus;
    }

    private String codeProperty() {
        return this.property + ".code";
    }

    private String descriptionProperty() {
        return this.property + ".description";
    }

    private String errorTypeProperty() {
        return this.property + ".error-type";
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public String getErrorType() {
        return "Technical";
    }

    public String getCode() {
        return "TL9999";
    }

    public String getDescription() {
        return "Sin descripcion configurada.";
    }
}