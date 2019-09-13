package link.hefang.mvc.entities;

import link.hefang.helpers.FileHelper;
import link.hefang.helpers.IOHelper;
import link.hefang.helpers.RandomHelper;
import link.hefang.mvc.Mvc;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static link.hefang.helpers.SystemHelper.javaIoTmpdir;

public class PostFile implements Closeable {
   private byte[] bytes;
   @NotNull
   private File tempFile;
   @NotNull
   private String fileName;
   @NotNull
   private String fieldName;
   @NotNull
   private String mimeType;

   public PostFile(@NotNull File tempFile, @NotNull String fileName, @NotNull String fieldName, @NotNull String mimeType) {
      this.tempFile = tempFile;
      this.fileName = fileName;
      this.fieldName = fieldName;
      this.mimeType = mimeType;
   }

   public PostFile(@NotNull byte[] bytes, @NotNull String fileName, @NotNull String fieldName, @NotNull String mimeType) {
      this(new File(path.getAbsolutePath() + File.separator + RandomHelper.guid() + "." + FileHelper.ext(fileName)),
         fileName, fieldName, mimeType);
      this.bytes = bytes;
      IOHelper.writeBytes(tempFile, bytes);
      Mvc.getLogger().debug("上传文件", "临时文件(" + tempFile.exists() + ")位置: " + tempFile.getAbsolutePath());
   }

   public byte[] getBytes() {
      return bytes;
   }

   @NotNull
   public File getTempFile() {
      return tempFile;
   }

   @NotNull
   public String getFileName() {
      return fileName;
   }

   @NotNull
   public String getFieldName() {
      return fieldName;
   }

   @NotNull
   public String getMimeType() {
      return mimeType;
   }

   public boolean delete() {
      if (tempFile.exists()) {
         return tempFile.delete();
      }
      return true;
   }

   @Override
   public void close() throws IOException {
      delete();
   }

   public static final File path = new File(javaIoTmpdir() + File.separatorChar + "java_upload_tmp_dir");
}
