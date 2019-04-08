package priv.bigant.intranet.client;

import priv.bigant.intrance.common.http.RequestProcessor;

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
    private int httpSocketPort;
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
                this.httpSocketPort = new Integer(split[2]);
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
        ClientContainer clientContainer = new ClientContainer(clientConfig);
        while (true) {
            RequestProcessor httpProcessor = new RequestProcessor(clientContainer, socket, clientConfig);
            httpProcessor.run();
        }
    }


    private SocketBean socketBean;


   /* @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostName, port));
            socketBean = new SocketBean(socket);
            socketBean.write((domainName + "\r\n").getBytes("UTF-8"));
            byte[] bytes = socketBean.readBytes();
            String s = new String(bytes, "UTF-8").replace("\r\n", "");
            String[] split = s.split("-");
            if ("10000".equals(split[0])) {
                dispose();
            } else {
                System.out.println("链接失败 " + split[0] + "    " + split[0]);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socketBean.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/


   /* private void dispose() {
        byte[] bytes = new byte[1024];
        BufferedInputStream bis = socketBean.getBis();
        BufferedOutputStream bos = socketBean.getBos();
        while (true) {
            SocketBean requestSocketBean = null;
            try {
                int read = bis.read(bytes);

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("127.0.0.1", this.thisPort));
                requestSocketBean = new SocketBean(socket);
                BufferedInputStream bis1 = requestSocketBean.getBis();
                BufferedOutputStream bos1 = requestSocketBean.getBos();

                do {
                    bos1.write(bytes, 0, read);
                } while (bis.available() != 0 && (read = bis.read(bytes)) != -1);
                bos1.flush();
                System.out.println("-----------------------------接受开始");

                int counts = 0;
                do {
                    read = bis1.read(bytes);
                    counts += read;
                    if (read != -1) {
                        bos.write(bytes, 0, read);
                    }
                } while (read != -1);
                System.out.println("-----------------------------接受完成" + counts);
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (requestSocketBean != null) {
                    *//*try {
                        requestSocketBean.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*//*
                }
            }
        }
    }*/

}
