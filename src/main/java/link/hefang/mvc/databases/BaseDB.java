package link.hefang.mvc.databases;

import link.hefang.collections.Pager;
import link.hefang.mvc.Mvc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static link.hefang.helpers.CollectionHelper.*;

abstract public class BaseDB {
    @NotNull
    private DataSource ds;

    public BaseDB(@NotNull DataSource ds) {
        this.ds = ds;
    }

    abstract public int insert(@NotNull String table, @NotNull Map<String, ?> data) throws SQLException;

    public int insert(@NotNull DbParam param) throws SQLException {
        if (isNullOrEmpty(param.getData())) {
            return 0;
        }
        return insert(param.getTable(), param.getData());
    }

    abstract public int transInsert(@NotNull DbParam... params) throws SQLException;

    public int transInsert(@NotNull Collection<DbParam> params) throws SQLException {
        return transInsert(params.toArray(new DbParam[0]));
    }

    abstract public int delete(@NotNull String table, @Nullable String where) throws SQLException;

    public int delete(@NotNull String table) throws SQLException {
        return delete(table, null);
    }

    public int delete(@NotNull DbParam param) throws SQLException {
        return delete(param.getTable(), param.getWhere());
    }

    abstract public int transDelete(@NotNull DbParam... params) throws SQLException;

    public int transDelete(@NotNull Collection<DbParam> params) throws SQLException {
        return transDelete(params.toArray(new DbParam[0]));
    }

    abstract public int update(@NotNull String table, @NotNull Map<String, ?> data, @Nullable String where) throws SQLException;

    public int update(@NotNull String table, @NotNull Map<String, ?> data) throws SQLException {
        return update(table, data, null);
    }

    public int update(@NotNull DbParam param) throws SQLException {
        if (isNullOrEmpty(param.getData())) {
            return 0;
        }
        return update(param.getTable(), param.getData(), param.getWhere());
    }

    abstract public int transUpdate(@NotNull DbParam... params) throws SQLException;

    public int transUpdate(@NotNull Collection<DbParam> params) throws SQLException {
        return transUpdate(params.toArray(new DbParam[0]));
    }

    abstract public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String where
            , @Nullable String search
            , @Nullable SqlSort[] sort
            , @Nullable String[] field2search
            , @Nullable String[] field2show
    ) throws SQLException;

    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , @Nullable String where
            , @Nullable String search
            , @Nullable SqlSort[] sort
            , @Nullable String[] field2search
    ) throws SQLException {
        return pager(table, pgIndex, Mvc.getDefaultPgSize(), where, search, sort, field2search, null);
    }

    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String search
            , @Nullable SqlSort[] sort
            , @Nullable String[] field2search
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, null, search, sort, field2search);
    }


    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String where
            , @Nullable String search
            , @Nullable SqlSort[] sort
            , @Nullable String[] field2search
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, where, search, sort, field2search, null);
    }

    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String where
            , @Nullable String search
            , @Nullable SqlSort[] sort
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, where, search, sort, null);
    }

    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String where
            , @Nullable String search
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, where, search, null);
    }

    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
            , @Nullable String where
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, where, null);
    }


    public Pager<Map<String, ?>> pager(
            @NotNull String table
            , int pgIndex
            , int pgSize
    ) throws SQLException {
        return pager(table, pgIndex, pgSize, null);
    }

    public Pager<Map<String, ?>> pager(@NotNull String table, int pgIndex) throws SQLException {
        return pager(table, pgIndex, Mvc.getDefaultPgSize());
    }

    public Map<String, ?> row(
            @NotNull String table
            , @Nullable String where
            , @Nullable String[] fields
    ) throws SQLException {
        Pager<Map<String, ?>> pager = pager(table, 1, 1, where, null, null, null, fields);
        if (pager.isEmpty()) return hashMapOf();
        return pager.get(0);
    }


    public Map<String, ?> row(
            @NotNull String table
            , @Nullable String where
    ) throws SQLException {
        return row(table, where, null);
    }

    public Map<String, ?> row(@NotNull String table) throws SQLException {
        return row(table, null);
    }

    @Nullable
    public Object single(@NotNull String table, @NotNull String column, @Nullable String where) throws SQLException {
        Map<String, ?> row = row(table, where, arrayOf(column));
        if (row.isEmpty()) return null;
        return row.get(row.keySet().iterator().next());
    }

    public Object single(@NotNull String table, @NotNull String column) throws SQLException {
        return single(table, column, null);
    }

    public int transaction(@NotNull Sql... sqls) throws SQLException {
        logSqls(sqls);
        final Connection connection = ds.getConnection();
        try {
            connection.setAutoCommit(false);
            int res = 0;
            for (Sql sql : sqls) {
                res += makeStatement(connection, sql).executeUpdate();
            }
            return res;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    public int transaction(@NotNull Collection<Sql> sqls) throws SQLException {
        return transaction(sqls.toArray(new Sql[0]));
    }

    public int executeUpdate(@NotNull Sql sql) throws SQLException {
        logSqls(sql);
        try (final Connection connection = ds.getConnection()) {
            return makeStatement(connection, sql).executeUpdate();
        }
    }

    @NotNull
    public ArrayList<HashMap<String, Object>> executeQuery(@NotNull Sql sql) throws SQLException {
        logSqls(sql);
        try (final Connection connection = ds.getConnection()) {
            try (final ResultSet result = makeStatement(connection, sql).executeQuery()) {
                ArrayList<HashMap<String, Object>> list = arrayListOf();
                while (result.next()) {
                    list.add(resultToMap(result));
                }
                return list;
            }
        }
    }

    @NotNull
    public DataSource getDataSource() {
        return ds;
    }

    /**
     * 将 sql 写入到日志
     *
     * @param sqls 要写入日志的sql
     */
    public static void logSqls(@NotNull Sql... sqls) {
        if (isNullOrEmpty(sqls)) return;
        final StringBuilder builder = new StringBuilder("\n");
        for (Sql sql : sqls) {
            builder.append("\nSQL语句: ").append(sql.getSql()).append("\n")
                    .append("参数:");
            final Object[] params = sql.getParams();
            for (int i = 0; i < params.length; i++) {
                final Object param = params[i];
                builder.append("\n\t")
                        .append(i).append("(").append(param.getClass().getSimpleName()).append("):").append(param);
            }
        }
        Mvc.getLogger().debug(sqls.length == 1 ? "执行SQL语句" : ("执行SQL事务(" + sqls.length + ")"), builder.toString());
    }

    public static PreparedStatement makeStatement(@NotNull Connection connection, @NotNull Sql sql) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(sql.getSql());
        final Object[] params = sql.getParams();
        int i = 0;
        for (Object param : params) {
            if (param == null) {
                statement.setNull(i, Types.NULL);
            } else if (param instanceof Short) {
                statement.setShort(i, (Short) param);
            } else if (param instanceof Integer) {
                statement.setInt(i, (Integer) param);
            } else if (param instanceof Long) {
                statement.setLong(i, (Long) param);
            } else if (param instanceof Float) {
                statement.setFloat(i, (Float) param);
            } else if (param instanceof Double) {
                statement.setDouble(i, (Double) param);
            } else if (param instanceof Boolean) {
                statement.setBoolean(i, (Boolean) param);
            } else if (param instanceof Byte) {
                statement.setByte(i, (Byte) param);
            } else if (param instanceof Byte[]) {
                statement.setBytes(i, (byte[]) param);
            } else if (param.getClass().isArray()) {
                statement.setArray(i, (Array) param);
            } else if (param instanceof Timestamp) {
                statement.setTimestamp(i, (Timestamp) param);
            } else if (param instanceof Time) {
                statement.setTime(i, (Time) param);
            } else if (param instanceof Date) {
                statement.setDate(i, (Date) param);
            } else {
                statement.setString(i, param + "");
            }
            i++;
        }
        return statement;
    }

    public static HashMap<String, Object> resultToMap(@NotNull ResultSet result) throws SQLException {
        final ResultSetMetaData md = result.getMetaData();
        final int count = md.getColumnCount();
        HashMap<String, Object> map = hashMapOf();
        for (int i = 0; i < count; i++) {
            map.put(md.getColumnName(i), result.getObject(i));
        }
        return map;
    }
}
