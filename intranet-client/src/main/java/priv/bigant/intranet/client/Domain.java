package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static priv.bigant.intrance.common.communication.CommunicationRequest.createCommunicationRequest;

public class Domain implements Connector {

    private static final Logger LOG = LoggerFactory.getLogger(Domain.class);
    ServerConnector.ConnectorThread httpConnect;
    ClientConfig clientConfig;
    ServerConnector.ConnectorThread communicationConnect;


    public Domain(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }


    public void connect() throws Exception {
        connectHttp();
        connectCommunication();
    }

    public void connectHttp() throws IOException {
        HttpProcessor httpProcessor = new HttpProcessor(clientConfig);
        httpConnect = new ServerConnector.ConnectorThread(httpProcessor, "clientHttpIntranetServiceProcess-thread");
        httpConnect.start();
    }

    public void connectCommunication() throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getIntranetPort()));
        channel.socket().setKeepAlive(true);
        channel.socket().setOOBInline(false);
        channel.configureBlocking(false);

        Communication communication = new Communication(channel, new CommunicationProcessor.ClientCommunicationDispose(httpConnect));
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

    }

    @Override
    public String getName() {
        return "client domain";
    }
}
