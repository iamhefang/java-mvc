package link.hefang.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Model {
    /**
     * 模块对应的表或视图
     *
     * @return 表名或视图名
     */
    String value();

    /**
     * 该模型是否为只读模型, 比如该模型对应是视图
     *
     * @return 是否为只读模型
     */
    boolean readOnly() default false;
}
