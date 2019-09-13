package link.hefang.mvc.views;

import link.hefang.helpers.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class JsonView extends TextView {
    public JsonView(@Nullable Object object) {
        super(JsonHelper.encode(object), TextView.JSON);
    }
}
