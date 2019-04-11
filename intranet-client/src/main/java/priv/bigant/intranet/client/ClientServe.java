package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class ClientServe extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientServe.class);
    ClientConfig clientConfig;

    public ClientServe(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    private byte[] bytes = new byte[1024];
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    boolean start = false;

    @Override
    public void run() {
        connect();
        threadDispose();
    }

    void connect() {
        try {
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getPort()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            outputStream.write(clientConfig.getDomainName().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            inputStream.read(bytes);
            String s = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "");
            String[] split = s.split("-");
            if ("10000".equals(split[0])) {
                start = true;
                LOGGER.info(split[0] + "    " + split[1] + "      " + split[2]);
                System.out.println();
            } else {
                start = false;
                LOGGER.error(split[0] + "    " + split[1]);
            }
        } catch (IOException e) {
            LOGGER.error("connect error: host =" + clientConfig.getDomainName(), e);
        }
    }

    void close() {
        try {
            if (socket != null)
                socket.close();
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void threadDispose() {
        while (true) {//监听连接情况
            try {
                if (start) {
                    while (true) {
                        ClientHttpProcessor httpProcessor = new ClientHttpProcessor(socket, clientConfig);
                        httpProcessor.run();
                    }
                }
            } catch (Exception e) {
                LOGGER.error("error", e);
            } finally {
                close();
            }
            try {
                connect();
                sleep(1000);
                LOGGER.warn("try connect: host=" + clientConfig.getDomainName());
            } catch (InterruptedException e) {
                LOGGER.error("try connect error", e);
            }
        }
    }
}
