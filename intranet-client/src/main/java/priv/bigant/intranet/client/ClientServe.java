package priv.bigant.intranet.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class ClientServe extends Thread {


    ClientConfig clientConfig;

    public ClientServe(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    byte[] bytes = new byte[1024];
    private Socket socket;
    InputStream inputStream;
    OutputStream outputStream;

    @Override
    public void run() {
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
                System.out.println(split[0] + "    " + split[1] + "      " + split[2]);
                threadDispose();
            } else
                System.out.println(split[0] + "    " + split[1]);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
    }

    private void threadDispose() {
        while (true) {
            ClientHttpProcessor httpProcessor = new ClientHttpProcessor(socket, clientConfig);
            httpProcessor.run();
        }
    }
}
