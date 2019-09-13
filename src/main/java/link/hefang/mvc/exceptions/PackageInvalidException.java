package link.hefang.mvc.exceptions;

public class PackageInvalidException extends RuntimeException {
   public PackageInvalidException() {
      this("应用根包名无效");
   }

   public PackageInvalidException(String message) {
      super(message);
   }

   public PackageInvalidException(String message, Throwable cause) {
      super(message, cause);
   }

   public PackageInvalidException(Throwable cause) {
      super(cause);
   }

   public PackageInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
