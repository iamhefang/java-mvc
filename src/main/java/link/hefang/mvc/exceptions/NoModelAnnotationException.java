package link.hefang.mvc.exceptions;

import link.hefang.mvc.models.BaseModel;
import org.jetbrains.annotations.NotNull;

public class NoModelAnnotationException extends RuntimeException {
   public NoModelAnnotationException(@NotNull BaseModel baseModel) {
      this("模型类\"" + baseModel.getClass().getName() + "\"没有使用@Model(table)注解");
   }

   public NoModelAnnotationException() {
   }

   public NoModelAnnotationException(String message) {
      super(message);
   }

   public NoModelAnnotationException(String message, Throwable cause) {
      super(message, cause);
   }

   public NoModelAnnotationException(Throwable cause) {
      super(cause);
   }

   public NoModelAnnotationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
