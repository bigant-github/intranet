package priv.bigant.intranet.client;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start1 {

    public static void main(String[] args) {
        //new Home().showHome();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(Start1.class.getResource("/conf.properties").getPath());
            //inputStream = new FileInputStream(new File(userPath + "/conf.properties"));
            Properties properties = new Properties();
            properties.load(inputStream);
            String hostName = properties.getProperty("hostName");
            String localPort = properties.getProperty("localPort");
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setHostName(hostName);
            clientConfig.setDomainName(hostName);
            clientConfig.setLocalPort(Integer.valueOf(localPort));
            System.out.println("请求穿透域名" + hostName + "本地端口" + localPort);
            new ClientServe(clientConfig).start();
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
