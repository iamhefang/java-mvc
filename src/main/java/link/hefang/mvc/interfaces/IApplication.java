package link.hefang.mvc.interfaces;

import link.hefang.mvc.entities.Router;
import link.hefang.mvc.views.BaseView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.arrayListOf;

public interface IApplication {

   @Nullable
   default Collection<String> trustServerHost() {
      return arrayListOf();
   }

   @NotNull
   Map<String, Object> onInit(@Nullable ServletConfig config);

   @Nullable
   Router onRequest(@NotNull HttpServletRequest request);

   @Nullable
   BaseView onException(@NotNull Throwable e);
}
