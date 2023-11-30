package persistence.sql.ddl;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import persistence.sql.ddl.dialect.Dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityFields {

    private final List<EntityField> entityFieldList;

    public EntityFields(Class<?> type) {
        entityFieldList = Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Transient.class))
                .map(EntityField::new)
                .collect(Collectors.toList());
    }

    public String getDefinition(Dialect dialect) {
        return entityFieldList.stream()
                .filter(entityField -> !entityField.hasJoinAnnotation())
                .map(entityField -> entityField.getDefinition(dialect))
                .collect(Collectors.joining(","));
    }

    public String getColumnNames(Object entity) {
        return entityFieldList.stream()
                .filter(entityField -> !entityField.hasJoinAnnotation())
                .map(entityField -> {
                    try {
                        return entityField.getColumnName(entity);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return "";
                })
                .filter(columnName -> !columnName.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public String getColumnNames(String tableAlias) {
        return entityFieldList.stream()
                .filter(entityField -> !entityField.hasJoinAnnotation())
                .map(entityField -> {
                    return tableAlias + "." + entityField.getColumnName();
                })
                .collect(Collectors.joining(", "));
    }

    public String getValueFrom(Object entity) {
        return entityFieldList.stream()
                .filter(entityField -> !entityField.hasJoinAnnotation())
                .map(entityField -> {
                    try {
                        return entityField.getValueFrom(entity);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    public String getIdColumnName() {
        return entityFieldList.stream()
                .filter(EntityField::isId)
                .map(EntityField::getColumnName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No @Id annotation"));
    }

    public String getIdColumnName(String tableAlias) {
        return entityFieldList.stream()
                .filter(EntityField::isId)
                .map(entityField -> {
                    return tableAlias + "." + entityField.getColumnName();
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No @Id annotation"));
    }

    public String getIdColumnValue(Object entity) {
        String idColumnName = getIdColumnName();
        return entityFieldList.stream()
                .filter(EntityField -> EntityField.getColumnName().equals(idColumnName))
                .map(entityField -> {
                    try {
                        return entityField.getValueFrom(entity);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ID 컬럼 값이 없습니다."));
    }

    public <T> void setInstanceValue(T instance, ResultSet resultSet) throws SQLException, IllegalAccessException {
        for (EntityField entityField : entityFieldList) {
            if (entityField.hasJoinAnnotation()) {
                continue;
            }

            entityField.setInstanceValue(instance, resultSet);
        }
    }

    public String getUpdateClause(Object entity, Object snapshot) {
        return entityFieldList.stream()
                .map(entityField -> {
                    return entityField.getUpdateClause(entity, snapshot);
                })
                .filter(String::isEmpty)
                .collect(Collectors.joining(", "));
    }

    public boolean hasDifferentValue(Object entity, Object snapshot) {
        return entityFieldList.stream()
                .anyMatch(entityField -> {
                    return entityField.hasDifferentValue(entity, snapshot);
                });
    }

    public boolean haveFetchJoinAnnotations() {
        return entityFieldList.stream()
                .anyMatch(EntityField::hasFetchJoinAnnotation);
    }

    public List<Class<?>> getJoinTables(FetchType fetchType) {
        return entityFieldList.stream()
                .filter(EntityField::hasJoinAnnotation)
                .filter(entityField -> entityField.isSameFetchType(fetchType))
                .map(EntityField::getJoinTable)
                .collect(Collectors.toList());
    }

    public String getJoinColumnName(Class<?> type, Class<?> joinTableType, String tableAlias) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .map(EntityField::new)
                .filter(EntityField::hasJoinColumnAnnotation)
                .filter(entityField -> entityField.getJoinTable().equals(joinTableType))
                .map(entityField -> {
                    return tableAlias + "." + entityField.getJoinColumnName();
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No join column name"));
    }

    public void setJoinEntity(Object entity, Class<?> type, Object joinEntity) {
        entityFieldList.stream()
                .filter(EntityField::hasJoinColumnAnnotation)
                .filter(entityField -> entityField.getJoinTable().equals(type))
                .forEach(entityField -> {
                    entityField.setJoinEntity(entity, joinEntity);
                });
    }

}
