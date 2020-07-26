package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intranet.client.ex.ServerConnectException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static priv.bigant.intrance.common.communication.CommunicationRequest.createCommunicationRequest;

public class Domain implements Connector {

    private static final Logger LOG = LoggerFactory.getLogger(Domain.class);

    private ClientConfig clientConfig;

    private ServerConnector.ConnectorThread httpConnect;
    private ServerConnector.ConnectorThread communicationConnect;

    private Communication communication;

    private DomainListener domainListener;

    public Domain(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void connect() throws ServerConnectException, IOException {

        startHttpProcessor();
        try {
            connectCommunication();
        } catch (IOException e) {
            throw new ServerConnectException("服务器链接失败", e);
        }
    }

    public void startListener() {
        this.domainListener = new DomainListener(clientConfig, this);
        domainListener.start();
    }

    public void startHttpProcessor() throws IOException {
        HttpProcessor httpProcessor = new HttpProcessor(clientConfig);
        httpConnect = new ServerConnector.ConnectorThread(httpProcessor, "clientHttpIntranetServiceProcess-thread");
        httpConnect.start();
    }

    public void connectCommunication() throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getIntranetPort()));
        channel.socket().setKeepAlive(true);
        channel.socket().setOOBInline(false);
        channel.configureBlocking(false);

        this.communication = new Communication(channel, new CommunicationProcessor.ClientCommunicationDispose(httpConnect));
        CommunicationProcessor communicationProcessor = new CommunicationProcessor(communication);
        communicationConnect = new ServerConnector.ConnectorThread(communicationProcessor, "clientCommunication");
        communicationConnect.register(channel, SelectionKey.OP_READ);//注册事件
        communicationConnect.start();

        CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
        communicationHttpFirst.setHost(clientConfig.getHostName());
        communication.writeN(createCommunicationRequest(communicationHttpFirst));
    }


    @Override
    public void showdown() {
        communicationConnect.showdown();
        httpConnect.showdown();
        domainListener.showdown();
    }

    @Override
    public String getName() {
        return "client domain";
    }

    public ServerConnector.ConnectorThread getHttpConnect() {
        return httpConnect;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public ServerConnector.ConnectorThread getCommunicationConnect() {
        return communicationConnect;
    }

    public Communication getCommunication() {
        return communication;
    }

    /**
     * 主进程监听器
     */
    public static class DomainListener extends Thread {
        private final static Logger LOGGER = LoggerFactory.getLogger(DomainListener.class);
        private ClientConfig clientConfig;
        private Domain domain;
        private boolean isRun = true;

        public DomainListener(ClientConfig clientConfig, Domain domain) {
            super("DomainListener");
            this.clientConfig = clientConfig;
            this.domain = domain;
        }

        @Override
        public void run() {
            while (true) {
                if (domain.getCommunication().isClose()) {
                    LOGGER.info("CommunicationListener 连接已断开");
                    try {
                        if (isRun) domain.connect();
                    } catch (Exception e) {
                        LOGGER.error("连接失败", e);
                    }
                } else {
                    LOGGER.info("CommunicationListener 连接正常");
                }
                try {
                    sleep(clientConfig.getListenerTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void showdown() {
            isRun = false;
        }
    }
}
