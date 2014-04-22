package launcher;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;

public class TomcatLauncher {

    public static void main(String[] args) throws ServletException, LifecycleException {

        //edit this webapp dir
        String webappDir = "../../src/main/webapp";
        Tomcat tomcat = new Tomcat();

        String webPort = System.getenv("PORT");
        if(webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        tomcat.setPort(8568);

        //change this to work form  any dir.look for more examples
        tomcat.addWebapp("/buddhi", new File(webappDir).getAbsolutePath());
        //System.out.println("configuring app with basedir: " + new File( webappDir).getAbsolutePath());

        tomcat.start();
        tomcat.getServer().await();
    }
}
