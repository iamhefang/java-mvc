package link.hefang.mvc.entities;

import link.hefang.interfaces.IMapObject;
import link.hefang.mvc.Mvc;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static link.hefang.helpers.CollectionHelper.arrayOf;
import static link.hefang.helpers.CollectionHelper.hashMapOf;
import static link.hefang.helpers.CollectionHelper.pair;

public class Router implements IMapObject {
    @SuppressWarnings("unchecked")
    private Map<String, String> map = hashMapOf(
            pair("module", Mvc.getDefaultModule()),
            pair("controller", Mvc.getDefaultController()),
            pair("action", Mvc.getDefaultAction()),
            pair("theme", "default"),
            pair("cmd", ""),
            pair("format", "")
    );

    @NotNull
    @Override
    public Map<String, String> toMap() {
        return map;
    }

    public String getModule() {
        return this.map.get("module");
    }

    public Router setModule(@NotNull String module) {
        this.map.put("module", module.toLowerCase());
        return this;
    }

    public String getController() {
        return this.map.get("controller");
    }

    public Router setController(@NotNull String controller) {
        this.map.put("controller", controller.toLowerCase());
        return this;
    }

    public String getAction() {
        return this.map.get("action");
    }

    public Router setAction(@NotNull String action) {
        this.map.put("action", action.toLowerCase());
        return this;
    }

    public String getCmd() {
        return this.map.get("cmd");
    }

    public Router setCmd(@Nullable String cmd) {
        this.map.put("cmd", cmd == null ? null : cmd.toLowerCase());
        return this;
    }

    public String getFormat() {
        return this.map.get("format");
    }

    public Router setFormat(@Nullable String format) {
        this.map.put("format", format == null ? null : format.toLowerCase());
        return this;
    }

    public String getTheme() {
        return this.map.get("theme");
    }

    public Router setTheme(@NotNull String theme) {
        this.map.put("theme", theme.toLowerCase());
        return this;
    }


    @NotNull
    @Contract(pure = true)
    private static String group(String name) {
        return "/(?<" + name + ">[^\\s\\./]+)";
    }

    private static final String regexFormat = "(?<format>\\.([a-zA-Z0-9]+))?";
    private static final String[] items = arrayOf("module", "controller", "action", "cmd", "format");
    private static final Pattern[] regexs = arrayOf(
            Pattern.compile(group("module") + group("controller") + group("action") + group("cmd") + regexFormat),
            Pattern.compile(group("module") + group("controller") + group("action") + regexFormat),
            Pattern.compile(group("module") + group("controller") + regexFormat),
            Pattern.compile(group("action") + regexFormat)
    );

    @Nullable
    private static Matcher match(String input) {
        for (Pattern regex : regexs) {
            Matcher matcher = regex.matcher(input);
            if (matcher.matches()) return matcher;
        }
        return null;
    }

    public static Router parse(@NotNull HttpServletRequest request) {
        Router router = new Router();
        Matcher matcher = match(request.getRequestURI());
        if (matcher != null) {
            for (String item : items) {
                String value = matcher.group(item);
                router.map.put(item, value == null ? router.map.get(item) : value);
            }
        }
        return router;
    }

}
