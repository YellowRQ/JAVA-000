package org.example.spring.homework1.jdkProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ClassName:MyInvocationHandler
 * Package:org.example.spring.homework1
 * Description:
 * 1.（选做） Java 里的动态代理，实现一个简单的 AOP。
 * @author:YellowRQ
 * @data:2020/11/18 3:55
 */
public class MyInvocationHandler implements InvocationHandler {

    private Object target;

    public MyInvocationHandler(){}

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("method invoke start...");
        String result = (String) method.invoke(target, args);
        result = result.toUpperCase();
        System.out.println("method invoke end...");
        return result;
    }
}
