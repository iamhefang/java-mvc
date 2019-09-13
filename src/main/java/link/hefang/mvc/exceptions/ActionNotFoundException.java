package link.hefang.mvc.exceptions;

import link.hefang.mvc.entities.Router;
import org.jetbrains.annotations.NotNull;

public class ActionNotFoundException extends Exception {
   public ActionNotFoundException(@NotNull Router router) {
      this(router.toString());
   }

   public ActionNotFoundException() {
   }

   public ActionNotFoundException(String message) {
      super(message);
   }

   public ActionNotFoundException(String message, Throwable cause) {
      super(message, cause);
   }

   public ActionNotFoundException(Throwable cause) {
      super(cause);
   }

   public ActionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
