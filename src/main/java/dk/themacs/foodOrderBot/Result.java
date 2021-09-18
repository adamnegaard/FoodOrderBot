package dk.themacs.foodOrderBot;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

public class Result<T> {

    private final Status status;
    private final T value;
    private final String errorMessage;
    private final boolean isError;
    private final String responseType;

    public Result(Status status, T value) {
        this(status, value, APPLICATION_JSON_VALUE);
    }

    public Result(Status status, T value, String responseType) {
        this(status, value, null, false, responseType);
    }

    public Result(Status status, T value, boolean isError) {
        this(status, value, null, isError,  APPLICATION_JSON_VALUE);
    }

    public Result(Status status, String errorMessage) {
        this(status, null, errorMessage, true, TEXT_PLAIN_VALUE);
    }

    public Result(Status status, T value, String errorMessage, boolean isError, String responseType) {
        this.status = status;
        this.value = value;
        this.errorMessage = errorMessage;
        this.isError = isError;
        this.responseType = responseType;
    }

    public Status getStatus() {
        return status;
    }

    public T getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return isError;
    }

    public String getResponseType() {
        return responseType;
    }
}