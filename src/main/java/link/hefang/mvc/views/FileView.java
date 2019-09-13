package link.hefang.mvc.views;

import link.hefang.helpers.FileHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static link.hefang.helpers.IOHelper.readBytes;

public class FileView extends BaseView {
   private File file;

   public FileView(@NotNull File file) {
      this(file, FileHelper.mimeType(file));
   }

   public FileView(@NotNull File file, String mimeType) {
      super();
      contentType = mimeType;
      this.file = file;
   }

   @Override
   public BaseView compile() {
      isCompiled = true;
      result = readBytes(file);
      return this;
   }
}
