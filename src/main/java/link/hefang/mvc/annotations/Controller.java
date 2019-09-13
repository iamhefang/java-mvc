package link.hefang.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Controller {
   /**
    * 控制器名
    *
    * @return 控制器名
    */
   String value() default "";

   /**
    * 是否是一个控制器
    *
    * @return 是否是一个控制器
    */
   boolean isController() default true;

   /**
    * 模块名
    *
    * @return 模块名
    */
   String module() default "";
}
