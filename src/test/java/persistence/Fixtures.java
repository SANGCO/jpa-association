package persistence;

import domain.Order;
import domain.OrderItem;
import domain.Person;

public class Fixtures {

    public static Person person1() {
        return new Person("test1", 30, "test1@gmail.com");
    }

    public static Person person2() {
        return new Person("test2", 30, "test2@gmail.com");
    }


    public static Order order1() {
        return new Order("testOrder1");
    }

    public static OrderItem orderItem1() {
        return new OrderItem("testProduct1", 1);
    }

    public static OrderItem orderItem2() {
        return new OrderItem("testProduct2", 2);
    }

}
