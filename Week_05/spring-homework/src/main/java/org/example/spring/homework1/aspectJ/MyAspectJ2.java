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
public class MyAspectJ2 {

    public void before() {
        System.out.println("==============MyAspectJ2 before==============");
    }

    public void after() {
        System.out.println("==============MyAspectJ2 after==============");
    }

    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("==============MyAspectJ2 around start==============");
        joinPoint.proceed();
        System.out.println("==============MyAspectJ2 around end==============");
    }
}
