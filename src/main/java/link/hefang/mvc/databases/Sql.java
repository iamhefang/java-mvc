package link.hefang.mvc.databases;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static link.hefang.helpers.CollectionHelper.addAll;
import static link.hefang.helpers.CollectionHelper.arrayListOf;

public class Sql {
   @NotNull
   private String sql;
   @NotNull
   private ArrayList<Object> params = arrayListOf();

   public Sql(@NotNull String sql, @NotNull Object[] params) {
      this.sql = sql;
      addAll(this.params, params);
   }

   @NotNull
   public String getSql() {
      return sql;
   }

   public Sql setSql(@NotNull String sql) {
      this.sql = sql;
      return this;
   }

   @NotNull
   public Object[] getParams() {
      return params.toArray();
   }

   public Sql addParams(@NotNull Object... params) {
      addAll(this.params, params);
      return this;
   }

   public Sql clearParams() {
      this.params.clear();
      return this;
   }
}
