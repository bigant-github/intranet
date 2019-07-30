package priv.bigant.intranet.client;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (b)
            new ClientCommunication().start();
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
