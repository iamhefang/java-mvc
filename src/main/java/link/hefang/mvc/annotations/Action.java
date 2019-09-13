package link.hefang.mvc.annotations;

import link.hefang.network.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Action {
   /**
    * 动作名称
    *
    * @return 动作名称
    */
   String value() default "";

   /**
    * 该方法是否是一个动作
    *
    * @return 是否是一个动作
    */
   boolean isAction() default true;

   /**
    * 该动作可接受的请求方式
    *
    * @return 请求方式
    */
   RequestMethod method() default RequestMethod.GET;
}
