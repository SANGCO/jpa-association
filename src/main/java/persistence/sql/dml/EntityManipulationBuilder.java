package persistence.sql.dml;

import persistence.sql.ddl.EntityMetadata;
import utils.CustomStringBuilder;

import java.util.List;

import static utils.CustomStringBuilder.*;

public class EntityManipulationBuilder {

    public String findAll(EntityMetadata entityMetadata) {
        if (entityMetadata.haveJoinTables()) {
            return join(entityMetadata);
        }

        return toFindAllStatement(entityMetadata.getTableName(), entityMetadata.getColumnNames());
    }

    private String join(EntityMetadata entityMetadata) {
        List<EntityMetadata> joinTables = entityMetadata.getJoinTables();
        StringBuilder columnNames = new StringBuilder();
        columnNames.append(entityMetadata.getColumnNames());
        columnNames.append(", ");

        joinTables.forEach(joinEntityMetadata -> {
            columnNames.append(joinEntityMetadata.getColumnNames());
            columnNames.append(", ");
            columnNames.append(joinEntityMetadata.getJoinColumnName(entityMetadata));
            columnNames.append(", ");
        });
        columnNames.delete(columnNames.length() - 2, columnNames.length());

        StringBuilder result = new StringBuilder();
        result.append(toFindAllStatement(entityMetadata.getTableName(), columnNames.toString()));
        result.delete(result.length() - 1, result.length());

        joinTables.forEach(joinEntityMetadata -> {
            result.append(" ");
            result.append(toJoinClause(
                    joinEntityMetadata.getTableName(),
                    entityMetadata.getIdColumnName(),
                    joinEntityMetadata.getJoinColumnName(entityMetadata)
            ));
        });
        result.delete(result.length() - 1, result.length());

        return result.toString();
    }

    public String findById(long id, EntityMetadata entityMetadata) {
        String tableName = entityMetadata.getTableName();

        return toFindByIdStatement(
                tableName,
                entityMetadata.getColumnNames(),
                entityMetadata.getIdColumnName(),
                String.valueOf(id)
        );
    }

    public String insert(Object entity, EntityMetadata entityMetadata) {
        return new CustomStringBuilder()
                .append(columnsClause(entity, entityMetadata))
                .append(valuesClause(entity, entityMetadata))
                .toString();
    }

    public String update(Object entity, Object snapshot, EntityMetadata entityMetadata) {
        String updateClause = entityMetadata.getUpdateClause(entity, snapshot);
        if (updateClause.isEmpty()) {
            return "";
        }

        return toUpdateStatement(
                entityMetadata.getTableName(),
                entityMetadata.getIdColumnName(),
                entityMetadata.getIdColumnValue(entity),
                updateClause
        );
    }

    public String delete(String id, EntityMetadata entityMetadata) {
        return toDeleteStatement(
                entityMetadata.getTableName(),
                entityMetadata.getIdColumnName(),
                id);
    }

    private String columnsClause(Object entity, EntityMetadata entityMetadata) {
        return toInsertColumnsClause(entityMetadata.getTableName(), entityMetadata.getColumnNames(entity));
    }

    private String valuesClause(Object entity, EntityMetadata entityMetadata) {
        return toInsertValuesClause(entityMetadata.getValueFrom(entity));
    }

}
