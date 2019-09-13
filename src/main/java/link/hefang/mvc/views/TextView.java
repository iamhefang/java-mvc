package link.hefang.mvc.views;

import org.jetbrains.annotations.NotNull;

public class TextView extends BaseView {
   public static final String PLAIN = "text/plain";
   public static final String HTML = "text/html";
   public static final String JSON = "application/json";
   public static final String XML = "application/xml";
   public static final String CSS = "text/css";
   public static final String JAVASCRIPT = "application/javascript";

   @NotNull
   private String text;
   @NotNull
   private String contentType = PLAIN;

   public TextView(@NotNull String text) {
      this.text = text;
   }

   public TextView(@NotNull String text, @NotNull String contentType) {
      this.text = text;
      this.contentType = contentType;
   }

   @NotNull
   public String getText() {
      return text;
   }

   @Override
   @NotNull
   public String getContentType() {
      return contentType;
   }

   @Override
   public BaseView compile() {
      isCompiled = true;
      result = text.getBytes();
      return this;
   }
}
