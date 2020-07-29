package priv.bigant.intranet.client;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class Start {

    private final static Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) throws Exception {
        ClientConfig config = createdConfig();
        Domain domain = new Domain(config);
        domain.setReturnError(x -> domain.showdown());
        domain.connect();
        domain.startListener();
        //start(config);
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
            LOG.severe("加载配置文件错误" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try {
            properties.putAll(System.getProperties());
            System.getProperties().putAll(properties);
            BeanUtils.copyProperties(clientConfig, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.severe("加载配置文件错误" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        LOG.info("请求穿透域名" + clientConfig.getHostName() + "本地端口" + clientConfig.getHostName());
        return clientConfig;
    }
}
