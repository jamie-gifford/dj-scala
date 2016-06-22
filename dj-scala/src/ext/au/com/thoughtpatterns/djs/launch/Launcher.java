package au.com.thoughtpatterns.djs.launch;

import com.jdotsoft.jarloader.JarClassLoader;


public class Launcher {

    public static void main(String[] args) {
        JarClassLoader jcl = new JarClassLoader();
        try {
            jcl.invokeMain("au.com.thoughtpatterns.djs.app.App", args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    } // main()
    
}