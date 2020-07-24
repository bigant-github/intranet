package priv.bigant.intranet.client;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.manager.ConnectorManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    private final static Logger LOG = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) throws Exception {
        ClientConfig config = createdConfig();
        Domain domain = new Domain(config);
        domain.connect();
        domain.startListener();
        //start(config);
    }

    public static void start(ClientConfig config) {
        ConnectorThread serviceConnectorThread;
        HttpProcessor httpProcessor = new HttpProcessor(config);

        try {
            serviceConnectorThread = new ConnectorThread(httpProcessor, "clientHttpIntranetServiceProcess-thread");
            serviceConnectorThread.start();
        } catch (IOException e) {
            LOG.error("http处理器启动失败");
            return;
        }

        ClientCommunication clientCommunication = new ClientCommunication(serviceConnectorThread, config);
        try {
            clientCommunication.createCommunicationProcess();
            clientCommunication.connect();
            new CommunicationListener(clientCommunication, config.getListenerTime()).start();
        } catch (Exception e) {
            LOG.error("通信器连接失败", e);
            ConnectorManager.showdownAll();

        }
    }

    /**
     * 加载配置文件
     *
     * @return
     */
    public static ClientConfig createdConfig() throws IOException, InvocationTargetException, IllegalAccessException {
        ClientConfig clientConfig = new ClientConfig();
        String configFile = System.getProperty("configFile");//通过启动参数指定配置文件位置
        if (StringUtils.isBlank(configFile))
            configFile = System.getProperty("user.dir") + "/conf.properties";
        Properties properties;
        try (FileInputStream inputStream = new FileInputStream(configFile)) {//properties 文件
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            LOG.error("加载配置文件错误", e);
            throw e;
        }

        try {
            properties.putAll(System.getProperties());
            System.getProperties().putAll(properties);
            BeanUtils.copyProperties(clientConfig, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("加载配置文件错误", e);
            throw e;
        }
        LOG.info("请求穿透域名" + clientConfig.getHostName() + "本地端口" + clientConfig.getHostName());
        Config.config = clientConfig;
        return clientConfig;
    }
}
