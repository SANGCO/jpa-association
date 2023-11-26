package persistence.sql.ddl;

import domain.Order;
import domain.OrderItem;
import domain.Person;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.Fixtures;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class EntityMetadataTest {

    @Test
    @DisplayName("@Entity 없으면 Exception")
    public void noEntityAnnotation() {

        assertThatThrownBy(() -> {
            new EntityMetadata(NoEntityAnnotation.class);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No @Entity annotation");
    }

    @Test
    @DisplayName("setIdToEntity() 메서드 테스트")
    public void setIdToEntity() {
        EntityMetadata entityMetadata = new EntityMetadata(Person.class);
        Person person = Fixtures.person1();

        entityMetadata.setIdToEntity(person, 1L);

        assertThat(person.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("setIdToEntity() 메서드 Exception 테스트")
    public void setIdToEntityException() {
        EntityMetadata entityMetadata = new EntityMetadata(EntityAnnotation.class);
        EntityAnnotation entityAnnotation = new EntityAnnotation();

        assertThatThrownBy(() -> {
            entityMetadata.setIdToEntity(entityAnnotation, 1L);
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("Entity 객체에 ID 값을 세팅 중 오류 발생");
    }

    @Test
    @DisplayName("haveJoinTables() 메서드 테스트")
    public void haveJoinTables() {
        EntityMetadata entityMetadata = new EntityMetadata(Person.class);
        EntityMetadata joinEntityMetadata = new EntityMetadata(Order.class);

        assertAll(
                () -> assertThat(entityMetadata.hasFetchJoin()).isFalse(),
                () -> assertThat(joinEntityMetadata.hasFetchJoin()).isTrue()
        );
    }

    @Test
    @DisplayName("getJoinTables() 메서드 테스트")
    public void getJoinTables() {
        EntityMetadata joinEntityMetadata = new EntityMetadata(Order.class);

        List<EntityMetadata> joinTables = joinEntityMetadata.getJoinTables(FetchType.EAGER);
        Class<?> joinTable = joinTables.stream()
                .map(entityMetadata -> entityMetadata.type)
                .filter(type -> type.equals(OrderItem.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No join table"));

        assertThat(joinTable).isEqualTo(OrderItem.class);
    }

    @Test
    @DisplayName("getJoinColumnName() 메서드 테스트")
    public void getJoinColumnName() {
        EntityMetadata entityMetadata = new EntityMetadata(Order.class);
        EntityMetadata joinEntityMetadata = new EntityMetadata(OrderItem.class);

        String joinColumnName = joinEntityMetadata.getJoinColumnName(entityMetadata);

        assertThat(joinColumnName).isEqualTo("order_items.order_id");
    }

    private class NoEntityAnnotation {
    }

    @Entity
    private class EntityAnnotation {

        @Id
        private Integer id;

    }

}
