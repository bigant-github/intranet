package priv.bigant.intranet.client;

import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intranet.client.ex.ServerConnectException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static priv.bigant.intrance.common.communication.CommunicationRequest.createCommunicationRequest;

public class Domain implements Connector {

    private static final ClientConfig clientConfig = ClientConfig.getClientConfig();

    private ServerConnector.ConnectorThread httpConnect;
    private ServerConnector.ConnectorThread communicationConnect;
    private CommunicationProcessor communicationProcessor;
    private Communication communication;
    private DomainListener domainListener;
    private Consumer<CommunicationRequest.CommunicationRequestHttpReturn.Status> returnError;


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
        HttpProcessor httpProcessor = new HttpProcessor();
        httpConnect = new ServerConnector.ConnectorThread(httpProcessor, "clientHttpIntranetServiceProcess-thread", clientConfig);
        httpConnect.start();
    }

    public void connectCommunication() throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getIntranetPort()));
        channel.socket().setKeepAlive(true);
        channel.socket().setOOBInline(false);
        channel.configureBlocking(false);

        this.communication = new Communication(channel, new CommunicationProcessor.ClientCommunicationDispose(httpConnect, clientConfig, x -> {
            if (returnError != null) returnError.accept(x);
        }), clientConfig);
        this.communicationProcessor = new CommunicationProcessor(communication, clientConfig);

        communicationConnect = new ServerConnector.ConnectorThread(communicationProcessor, "clientCommunication", clientConfig);
        communicationConnect.register(channel, SelectionKey.OP_READ);//注册事件
        communicationConnect.start();

        CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
        communicationHttpFirst.setHost(clientConfig.getHostName());
        communication.writeN(createCommunicationRequest(communicationHttpFirst));
    }


    @Override
    public void showdown() {
        if (communicationConnect != null) communicationConnect.showdown();
        if (httpConnect != null) httpConnect.showdown();
        if (domainListener != null) domainListener.showdown();
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

    public void setReturnError(Consumer<CommunicationRequest.CommunicationRequestHttpReturn.Status> returnError) {
        this.returnError = returnError;
    }

    public Consumer<CommunicationRequest.CommunicationRequestHttpReturn.Status> getReturnError() {
        return returnError;
    }

    /**
     * 主进程监听器
     */
    public static class DomainListener extends Thread {
        private static final Logger LOG = LogUtil.getLog();

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
            while (isRun) {
                if (domain.getCommunication().isClose()) {
                    LOG.info("CommunicationListener 连接已断开");
                    try {
                        if (isRun) domain.connect();
                    } catch (Exception e) {
                        LOG.severe("连接失败" + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    LOG.info("CommunicationListener 连接正常");
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
