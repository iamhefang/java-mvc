package link.hefang.mvc.databases;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DbParam {
   @NotNull
   private String table;
   @Nullable
   private Map<String, ?> data;
   @Nullable
   private String where;

   public DbParam(@NotNull String table) {
      this.table = table;
   }

   public DbParam(@NotNull String table, @Nullable String where) {
      this.table = table;
      this.where = where;
   }

   public DbParam(@NotNull String table, @NotNull Map<String, ?> data) {
      this.table = table;
      this.data = data;
   }

   public DbParam(@NotNull String table, @NotNull Map<String, ?> data, @Nullable String where) {
      this.table = table;
      this.data = data;
      this.where = where;
   }

   @NotNull
   public String getTable() {
      return table;
   }

   public DbParam setTable(@NotNull String table) {
      this.table = table;
      return this;
   }

   @Nullable
   public Map<String, ?> getData() {
      return data;
   }

   public DbParam setData(@NotNull Map<String, ?> data) {
      this.data = data;
      return this;
   }

   @Nullable
   public String getWhere() {
      return where;
   }

   public DbParam setWhere(@Nullable String where) {
      this.where = where;
      return this;
   }
}
