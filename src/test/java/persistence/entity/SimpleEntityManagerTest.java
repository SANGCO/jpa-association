package persistence.entity;

import domain.Order;
import domain.OrderItem;
import domain.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.DatabaseTestBase;
import persistence.Fixtures;
import persistence.sql.ddl.EntityDefinitionBuilder;
import persistence.sql.ddl.EntityMetadata;
import persistence.sql.ddl.dialect.Dialect;
import persistence.sql.ddl.dialect.H2Dialect;
import persistence.sql.dml.EntityManipulationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SimpleEntityManagerTest extends DatabaseTestBase {

    @Test
    @DisplayName("find() 메서드 테스트")
    void find() {
        Person person = entityManager.find(Person.class, 1L);

        assertThat(person.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("find() 메서드 조인 테스트")
    void findJoin() {
        Dialect dialect = new H2Dialect();
        EntityMetadata orderMetadata = EntityMetadata.of(Order.class);
        EntityDefinitionBuilder orderBuilder = new EntityDefinitionBuilder(orderMetadata);
        jdbcTemplate.execute(orderBuilder.create(dialect));
        jdbcTemplate.execute(new EntityManipulationBuilder()
                .insert(Fixtures.order1(), orderMetadata)
        );

        EntityMetadata orderItemMetadata = EntityMetadata.of(OrderItem.class);
        EntityDefinitionBuilder orderItemBuilder = new EntityDefinitionBuilder(orderItemMetadata);
        jdbcTemplate.execute("CREATE TABLE order_items (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "order_id BIGINT," +
                "product VARCHAR," +
                "quantity INT);");
        jdbcTemplate.execute("INSERT INTO order_items (order_id, product, quantity) VALUES (1, 'testProduct1', 1)");
        jdbcTemplate.execute("INSERT INTO order_items (order_id, product, quantity) VALUES (1, 'testProduct2', 2)");

        Order order = entityManager.find(Order.class, 1L);
        // TODO order 객체에 addOrderItem() 메서드로 oderItem 객체 추가 -> order 저장 -> orderItem 인서트문

        assertAll(
                () -> assertThat(order.getId()).isEqualTo(1L),
                () -> assertThat(order.getOrderItems().size()).isEqualTo(2)
        );

        jdbcTemplate.execute(orderBuilder.drop());
        jdbcTemplate.execute(orderItemBuilder.drop());
    }

    @Test
    @DisplayName("persist() 메서드 테스트")
    void persist() {
        Person person = Fixtures.person2();

        Person persist = entityManager.persist(person);

        assertAll(
                () -> assertThat(person.getName()).isEqualTo(persist.getName()),
                () -> assertThat(person.getId()).isEqualTo(2L)
        );
    }

    @Test
    @DisplayName("remove() 메서드 테스트")
    void remove() {
        Person person = entityManager.find(Person.class, 1L);
        entityManager.remove(person);

        assertThatThrownBy(() -> entityManager.find(Person.class, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("엔티티가 없습니다.");
    }

    @Test
    @DisplayName("remove() 메서드 @Id Exception 테스트")
    void removeIdNotFoundException() {
        TestPerson person = new TestPerson();

        assertThatThrownBy(() -> entityManager.remove(person))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No @Entity annotation");
    }

    @Test
    @DisplayName("merge() 메서드 테스트")
    void merge() {
        Person person = entityManager.find(Person.class, 1L);
        person.setName("new name");

        Person mergedPerson = entityManager.merge(person);

        assertAll(
                () -> assertThat(person.getName()).isEqualTo(mergedPerson.getName()),
                () -> assertThat(persistenceContext.getEntityStatus(person)).isEqualTo(Status.MANAGED)
        );
    }

    @Test
    @DisplayName("merge() 메서드 수정 사항이 없는 케이스 테스트")
    void mergeNoUpdate() {
        Person person = entityManager.find(Person.class, 1L);

        Person mergedPerson = entityManager.merge(person);

        assertAll(
                () -> assertThat(person.getName()).isEqualTo(mergedPerson.getName()),
                () -> assertThat(persistenceContext.getEntityStatus(person)).isEqualTo(Status.MANAGED)
        );
    }

    @Test
    @DisplayName("getIdValue() 메서드 테스트")
    void getIdValue() throws NoSuchMethodException, NoSuchFieldException {
        Person person = entityManager.find(Person.class, 1L);
        String methodName = "getIdValue";
        Class<?>[] parameterTypes = {Object.class, Field.class};
        Method method = SimpleEntityManager.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Field field = Person.class.getDeclaredField("id");
        field.setAccessible(false);
        SimpleEntityManager simpleEntityManager = new SimpleEntityManager(null, null, null);

        try {
            method.invoke(simpleEntityManager, person, field);
        } catch (InvocationTargetException e) {
            Throwable actualException = e.getTargetException();
            assertAll(
                    () -> assertThat(actualException instanceof RuntimeException).isEqualTo(true),
                    () -> assertThat("Field 값을 읽어오는데 실패했습니다.").isEqualTo(actualException.getMessage())
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private class TestPerson {

        private Long id;

    }

}
