package link.hefang.mvc.logger;

import link.hefang.enums.LogLevel;
import link.hefang.helpers.TimeHelper;
import link.hefang.interfaces.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static link.hefang.mvc.Mvc.isDebug;

public class ConsoleLogger implements ILogger {
   private LogLevel level = LogLevel.ERROR;

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
      System.out.println("[" + TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss") + "](日志) " + name + "\n" + content + "\n ");
   }

   @Override
   public void notice(@NotNull CharSequence name, @Nullable CharSequence content) {
      if (level.getValue() < LogLevel.NOTICE.getValue() && !isDebug()) return;
      System.out.println("[" + TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss") + "](提示) " + name + "\n" + content + "\n ");
   }

   @Override
   public void warn(@NotNull CharSequence name, @Nullable CharSequence content, @Nullable Throwable throwable) {
      if (level.getValue() < LogLevel.WARN.getValue() && !isDebug()) return;
      System.err.println("[" + TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss") + "](警告) " + name + "\n" + content + "\n ");
      if (throwable != null) {
         throwable.printStackTrace();
      }
   }

   @Override
   public void error(@NotNull CharSequence name, @Nullable CharSequence content, @Nullable Throwable throwable) {
      if (level.getValue() < LogLevel.ERROR.getValue() && !isDebug()) return;
      System.err.println("[" + TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss") + "](错误) " + name + "\n" + content + "\n ");
      if (throwable != null) {
         throwable.printStackTrace();
      }
   }

   @Override
   public void debug(@NotNull CharSequence name, @Nullable CharSequence content) {
      if (!isDebug()) return;
      System.out.println("[" + TimeHelper.format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss") + "](调试) " + name + "\n" + content + "\n ");
   }
}
