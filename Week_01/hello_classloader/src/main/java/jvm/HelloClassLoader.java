package jvm;

import java.util.Base64;

/**
 * ClassName:jvm.HelloClassLoader
 * Package:jvm
 * Description:
    自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，
    此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。文件群里提供。
 * @author:YellowRQ
 * @data:2020/10/20 23:04
 */
public class HelloClassLoader extends ClassLoader{

    public static void main(String[] args) {
        try {
            //加载并初始化Hello类
            new HelloClassLoader().findClass("jvm.Hello").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        String helloBase64 = "yv66vgAAADQAHAoABgAOCQAPABAIABEKABIAEwcAFAcAFQEABjxpbml0PgEAAygpVgEABENvZGUB" +
                "AA9MaW5lTnVtYmVyVGFibGUBAAg8Y2xpbml0PgEAClNvdXJjZUZpbGUBAApIZWxsby5qYXZhDAAH" +
                "AAgHABYMABcAGAEAF0hlbGxvIENsYXNzIEluaXRpYWxpemVkBwAZDAAaABsBAAlqdm0vSGVsbG8B" +
                "ABBqYXZhL2xhbmcvT2JqZWN0AQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1By" +
                "aW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAVKExqYXZhL2xhbmcv" +
                "U3RyaW5nOylWACEABQAGAAAAAAACAAEABwAIAAEACQAAAB0AAQABAAAABSq3AAGxAAAAAQAKAAAA" +
                "BgABAAAACwAIAAsACAABAAkAAAAlAAIAAAAAAAmyAAISA7YABLEAAAABAAoAAAAKAAIAAAAOAAgA" +
                "DwABAAwAAAACAA0=";
        byte[] bytes = decode(helloBase64);
        return defineClass(name,bytes,0,bytes.length);
    }

    public byte[] decode (String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
