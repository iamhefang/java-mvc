package link.hefang.mvc.entities;

import link.hefang.helpers.JsonHelper;
import link.hefang.interfaces.IJsonObject;
import link.hefang.interfaces.IMapObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static link.hefang.helpers.CollectionHelper.hashMapOf;
import static link.hefang.helpers.CollectionHelper.pair;

public class ApiResult implements IMapObject, IJsonObject {

   private boolean success = false;
   private Object result = null;
   private boolean needLogin = false;
   private boolean needPassword = false;
   private boolean needAdmin = false;
   private boolean needSuperAdmin = false;
   private boolean needPermission = false;
   private boolean needUnlock = false;
   private boolean needDeveloper = false;

   public boolean isSuccess() {
      return success;
   }

   public ApiResult setSuccess(boolean success) {
      this.success = success;
      return this;
   }

   @Nullable
   public Object getResult() {
      return result;
   }

   public ApiResult setResult(@Nullable Object result) {
      this.result = result;
      return this;
   }

   public boolean isNeedLogin() {
      return needLogin;
   }

   public ApiResult setNeedLogin(boolean needLogin) {
      this.needLogin = needLogin;
      return this;
   }

   public boolean isNeedPassword() {
      return needPassword;
   }

   public ApiResult setNeedPassword(boolean needPassword) {
      this.needPassword = needPassword;
      return this;
   }

   public boolean isNeedAdmin() {
      return needAdmin;
   }

   public ApiResult setNeedAdmin(boolean needAdmin) {
      this.needAdmin = needAdmin;
      return this;
   }

   public boolean isNeedSuperAdmin() {
      return needSuperAdmin;
   }

   public ApiResult setNeedSuperAdmin(boolean needSuperAdmin) {
      this.needSuperAdmin = needSuperAdmin;
      return this;
   }

   public boolean isNeedPermission() {
      return needPermission;
   }

   public ApiResult setNeedPermission(boolean needPermission) {
      this.needPermission = needPermission;
      return this;
   }

   public boolean isNeedUnlock() {
      return needUnlock;
   }

   public ApiResult setNeedUnlock(boolean needUnlock) {
      this.needUnlock = needUnlock;
      return this;
   }

   public boolean isNeedDeveloper() {
      return needDeveloper;
   }

   public ApiResult setNeedDeveloper(boolean needDeveloper) {
      this.needDeveloper = needDeveloper;
      return this;
   }

   @NotNull
   @Override
   public String toJsonString() {
      return JsonHelper.encode(toMap());
   }


   @NotNull
   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> toMap() {
      return hashMapOf(
         pair("success", success),
         pair("result", result),
         pair("needLogin", needLogin),
         pair("needPassword", needPassword),
         pair("needAdmin", needAdmin),
         pair("needSuperAdmin", needSuperAdmin),
         pair("needPermission", needPermission),
         pair("needUnlock", needUnlock),
         pair("needDeveloper", needDeveloper)
      );
   }
}
