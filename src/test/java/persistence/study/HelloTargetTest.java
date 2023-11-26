package persistence.study;

import net.sf.cglib.proxy.Enhancer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloTargetTest {

    @Test
    public void test() {
        HelloTarget helloTarget = new HelloTarget();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(helloTarget.getClass());
        enhancer.setCallback(new HelloCglibProxy(helloTarget));

        HelloTarget helloProxy = (HelloTarget) enhancer.create();

        assertThat(helloProxy.sayHello("Toby")).isEqualTo("HELLO TOBY");
        assertThat(helloProxy.sayHi("Toby")).isEqualTo("HI TOBY");
        assertThat(helloProxy.sayThankYou("Toby")).isEqualTo("THANK YOU TOBY");
    }

}