package priv.bigant.test.Integral;

import org.junit.Test;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intranet.server.ServerConfig;
import priv.bigant.intranet.server.process.CommunicationProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpIntranetConnectorProcessTest {

    @Test
    public void start() throws IOException {
        ServerConfig config = (ServerConfig) ServerConfig.getConfig();
        CommunicationProcessor httpIntranetConnectorProcess = new CommunicationProcessor();
        ServerConnector testHttpIntranetConnectorProcess = new ServerConnector("信息交换器", httpIntranetConnectorProcess, 9999);
        testHttpIntranetConnectorProcess.start();
        System.in.read();
    }

    @Test
    public void send() throws Exception {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9999));
        Communication communication = new Communication(socketChannel);
        CommunicationRequest.CommunicationRequestTest communicationRequestTest = new CommunicationRequest.CommunicationRequestTest();
        for (int i = 0; i < 1000; i++) {
            communication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestTest));
        }
    }

    @Test
    public void a() throws Exception {
        CommunicationRequest.CommunicationRequestTest communicationRequestTest = new CommunicationRequest.CommunicationRequestTest();
        CommunicationRequest communicationRequest = CommunicationRequest.createCommunicationRequest(communicationRequestTest);
        byte[] bytes = communicationRequest.toByte();
        System.out.println(Arrays.toString("{\"type\":\"TEST\"}".getBytes(StandardCharsets.UTF_8)));
    }

}
