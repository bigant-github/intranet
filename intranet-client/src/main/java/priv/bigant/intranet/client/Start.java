package priv.bigant.intranet.client;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    public static void main(String[] args) {
        //new Home().showHome();
        String userPath = System.getProperty("user.dir");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(Start.class.getResource("/conf.properties").getPath());
            //inputStream = new FileInputStream(new File(userPath + "/conf.properties"));
            Properties properties = new Properties();
            properties.load(inputStream);
            String hostName = properties.getProperty("hostName");
            String port = properties.getProperty("port");
            System.out.println("请求穿透域名" + hostName + "本地端口" + port);
            new ThroughThread(hostName, 45678, hostName, new Integer(port)).start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
