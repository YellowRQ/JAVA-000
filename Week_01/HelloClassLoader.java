/**
 * ClassName:Hello
 * Package:PACKAGE_NAME
 * Description:
 *  基本类型，四则运行，if 和 for，然后自己分析一下对应的字节码
 * @author:yellowrq
 * @date: 2020/10/19 16:30
 */
public class Hello {

    public static void main(String[] args) {
        int i1 = 10;
        int i2 = 1;
        double d1 = 2.5;
        for (int i = 0; i < 5; i++) {
            i1 = i1 - 1;
            i2 = i2 * 2;
            if (i2 > i1) {
                d1 = i2 / d1;
            }
        }
    }
}
