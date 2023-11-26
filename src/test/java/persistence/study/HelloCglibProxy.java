package persistence.study;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class HelloCglibProxy implements MethodInterceptor {

    private final Object target;

    public HelloCglibProxy(final Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String result = (String) method.invoke(target, args);
        return result.toUpperCase();
    }

}
