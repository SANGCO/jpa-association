package persistence.sql.ddl;

import domain.Person;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTableTest {

    @Test
    @DisplayName("@Table 어노테이션 있는 경우")
    public void tableAnnotation() {
        EntityTable tableInfo = new EntityTable(Person.class);

        assertThat(tableInfo.getTableName()).isEqualTo("users");
    }

    @Test
    @DisplayName("@Table 어노테이션 있는데 name이 빈 값인 경우")
    public void tableAnnotationNameEmptyString() {
        EntityTable tableInfo = new EntityTable(TableAnnotationNameEmpty.class);

        assertThat(tableInfo.getTableName()).isEqualTo("TableAnnotationNameEmpty");
    }

    @Table(name = "")
    private class TableAnnotationNameEmpty {
    }

}
