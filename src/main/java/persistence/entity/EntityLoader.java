package persistence.entity;

import jakarta.persistence.FetchType;
import jdbc.JdbcTemplate;
import persistence.sql.ddl.EntityMetadata;
import persistence.sql.dml.EntityManipulationBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityLoader {

    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> T findById(Class<T> clazz, Long id) {
        EntityMetadata entityMetadata = EntityMetadata.of(clazz);
        Object result = jdbcTemplate.queryForObject(
                new EntityManipulationBuilder().findById(id, entityMetadata),
                resultSet -> getEntity(resultSet, entityMetadata)
        );

        return (T) result;
    }

    private <T> T getEntity(ResultSet resultSet, EntityMetadata entityMetadata) {
        Object entity = null;

        try {
            while (resultSet.next()) {

                if (entity == null) {
                    entity = entityMetadata.getEntity(resultSet);
                }

                if (entityMetadata.hasFetchJoin()) {
                    Object finalEntity = entity;
                    entityMetadata.getJoinTables(FetchType.EAGER).forEach(fetchJoin -> {

                        Object joinEntity = fetchJoin.getEntity(resultSet);
                        entityMetadata.setJoinEntity(finalEntity, fetchJoin, joinEntity);
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return (T) entity;
    }

}
