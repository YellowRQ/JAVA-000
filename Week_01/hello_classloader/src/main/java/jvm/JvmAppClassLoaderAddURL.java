package jvm;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassName:JvmAppClassLoaderAddURL
 * Package:jvm
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/10/20 23:38
 */
public class JvmAppClassLoaderAddURL {

    public static void main(String[] args) {

        String appPath = "";
        URLClassLoader urlClassLoader = (URLClassLoader) JvmAppClassLoaderAddURL.class.getClassLoader();
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            URL url = new URL(appPath);
            addURL.invoke(urlClassLoader, url);
            //效果跟Class.forName("jvm.Hello").newInstance一样
            Class.forName("jvm.Hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
