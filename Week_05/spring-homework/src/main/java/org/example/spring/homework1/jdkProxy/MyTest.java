package org.example.spring.homework1.jdkProxy;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * ClassName:MyTest
 * Package:org.example.spring.homework1
 * Description:
 * 1.（选做） Java 里的动态代理，实现一个简单的 AOP。
 * @author:YellowRQ
 * @data:2020/11/18 4:01
 */
public class MyTest {

    @Test
    public void test1() {
        SomeService target = new SomeServiceImpl();
        InvocationHandler myInvocationHandler = new MyInvocationHandler(target);
        SomeService proxy = (SomeService) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                myInvocationHandler);
        String s = proxy.doSomething();
        System.out.println("增强结果：" + s);
        System.out.println("proxy类型：" + proxy.getClass().getName());
    }
}
