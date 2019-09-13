package link.hefang.mvc.views;

import link.hefang.helpers.IOHelper;
import link.hefang.mvc.exceptions.ViewNotCompiledException;
import link.hefang.string.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.isNullOrEmpty;

abstract public class BaseView {
   protected String contentType;
   protected boolean isCompiled = false;
   protected byte[] result = new byte[0];
   protected HashMap<String, Object> data = new HashMap<>();
   protected Charset charset = Charsets.UTF_8;

   public void render(@NotNull HttpServletResponse response) throws IOException {
      if (!isCompiled) throw new ViewNotCompiledException();
      response.setContentType(contentType + ";" + charset.displayName());
      response.setContentLength(result.length);
      ServletOutputStream out = null;
      try {
         out = response.getOutputStream();
         if (!isNullOrEmpty(result)) {
            out.write(result);
            out.flush();
         }
      } finally {
         IOHelper.close(out);
      }
   }

   public String getContentType() {
      return contentType;
   }

   public int getContentLength() {
      return result.length;
   }

   public BaseView addData(@NotNull String name, @Nullable Object value) {
      data.put(name, value);
      return this;
   }

   public BaseView addData(@NotNull Map<String, Object> data) {
      this.data.putAll(data);
      return this;
   }

   public Object removeData(@NotNull String name) {
      return data.remove(name);
   }

   public BaseView clearData() {
      data.clear();
      return this;
   }

   public abstract BaseView compile();
}
