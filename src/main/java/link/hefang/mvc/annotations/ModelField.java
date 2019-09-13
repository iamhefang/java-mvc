package link.hefang.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ModelField {
    /**
     * 对应数据库中的字段
     */
    String value() default "";

    /**
     * 是否为主键
     */
    boolean isPrimaryKey() default false;

    /**
     * 当前字段是否是对应数据库中的字段
     */
    boolean isField() default true;

    /**
     * 是否是大量数据的字段, 比如 文章的内容字段
     */
    boolean isBigData() default false;

    /**
     * 该字段可否被搜索
     */
    boolean isSearchable() default false;

    /**
     * 不在json中显示
     */
    boolean hiddenInJson() default false;

    /**
     * 是否需要自动去除首尾空格
     */
    boolean needTrim() default false;
}
