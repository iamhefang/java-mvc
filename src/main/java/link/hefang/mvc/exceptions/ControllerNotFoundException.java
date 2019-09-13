package link.hefang.mvc.exceptions;

import link.hefang.mvc.entities.Router;
import org.jetbrains.annotations.NotNull;

public class ControllerNotFoundException extends Exception {
    public ControllerNotFoundException(@NotNull Router router) {
        this(router.toString());
    }

    public ControllerNotFoundException() {
    }

    public ControllerNotFoundException(String message) {
        super(message);
    }

    public ControllerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControllerNotFoundException(Throwable cause) {
        super(cause);
    }

    public ControllerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
