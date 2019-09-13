package link.hefang.mvc.models;

import link.hefang.helpers.TimeHelper;
import link.hefang.mvc.Mvc;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.indexOf;
import static link.hefang.helpers.RandomHelper.guid;

abstract public class BaseLoginModel extends BaseModel {

   public static final String LOGIN_SESSION_KEY = guid();

   private String loginIP = null;
   private long loginTime = -1;
   private String loginUserAgent = null;
   private boolean isLockedScreen = false;

   abstract public boolean isAdmin();

   abstract public boolean isSuperAdmin();

   abstract public String getRoleName();

   public boolean isDeveloper() {
      return indexOf(Mvc.getDevelopers(), getID()) != -1;
   }

   @NotNull
   @Override
   public Map<String, Object> toMap() {
      HashMap<String, Object> map = (HashMap<String, Object>) super.toMap();
      map.put("isSuperAdmin", isSuperAdmin());
      map.put("isAdmin", isAdmin());
      map.put("isDeveloper", isDeveloper());
      map.put("isLockedScreen", isLockedScreen);
      map.put("loginIP", loginIP);
      map.put("loginUserAgent", loginUserAgent);
      map.put("loginTime", TimeHelper.format(loginTime, "yyyy-MM-dd HH:mm:ss"));
      map.put("roleName", getRoleName());
      return map;
   }

   @NotNull
   abstract public String getID();

   @NotNull
   abstract public BaseLoginModel setID(@NotNull String id);

   @NotNull
   abstract public String getRoleID();

   @NotNull
   abstract public BaseLoginModel setRoleID(@NotNull String roleID);

   public String getLoginIP() {
      return loginIP;
   }

   public BaseLoginModel setLoginIP(String loginIP) {
      this.loginIP = loginIP;
      return this;
   }

   public long getLoginTime() {
      return loginTime;
   }

   public BaseLoginModel setLoginTime(long loginTime) {
      this.loginTime = loginTime;
      return this;
   }

   public String getLoginUserAgent() {
      return loginUserAgent;
   }

   public BaseLoginModel setLoginUserAgent(String loginUserAgent) {
      this.loginUserAgent = loginUserAgent;
      return this;
   }

   public boolean isLockedScreen() {
      return isLockedScreen;
   }

   public BaseLoginModel setLockedScreen(boolean lockedScreen) {
      isLockedScreen = lockedScreen;
      return this;
   }
}
