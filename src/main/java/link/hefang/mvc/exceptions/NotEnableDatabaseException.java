package link.hefang.mvc.exceptions;

public class NotEnableDatabaseException extends RuntimeException {
   public NotEnableDatabaseException() {
   }

   public NotEnableDatabaseException(String message) {
      super(message);
   }

   public NotEnableDatabaseException(String message, Throwable cause) {
      super(message, cause);
   }

   public NotEnableDatabaseException(Throwable cause) {
      super(cause);
   }

   public NotEnableDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
