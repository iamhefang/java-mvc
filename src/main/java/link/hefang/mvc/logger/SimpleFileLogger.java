package link.hefang.mvc.logger;

import link.hefang.enums.LogLevel;
import link.hefang.helpers.StringHelper;
import link.hefang.helpers.TimeHelper;
import link.hefang.interfaces.ILogger;
import link.hefang.mvc.Mvc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static link.hefang.mvc.Mvc.isDebug;

public class SimpleFileLogger implements ILogger {
   private LogLevel level = LogLevel.WARN;

   private static final String savePath = Mvc.getProperty("logger.save.path", Mvc.webInfPath + File.separatorChar + "logs");

   @NotNull
   @Override
   public LogLevel getLevel() {
      return level;
   }

   @Override
   public ILogger setLevel(@NotNull LogLevel logLevel) {
      this.level = logLevel;
      return this;
   }

   @Override
   public void log(@NotNull CharSequence name, @Nullable CharSequence content) {
      if (level == LogLevel.NONE && !isDebug()) return;
      write("log", name, content, null);
   }

   @Override
   public void notice(@NotNull CharSequence name, @Nullable CharSequence content) {
      if (level.getValue() < LogLevel.NOTICE.getValue() && !isDebug()) return;
      write("notice", name, content, null);
   }

   @Override
   public void warn(@NotNull CharSequence name, @Nullable CharSequence content, @Nullable Throwable throwable) {
      if (level.getValue() < LogLevel.WARN.getValue() && !isDebug()) return;
      write("warn", name, content, throwable);
   }

   @Override
   public void error(@NotNull CharSequence name, @Nullable CharSequence content, @Nullable Throwable throwable) {
      if (level.getValue() < LogLevel.ERROR.getValue() && !isDebug()) return;
      write("error", name, content, throwable);
   }

   @Override
   public void debug(@NotNull CharSequence name, @Nullable CharSequence content) {
      if (!isDebug()) return;
      write("debug", name, content, null);
   }

   private static void write(@NotNull CharSequence name, @NotNull CharSequence title, @Nullable CharSequence content, @Nullable Throwable throwable) {
      final File file = new File(savePath + TimeHelper.format(
         File.separatorChar + "yyyy-MM" + File.separatorChar + "dd" + File.separatorChar
      ) + name + ".log");
      final String time = TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss");
      final String logContent = StringHelper.contact("[", time, "](", name, ") ", title, "\n", content, "\n\n");
      try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
         raf.seek(raf.length());
         try (FileChannel channel = raf.getChannel()) {
            while (true) {
               try (FileLock ignored = channel.tryLock()) {
                  raf.write(logContent.getBytes());
                  break;
               } catch (Throwable e) {
                  try {
                     Thread.sleep(100);
                  } catch (Throwable ignored) {

                  }
               }
            }
         }

      } catch (IOException e) {
         System.err.println("写日志时出现异常");
         e.printStackTrace();
      }
   }
}
