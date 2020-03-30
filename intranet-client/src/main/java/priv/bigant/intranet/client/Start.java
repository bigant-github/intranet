package priv.bigant.intranet.client;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    private final static Logger LOG = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        boolean b = createdConfig();
        if (!b)
            return;
        ClientConfig config = (ClientConfig) ClientConfig.getConfig();

        ConnectorThread serviceConnectorThread;
        HttpIntranetServiceProcess httpIntranetServiceProcess = new HttpIntranetServiceProcess();
        try {
            serviceConnectorThread = new ConnectorThread(httpIntranetServiceProcess);
            serviceConnectorThread.start();
        } catch (IOException e) {
            LOG.error("http处理器启动失败");
            return;
        }

        ClientCommunication clientCommunication = new ClientCommunication(serviceConnectorThread);
        try {
            clientCommunication.createCommunicationProcess();
            clientCommunication.connect();
            new CommunicationListener(clientCommunication, config.getListenerTime()).start();
        } catch (Exception e) {
            serviceConnectorThread.showdown();
            LOG.error("通信器连接失败", e);
        }

    }

    /**
     * 加载配置文件
     */
    public static boolean createdConfig() {
        ClientConfig clientConfig = (ClientConfig) ClientConfig.getConfig();
        String configFile = System.getProperty("configFile");//通过启动参数指定配置文件位置
        if (StringUtils.isBlank(configFile))
            configFile = System.getProperty("user.dir") + "/conf.properties";
        Properties properties;
        try (FileInputStream inputStream = new FileInputStream(configFile)) {//properties 文件
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            LOG.error("加载配置文件错误", e);
            return false;
        }

        try {
            properties.putAll(System.getProperties());
            System.getProperties().putAll(properties);
            BeanUtils.copyProperties(clientConfig, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("加载配置文件错误", e);
            return false;
        }
        LOG.info("请求穿透域名" + clientConfig.getHostName() + "本地端口" + clientConfig.getHostName());
        return true;
    }
}
