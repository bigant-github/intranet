package priv.bigant.intranet.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class SocketThread extends Thread {

    private int httpSocketPort;
    private String msg;
    private String httpSocketHostName;
    private int thisPort;

    public SocketThread(String msg, String httpSocketHostName, int httpSocketPort, int thisPort) {
        this.httpSocketPort = httpSocketPort;
        this.msg = msg;
        this.httpSocketHostName = httpSocketHostName;
        this.thisPort = thisPort;
    }

    private SocketBean socketBean;

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(httpSocketHostName, httpSocketPort));
            this.socketBean = new SocketBean(socket);
            this.socketBean.write(msg.getBytes(StandardCharsets.UTF_8));
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketBean != null)
                    socketBean.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*private void dispose() {
        byte[] bytes = new byte[1024 * 3];
        BufferedInputStream bis = socketBean.getBis();
        BufferedOutputStream bos = socketBean.getBos();
        SocketBean requestSocketBean = null;
        try {
            int read = bis.read(bytes);
            int counts = 0;
            if (read != -1) {
                System.out.println("-----------------------------接受开始");
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("127.0.0.1", this.thisPort));
                requestSocketBean = new SocketBean(socket);
                BufferedInputStream bis1 = requestSocketBean.getBis();
                BufferedOutputStream bos1 = requestSocketBean.getBos();
                do {
                    bos1.write(bytes, 0, read);
                } while (bis.available() != 0 && (read = bis.read(bytes)) != -1);
                bos1.flush();

                do {
                    read = bis1.read(bytes);
                    counts += read;
                    if (read != -1) {
                        bos.write(bytes, 0, read);
                    }
                    bos.flush();
                } while (read != -1);

            }
            System.out.println("-----------------------------接受完成" + counts);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (requestSocketBean != null) {
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/


    private void dispose() {
        byte[] bytes = new byte[1024 * 3];
        BufferedInputStream bis = socketBean.getBis();
        BufferedOutputStream bos = socketBean.getBos();
        SocketBean requestSocketBean = null;
        try {
            int read = bis.read(bytes);
            int counts = 0;
            if (read != -1) {
                System.out.println("-----------------------------接受开始");
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("127.0.0.1", this.thisPort));
                requestSocketBean = new SocketBean(socket);
                BufferedInputStream bis1 = requestSocketBean.getBis();
                BufferedOutputStream bos1 = requestSocketBean.getBos();
                do {
                    bos1.write(bytes, 0, read);
                    bos1.flush();
                    do {
                        read = bis1.read(bytes);
                        counts += read;
                        if (read != -1) {
                            bos.write(bytes, 0, read);
                        }
                    } while (read == -1);
                    bos.flush();
                } while ((read = bis.read(bytes)) != -1);
            }
            System.out.println("-----------------------------接受完成" + counts);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (requestSocketBean != null) {
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void disposeNew() {
        byte[] bytes = new byte[1024 * 3];
        BufferedInputStream bis = socketBean.getBis();
        BufferedOutputStream bos = socketBean.getBos();
        SocketBean requestSocketBean = null;
        try {
            Socket socket = new Socket();
            int read1;
            boolean flag = false;
            do {
                read1 = bis.read(bytes);
                int counts = 0;

                if (read1 != -1) {

                    if (!flag) {
                        socket.connect(new InetSocketAddress("127.0.0.1", this.thisPort));
                        flag = true;
                        requestSocketBean = new SocketBean(socket);
                    }
                    System.out.println("-----------------------------接受开始");
                    BufferedInputStream bis1 = requestSocketBean.getBis();
                    BufferedOutputStream bos1 = requestSocketBean.getBos();
                    do {
                        bos1.write(bytes, 0, read1);
                    } while (bis.available() != 0 && (read1 = bis.read(bytes)) != -1);
                    bos1.flush();
                    int read;
                    do {
                        read = bis1.read(bytes);
                        counts += read;
                        if (read != -1) {
                            bos.write(bytes, 0, read);
                        }
                        bos.flush();
                    } while (read != -1);

                }
                System.out.println("-----------------------------接受完成" + counts);
            } while ((read1 = bis.read(bytes)) != -1);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (requestSocketBean != null) {
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
