package com.bcd.base.support_jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SqlUtil {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtil.class);

    public record InsertSqlResult<T>(String sql, Class<T> clazz, Field[] insertFields) {
        public int[] insertBatch(final List<T> dataList, final JdbcTemplate jdbcTemplate) {
            final List<Object[]> paramList = new ArrayList<>();
            try {
                for (T t : dataList) {
                    final Object[] param = new Object[insertFields.length];
                    for (int i = 0; i < insertFields.length; i++) {
                        param[i] = insertFields[i].get(t);
                    }
                    paramList.add(param);
                }
            } catch (IllegalAccessException ex) {
                throw BaseRuntimeException.getException(ex);
            }
            return jdbcTemplate.batchUpdate(sql, paramList);
        }
    }

    public record UpdateSqlResult<T>(String sql, Class<T> clazz, Field[] updateFields, Field[] whereFields) {
        public int[] updateBatch(final List<T> dataList, final JdbcTemplate jdbcTemplate) {
            final List<Object[]> paramList = new ArrayList<>();
            final int fieldsLength = updateFields.length;
            final int whereFieldsLength = whereFields.length;
            try {
                for (T t : dataList) {
                    final Object[] param = new Object[fieldsLength + whereFieldsLength];
                    for (int i = 0; i < fieldsLength; i++) {
                        param[i] = updateFields[i].get(t);
                    }
                    for (int i = 0; i < whereFieldsLength; i++) {
                        param[i + fieldsLength] = whereFields[i].get(t);
                    }
                    paramList.add(param);
                }
            } catch (IllegalAccessException ex) {
                throw BaseRuntimeException.getException(ex);
            }
            return jdbcTemplate.batchUpdate(sql, paramList);
        }
    }


    /**
     * 将class转换为insert sql语句对象
     * 会获取父类的字段
     * insert字段会去除掉静态字段
     *
     * @param clazz                实体类class
     * @param table                表名
     * @param fieldFilter          字段名过滤器、false则排除掉、会应用于insert字段
     * @param camelCaseToUnderline 生成的sql中、列名是否是由 字段名驼峰格式转下划线格式而来、会应用于insert字段
     * @param <T>
     * @return
     */
    public static <T> InsertSqlResult<T> toInsertSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter, boolean camelCaseToUnderline) {
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        final List<Field> insertFieldList = new ArrayList<>();
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && fieldFilter.apply(field)) {
                insertFieldList.add(field);
            }
        }
        final StringJoiner sj1 = new StringJoiner(",");
        final StringJoiner sj2 = new StringJoiner(",");
        for (Field field : insertFieldList) {
            field.setAccessible(true);
            if (camelCaseToUnderline) {
                sj1.add(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sj1.add(field.getName());
            }
            sj2.add("?");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(table);
        sb.append("(");
        sb.append(sj1);
        sb.append(") values(");
        sb.append(sj2);
        sb.append(")");
        return new InsertSqlResult<>(sb.toString(), clazz, insertFieldList.toArray(new Field[0]));
    }

    /**
     * 将class转换为update sql语句对象
     * 会获取父类的所有字段
     * update字段会去除掉静态字段
     *
     * @param clazz                实体类
     * @param table                表名
     * @param fieldFilter          字段名过滤器、false则排除掉、会应用于update字段
     * @param camelCaseToUnderline 生成的sql中、列名是否是由 字段名驼峰格式转下划线格式而来、会应用于update字段和where字段
     * @param whereFieldNames      where字段名
     * @param <T>
     * @return
     */
    public static <T> UpdateSqlResult<T> toUpdateSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter, boolean camelCaseToUnderline, String... whereFieldNames) {
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        final List<Field> updateFieldList = new ArrayList<>();
        final Map<String, Field> whereMap = new HashMap<>();
        final Set<String> whereColumnSet = Set.of(whereFieldNames);
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && fieldFilter.apply(field)) {
                updateFieldList.add(field);
            }
            if (whereColumnSet.contains(field.getName())) {
                whereMap.put(field.getName(), field);
            }
        }

        final List<Field> whereFieldList = new ArrayList<>();
        final Set<String> whereNullFieldSet = new HashSet<>();
        for (String whereColumn : whereFieldNames) {
            if (whereMap.containsKey(whereColumn)) {
                whereFieldList.add(whereMap.get(whereColumn));
            } else {
                whereNullFieldSet.add(whereColumn);
            }
        }
        if (!whereNullFieldSet.isEmpty()) {
            throw BaseRuntimeException.getException("whereField[{}] not exist", Arrays.toString(whereNullFieldSet.toArray(new String[0])));
        }

        final StringBuilder sb = new StringBuilder("update ");
        sb.append(table);
        sb.append(" set ");
        for (int i = 0; i < updateFieldList.size(); i++) {
            final Field field = updateFieldList.get(i);
            field.setAccessible(true);
            if (i > 0) {
                sb.append(",");
            }
            if (camelCaseToUnderline) {
                sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sb.append(field.getName());
            }
            sb.append("=?");
        }
        sb.append(" where ");
        for (int i = 0; i < whereFieldList.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            final Field field = whereFieldList.get(i);
            field.setAccessible(true);
            if (camelCaseToUnderline) {
                sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sb.append(field.getName());
            }
            sb.append("=?");
        }
        return new UpdateSqlResult<>(sb.toString(), clazz, updateFieldList.toArray(new Field[0]), whereFieldList.toArray(new Field[0]));
    }

}
