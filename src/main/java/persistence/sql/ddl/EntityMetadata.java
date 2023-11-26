package persistence.sql.ddl;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import persistence.sql.ddl.dialect.Dialect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

public class EntityMetadata {

    private final TableMetadataExtractor tableMetaDataExtractor;
    private final FieldMetadataExtractors fieldMetadataExtractors;
    final Class<?> type;

    public EntityMetadata(Class<?> type) {
        if (!type.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("No @Entity annotation");
        }

        tableMetaDataExtractor = new TableMetadataExtractor(type);
        fieldMetadataExtractors = new FieldMetadataExtractors(type);
        this.type = type;
    }

    public static EntityMetadata of(Class<?> type) {
        return new EntityMetadata(type);
    }

    public String getTableName() {
        return tableMetaDataExtractor.getTableName();
    }

    public String getColumnInfo(Dialect dialect) {
        return fieldMetadataExtractors.getDefinition(dialect);
    }

    public String getColumnNames(Object entity) {
        return fieldMetadataExtractors.getColumnNames(entity);
    }

    public String getColumnNames() {
        return fieldMetadataExtractors.getColumnNames(getTableName());
    }

    public String getValueFrom(Object entity) {
        return fieldMetadataExtractors.getValueFrom(entity);
    }

    public String getIdColumnName() {
        return fieldMetadataExtractors.getIdColumnName(getTableName());
    }

    public String getIdColumnValue(Object entity) {
        return fieldMetadataExtractors.getIdColumnValue(entity);
    }

    public <T> T getEntity(ResultSet resultSet) {
        try {
            Constructor<T> constructor = (Constructor<T>) type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            fieldMetadataExtractors.setInstanceValue(instance, resultSet);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("엔티티 객체를 생성하는데 오류가 발생하였습니다.", e);
        }
    }

    public <T> void setIdToEntity(T entity, long id) {
        try {
            String idColumnName = fieldMetadataExtractors.getIdColumnName();
            Field declaredField = type.getDeclaredField(idColumnName);
            declaredField.setAccessible(true);
            declaredField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Entity 객체에 ID 값을 세팅 중 오류 발생", e);
        }
    }

    public String getUpdateClause(Object entity, Object snapshot) {
        return fieldMetadataExtractors.getUpdateClause(entity, snapshot);
    }

    public boolean hasDifferentValue(Object entity, Object snapshot) {
        return fieldMetadataExtractors.hasDifferentValue(entity, snapshot);
    }

    public boolean hasFetchJoin() {
        return fieldMetadataExtractors.haveFetchJoinAnnotations();
    }

    public List<EntityMetadata> getJoinTables(FetchType fetchType) {
        return fieldMetadataExtractors.getJoinTables(fetchType).stream()
                .map(EntityMetadata::of)
                .collect(Collectors.toList());
    }

    public String getJoinColumnName(EntityMetadata entityMetadata) {
        return entityMetadata.getJoinColumnName(type, getTableName());
    }

    public void setJoinEntity(Object entity, EntityMetadata fetchJoin, Object joinEntity) {
        fieldMetadataExtractors.setJoinEntity(entity, fetchJoin.getType(), joinEntity);
    }

    public Class<?> getType() {
        return type;
    }

    private String getJoinColumnName(Class<?> joinTableType, String tableAlias) {
        return fieldMetadataExtractors.getJoinColumnName(type, joinTableType, tableAlias);
    }

}
