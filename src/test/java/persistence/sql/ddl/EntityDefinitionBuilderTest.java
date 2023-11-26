package persistence.sql.ddl;

import domain.Order;
import domain.OrderItem;
import domain.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.dialect.Dialect;
import persistence.sql.ddl.dialect.H2Dialect;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityDefinitionBuilderTest {

    @Test
    @DisplayName("Person 엔터티 create 쿼리 만들기")
    public void createQueryTest() {
        EntityMetadata entityMetadata = EntityMetadata.of(Person.class);
        EntityDefinitionBuilder entityDefinitionBuilder = new EntityDefinitionBuilder(entityMetadata);
        Dialect dialect = new H2Dialect();
        String query = entityDefinitionBuilder.create(dialect);

        assertThat(query).isEqualTo("CREATE TABLE users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "nick_name VARCHAR," +
                "old INT," +
                "email VARCHAR NOT NULL);");
    }

    @Test
    @DisplayName("Person 엔터티 drop쿼리 만들기")
    public void dropQueryTest() {
        EntityMetadata entityMetadata = EntityMetadata.of(Person.class);
        EntityDefinitionBuilder entityDefinitionBuilder = new EntityDefinitionBuilder(entityMetadata);
        String query = entityDefinitionBuilder.drop();

        assertThat(query).isEqualTo("DROP TABLE users;");
    }

    @Test
    @DisplayName("Order 엔터티 create 쿼리 만들기")
    public void orderCreateQueryTest() {
        EntityMetadata entityMetadata = EntityMetadata.of(Order.class);
        EntityDefinitionBuilder entityDefinitionBuilder = new EntityDefinitionBuilder(entityMetadata);
        Dialect dialect = new H2Dialect();
        String query = entityDefinitionBuilder.create(dialect);

        assertThat(query).isEqualTo("CREATE TABLE orders (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "order_number VARCHAR);");
    }

    @Test
    @DisplayName("OrderItem 엔터티 create 쿼리 만들기")
    public void OrderItemCreateQueryTest() {
        EntityMetadata entityMetadata = EntityMetadata.of(OrderItem.class);
        EntityDefinitionBuilder entityDefinitionBuilder = new EntityDefinitionBuilder(entityMetadata);
        Dialect dialect = new H2Dialect();
        String query = entityDefinitionBuilder.create(dialect);

        assertThat(query).isEqualTo("CREATE TABLE order_items (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "product VARCHAR," +
                "quantity INT);");
    }

}
