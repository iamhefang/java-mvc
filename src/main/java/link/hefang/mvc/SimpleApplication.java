package link.hefang.mvc;

import link.hefang.mvc.entities.Router;
import link.hefang.mvc.interfaces.IApplication;
import link.hefang.mvc.views.BaseView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.hashMapOf;

public class SimpleApplication implements IApplication {
   @NotNull
   @Override
   public Map<String, Object> onInit(@Nullable ServletConfig config) {
      return hashMapOf();
   }

   @Nullable
   @Override
   public Router onRequest(@NotNull HttpServletRequest request) {
      return null;
   }

   @Nullable
   @Override
   public BaseView onException(@NotNull Throwable e) {
      Mvc.getLogger().error("未知异常", e.getMessage(), e);
      return null;
   }
}
