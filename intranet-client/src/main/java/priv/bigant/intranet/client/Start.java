package priv.bigant.intranet.client;


import com.sun.org.apache.bcel.internal.generic.NEW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    private final static Logger LOGGER = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        boolean b = createdConfig();
        if (b)
            new ClientCommunication().start();
    }

    public static boolean createdConfig() {
        //new Home().showHome();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("/tmp/conf.properties"));
            //inputStream = new FileInputStream(Start.class.getResource("/conf.properties").getPath());
            //inputStream = new FileInputStream(new File(userPath + "/conf.properties"));
            Properties properties = new Properties();
            properties.load(inputStream);
            String hostName = properties.getProperty("hostName");
            String localPort = properties.getProperty("localPort");
            String localHost = properties.getProperty("localHost");
            ClientConfig clientConfig = (ClientConfig) ClientConfig.getConfig();
            clientConfig.setHostName(hostName);
            clientConfig.setLocalHost(localHost);
            clientConfig.setDomainName(hostName);
            clientConfig.setLocalPort(Integer.valueOf(localPort));
            LOGGER.info("请求穿透域名" + hostName + "本地端口" + localPort);
        } catch (IOException e) {
            LOGGER.error("config file error", e);
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
