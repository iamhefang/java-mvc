package link.hefang.mvc.exceptions;

public class ViewNotCompiledException extends RuntimeException {
   public ViewNotCompiledException() {
   }

   public ViewNotCompiledException(String message) {
      super(message);
   }

   public ViewNotCompiledException(String message, Throwable cause) {
      super(message, cause);
   }

   public ViewNotCompiledException(Throwable cause) {
      super(cause);
   }

   public ViewNotCompiledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
