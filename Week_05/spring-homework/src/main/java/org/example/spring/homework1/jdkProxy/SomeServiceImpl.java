package org.example.spring.homework1.jdkProxy;

/**
 * ClassName:SomeServiceImpl
 * Package:org.example.spring.homework1
 * Description:
 * 1.（选做） Java 里的动态代理，实现一个简单的 AOP。
 * @author:YellowRQ
 * @data:2020/11/18 3:52
 */
public class SomeServiceImpl implements SomeService {

    @Override
    public String doSomething() {
        System.out.println("执行doSomething...");
        return "do something";
    }
}
