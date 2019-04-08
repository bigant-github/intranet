package priv.bigant.intranet.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class ThroughThread extends Thread {

    private int port;
    private String hostName;
    private String domainName;
    private int thisPort;


    public ThroughThread(String hostName, int port, String domainName, int thisPort) {
        this.hostName = hostName;
        this.port = port;
        this.domainName = domainName;
        this.thisPort = thisPort;
    }

    private int httpSocketPort;
    private Socket socket;
    private PrintWriter pw;
    private BufferedReader br;


    @Override
    public void run() {
        try {
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(hostName, port));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.pw = new PrintWriter(socket.getOutputStream());

            pw.write(domainName);
            pw.flush();

            String s = br.readLine().replace("\r\n", "");
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
                if (pw != null)
                    pw.close();
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void threadDispose() {
        while (true) {
            try {
                String s = br.readLine().replace("\r\n", "");
                new SocketThread(s, hostName, httpSocketPort, thisPort).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
