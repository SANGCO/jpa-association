package persistence.sql.ddl;

import jakarta.persistence.*;
import persistence.sql.ddl.dialect.Dialect;
import utils.CustomStringBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.JdbcTypeMapper.getJdbcTypeForClass;

public class FieldMetadataExtractor {

    private final Field field;

    public FieldMetadataExtractor(Field field) {
        this.field = field;
    }

    public String getDefinition(Dialect dialect) {
        return new CustomStringBuilder()
                .append(getColumnName())
                .append(dialect.getType(getJdbcTypeForClass(field.getType())))
                .append(getColumnOptionValue(dialect))
                .toString();
    }

    public String getColumnName(Object entity) throws NoSuchFieldException, IllegalAccessException {
        Field entityFiled = entity.getClass().getDeclaredField(field.getName());
        entityFiled.setAccessible(true);
        if (entityFiled.get(entity) != null) {
            return getColumnName();
        }

        return "";
    }

    public String getValueFrom(Object entity) throws NoSuchFieldException, IllegalAccessException {
        Object object = extractValue(entity);

        if (object instanceof String) {
            return "'" + object + "'";
        } else if (object instanceof Integer) {
            return String.valueOf(object);
        } else if (object instanceof Long) {
            return String.valueOf(object);
        }

        return null;
    }

    public String getColumnName() {
        if (field.isAnnotationPresent(Column.class)
                && !isAnnotationNameEmpty(field)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }

        return transformColumnName(field.getName());
    }

    public boolean isId() {
        return field.isAnnotationPresent(Id.class);
    }

    public <T> void setInstanceValue(T instance, ResultSet resultSet) throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        Object value = resultSet.getObject(getColumnName());
        field.set(instance, value);
    }

    private boolean isAnnotationNameEmpty(Field field) {
        return field.getAnnotation(Column.class).name().equals("");
    }

    private String getColumnOptionValue(Dialect dialect) {
        return ColumnOptionFactory.createColumnOption(field, dialect);
    }

    public String getUpdateClause(Object entity, Object snapshot) {
        try {
            Object entityValue = extractValue(entity);
            Object snapshotValue = extractValue(snapshot);

            if (entityValue == null || !entityValue.equals(snapshotValue)) {
                return "";
            }

            if (entityValue instanceof String) {
                return getColumnName() + " = '" + entityValue + "'";
            } else if (entityValue instanceof Integer) {
                return getColumnName() + " = " + entityValue;
            } else if (entityValue instanceof Long) {
                return getColumnName() + " = " + entityValue;
            }

            return "";
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return "";
    }

    public boolean hasDifferentValue(Object entity, Object snapshot) {
        try {
            Object entityValue = extractValue(entity);
            Object snapshotValue = extractValue(snapshot);

            if (entityValue == null || !entityValue.equals(snapshotValue)) {
                return true;
            }

            return false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasJoinAnnotation() {
        return Arrays.stream(field.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("OneToMany"));
    }

    public boolean hasFetchJoinAnnotation() {
        if (hasJoinAnnotation()) {
            return field.getAnnotation(OneToMany.class).fetch().equals(FetchType.EAGER);
        }

        return false;
    }

    public boolean hasJoinColumnAnnotation() {
        return Arrays.stream(field.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("JoinColumn"));
    }

    public Class<?> getJoinTable() {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }

        return (Class<?>) field.getGenericType();
    }

    public String getJoinColumnName() {
        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.name();
        }

        return field.getName();
    }

    public boolean isSameFetchType(FetchType fetchType) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            OneToMany annotation = field.getAnnotation(OneToMany.class);
            return annotation.fetch().equals(fetchType);
        }

        return false;
    }

    public void setJoinEntity(Object entity, Object joinEntity) {
        try {
            Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType) {
                field.setAccessible(true);
                List<Object> newList = (List<Object>) field.get(entity);

                if (newList == null) {
                    newList = new ArrayList<>();
                }
                newList.add(joinEntity);
                field.setAccessible(true);
                field.set(entity, newList);
                return;
            }
            field.setAccessible(true);
            field.set(entity, joinEntity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object extractValue(Object entity) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        return field.get(entity);
    }

    private String transformColumnName(String originalName) {
        return originalName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
