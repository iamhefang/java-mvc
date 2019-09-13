package link.hefang.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NeedLogin {
   /**
    * 该动作或控制器是否需要登录才能使用
    *
    * @return 是否需要登录
    */
   boolean value() default true;

   /**
    * 未登录时的提示信息
    *
    * @return 提示信息
    */
   String needLoginMessage() default "您当前未登录, 请先登录";

   /**
    * 是否需要超级管理员
    *
    * @return 是否需要超级管理员
    */
   boolean needSuperAdmin() default false;

   /**
    * 登录用户不是超级管理员时的提示信息
    *
    * @return 登录用户不是超级管理员时的提示信息
    */
   String needSuperAdminMessage() default "该功能只有超级管理员才能使用";

   /**
    * 是否需要管理员
    *
    * @return 是否需要管理员
    */
   boolean needAdmin() default false;

   /**
    * 登录用户不是管理员时的提示信息
    *
    * @return 登录用户不是管理员时的提示信息
    */
   String needAdminMessage() default "该功能只有管理员才能使用";

   /**
    * 是否需要解锁
    *
    * @return 是否需要解锁
    */
   boolean needUnLock() default false;

   /**
    * 未解锁时的提示信息
    *
    * @return 未解锁时的提示信息
    */
   String needUnLockMessage() default "您当前已锁屏, 请先解锁";

   /**
    * 是否需要开发者身份
    *
    * @return 是否需要开发者身份
    */
   boolean needDeveloper() default false;

   /**
    * 不是开发者时的提示信息
    *
    * @return 不是开发者时的提示信息
    */
   String needDeveloperMessage() default "该功能正在开发中";

   /**
    * 是否需要指定角色
    *
    * @return 是否需要指定角色
    */
   String[] needRoles() default {};

   /**
    * 不是指定角色时的提示信息
    *
    * @return 不是指定角色时的提示信息
    */
   String needRolesMessage() default "该功能只有具备权限后才能使用";
}
