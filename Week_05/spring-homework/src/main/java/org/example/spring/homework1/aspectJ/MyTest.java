package org.example.spring.homework1.aspectJ;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ClassName:MyTest
 * Package:org.example.spring.homework1.aspectJ
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 4:43
 */
public class MyTest {

    ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

    @Test
    public void test() {
        Student student001 = (Student) context.getBean("student001");
        System.out.println(student001.toString() + "\n");
        Klass class1 = (Klass) context.getBean("class1");
        System.out.println(class1.toString() + "\n");
        System.out.println("context.getBeanDefinitionNames() ===>> \n"+
                String.join("\n", context.getBeanDefinitionNames()));

    }

    @Test
    public void cglibTest() {
        Klass class1 = context.getBean(Klass.class);
        class1.study();
        System.out.println("Klass对象AOP代理后的实际类型："+class1.getClass());
        System.out.println("Klass对象AOP代理后的实际类型是否是Klass子类："+(class1 instanceof Klass));
    }

    @Test
    public void jdkTest() {
        ISchool school = context.getBean(ISchool.class);
        System.out.println(school);
        school.ding();
        System.out.println("ISchool接口的对象AOP代理后的实际类型："+school.getClass());
    }

}
