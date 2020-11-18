package org.example.spring.homework1.aspectJ;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * ClassName:MyAspectJ1
 * Package:org.example.spring.homework1.aspectJ
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 4:37
 */
@Aspect
public class MyAspectJ1 {

    @Pointcut(value = "execution(* org.example.spring.homework1.aspectJ.Klass.study(..))")
    public void point(){}

    @Before(value = "point()")
    public void before() {
        System.out.println("============== MyAspectJ1 before==============");
    }

    @After(value = "point()")
    public void after() {
        System.out.println("============== MyAspectJ1 after==============");
    }

    @Around(value = "point()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("============== MyAspectJ1 around start==============");
        joinPoint.proceed();
        System.out.println("============== MyAspectJ1 around end==============");
    }
}
