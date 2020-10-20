package jvm;

/**
 * ClassName:Hello
 * Package:jvm
 * Description:
 * 需要被加载的Hello类
 * @author:YellowRQ
 * @data:2020/10/20 23:08
 */
public class Hello {

    static {
        System.out.println("Hello Class Initialized");
    }
}
