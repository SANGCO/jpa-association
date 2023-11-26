package persistence.sql.dml;

import domain.Order;
import domain.Person;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.Fixtures;
import persistence.sql.ddl.EntityMetadata;

import static org.assertj.core.api.Assertions.assertThat;

class EntityManipulationBuilderTest {

    private static EntityMetadata entityMetadata;

    @BeforeAll
    static void setUp() {
        entityMetadata = EntityMetadata.of(Person.class);
    }

    @Test
    @DisplayName("Person 엔터티 insert 쿼리 만들기")
    public void insertQueryTest() {
        Person person = Fixtures.person1();
        String query = new EntityManipulationBuilder().insert(person, entityMetadata);

        assertThat(query).isEqualTo("INSERT INTO users (nick_name, old, email) " +
                "VALUES ('test1', 30, 'test1@gmail.com');");
    }

    @Test
    @DisplayName("Person 엔터티 findAll 쿼리 만들기")
    public void findAllQueryTest() {
        String query = new EntityManipulationBuilder().findAll(entityMetadata);

        assertThat(query).isEqualTo(
        "SELECT " +
                    "users.id, users.nick_name, users.old, users.email " +
                "FROM users;"
        );
    }

    @Test
    @DisplayName("Person 엔터티 findById 쿼리 만들기")
    public void findByIdQueryTest() {
        long id = 1L;
        String query = new EntityManipulationBuilder().findById(id, entityMetadata);

        assertThat(query).isEqualTo(
        "SELECT " +
                    "users.id, users.nick_name, users.old, users.email " +
                "FROM users WHERE users.id = " + id + ";"
        );
    }

    @Test
    @DisplayName("Person 엔터티 delete 쿼리 만들기")
    public void deleteQueryTest() {
        String id = "1";
        String query = new EntityManipulationBuilder().delete(id, entityMetadata);

        assertThat(query).isEqualTo("DELETE FROM users WHERE users.id = " + id + ";");
    }

    @Test
    @DisplayName("Order 엔터티 findAll join 쿼리 만들기")
    public void joinQueryTest() {
        String query = new EntityManipulationBuilder().findAll(EntityMetadata.of(Order.class));

        assertThat(query).isEqualTo(
        "SELECT " +
                    "orders.id, orders.order_number, " +
                    "order_items.id, order_items.product, order_items.quantity, order_items.order_id " +
                "FROM orders " +
                "JOIN order_items ON orders.id = order_items.order_id;");
    }

    @Test
    @DisplayName("Order 엔터티 findById join 쿼리 만들기")
    public void findByIdJoinQueryTest() {
        String query = new EntityManipulationBuilder().findById(1L, EntityMetadata.of(Order.class));

        assertThat(query).isEqualTo(
        "SELECT " +
                    "orders.id, orders.order_number, " +
                    "order_items.id, order_items.product, order_items.quantity, order_items.order_id " +
                "FROM orders " +
                "JOIN order_items ON orders.id = order_items.order_id " +
                "WHERE orders.id = 1;"
        );
    }

}
