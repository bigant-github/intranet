package priv.bigant.intranet.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    private final static Logger LOG = LoggerFactory.getLogger(Start.class);
    private static ClientConfig clientConfig;

    public static void main(String[] args) {
        boolean b = createdConfig();
        if (b)
            new ClientCommunication().start();
    }

    public static boolean createdConfig() {
        //new Home().showHome();
        FileInputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(new File("/tmp/conf.properties"));
            inputStream = new FileInputStream(Start.class.getResource("/conf.properties").getPath());
            //inputStream = new FileInputStream(new File(userPath + "/conf.properties"));
            Properties properties = new Properties();
            properties.load(inputStream);
            String hostName = properties.getProperty("hostName");
            String localPort = properties.getProperty("localPort");
            String localHost = properties.getProperty("localHost");
            clientConfig = (ClientConfig) ClientConfig.getConfig();
            clientConfig.setHostName(hostName);
            clientConfig.setLocalHost(localHost);
            clientConfig.setDomainName(hostName);
            clientConfig.setLocalPort(Integer.valueOf(localPort));
            LOG.info("请求穿透域名" + hostName + "本地端口" + localPort);
        } catch (IOException e) {
            LOG.error("config file error", e);
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
