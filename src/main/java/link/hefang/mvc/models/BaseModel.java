package link.hefang.mvc.models;

import link.hefang.helpers.JsonHelper;
import link.hefang.interfaces.IJsonObject;
import link.hefang.interfaces.IMapObject;
import link.hefang.mvc.Mvc;
import link.hefang.mvc.annotations.Model;
import link.hefang.mvc.annotations.ModelField;
import link.hefang.mvc.exceptions.NoModelAnnotationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.arrayListOf;
import static link.hefang.helpers.CollectionHelper.hashMapOf;
import static link.hefang.helpers.StringHelper.isNullOrBlank;

abstract public class BaseModel implements IMapObject, IJsonObject {
    private boolean exist = false;
    private boolean readOnly;
    private ArrayList<Field> fields = arrayListOf();
    private ArrayList<String> primaryKeys = arrayListOf();


    @NotNull
    private String table;

    public BaseModel() {
        Model model = getClass().getAnnotation(Model.class);
        if (model == null) {
            throw new NoModelAnnotationException(this);
        }
        table = model.value();
        readOnly = model.readOnly();
    }


    public boolean isExist() {
        return exist;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * 获取当前类对应的表名或视图名
     *
     * @return 表名或视图名
     */
    @NotNull
    public String getTable() {
        return table;
    }

    public String[] getPrimaryKeys() {
        return primaryKeys.toArray(new String[0]);
    }

    @NotNull
    @Override
    public String toJsonString() {
        return JsonHelper.encode(toMap());
    }

    @NotNull
    @Override
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = hashMapOf();
        HashMap<String, String> kvs = hashMapOf();
        for (Field field : fields) {
            if (field.hiddenInJson) continue;
            map.put(field.key, field.getValue());
            if (Mvc.isDebug()) {
                kvs.put(field.key, field.name);
            }
        }
        if (Mvc.isDebug()) {
            map.put("__kvs__", kvs);
        }
        return map;
    }

    @Nullable
    public Object getValFromKey(@NotNull String key) {
        for (Field field : fields) {
            if (key.equals(field.key)) return field.getValue();
        }
        return null;
    }

    public BaseModel setValToKey(@Nullable Object value, @NotNull String key) {
        for (Field field : fields) {
            if (key.equals(field.key)) {
                field.setValue(value);
            }
        }
        return this;
    }

    @Nullable
    public Object getValFromField(@NotNull String name) {
        for (Field field : fields) {
            if (name.equals(field.name)) return field.getValue();
        }
        return null;
    }

    public BaseModel setValToField(@Nullable Object value, @NotNull String key) {
        for (Field field : fields) {
            if (key.equals(field.name)) {
                field.setValue(value);
            }
        }
        return this;
    }

    public class Field {
        @NotNull
        private Class type;
        @NotNull
        private String key;
        @NotNull
        private String name;
        private boolean needTrim, hiddenInJson, isPrimaryKey, isBigData;
        @NotNull
        private java.lang.reflect.Field field;

        public Field(@NotNull ModelField modelField, @NotNull java.lang.reflect.Field field) {
            type = field.getType();
            name = isNullOrBlank(modelField.value()) ? field.getName() : modelField.value();
            key = field.getName();
            needTrim = modelField.needTrim();
            hiddenInJson = modelField.hiddenInJson();
            isPrimaryKey = modelField.isPrimaryKey();
            isBigData = modelField.isBigData();
            this.field = field;
        }

        @Nullable
        public Object getValue() {
            try {
                return field.get(BaseModel.this);
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        public Field setValue(@Nullable Object value) {
            try {
                field.set(BaseModel.this, value);
            } catch (IllegalAccessException ignored) {

            }
            return this;
        }

        @NotNull
        public Class getType() {
            return type;
        }

        @NotNull
        public String getKey() {
            return key;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public boolean isNeedTrim() {
            return needTrim;
        }

        public boolean isHiddenInJson() {
            return hiddenInJson;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean isBigData() {
            return isBigData;
        }
    }
}
