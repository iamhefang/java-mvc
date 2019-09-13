package link.hefang.mvc.databases;

import org.jetbrains.annotations.NotNull;

public class SqlSort {
    @NotNull
    private String key;
    @NotNull
    private Type type = Type.DEFAULT;
    private boolean nullsFirst = false;


    public SqlSort(@NotNull String key) {
        this.key = key;
    }

    public SqlSort(@NotNull String key, @NotNull Type type) {
        this.key = key;
        this.type = type;
    }

    public SqlSort(@NotNull String key, @NotNull Type type, boolean nullsFirst) {
        this.key = key;
        this.type = type;
        this.nullsFirst = nullsFirst;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public SqlSort setKey(@NotNull String key) {
        this.key = key;
        return this;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public SqlSort setType(@NotNull Type type) {
        this.type = type;
        return this;
    }

    public boolean isNullsFirst() {
        return nullsFirst;
    }

    public SqlSort setNullsFirst(boolean nullsFirst) {
        this.nullsFirst = nullsFirst;
        return this;
    }

    public static enum Type {
        ASC, DESC, DEFAULT
    }
}
