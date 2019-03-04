package priv.bigant.intrance.common.thread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class SocketWrite {


    /*public static boolean write(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        int read;
        byte[] bytes = new byte[1024 * 3];
        int counts = 0;
        do {
            read = bis.read(bytes);
            System.out.println("read" + read);
            counts += read;
            if (read != -1) {
                bos.write(bytes, 0, read);
            }
            bos.flush();
        } while (read != -1);// while (read != -1 && bis.available() != 0);
        System.out.println("++++++++++++++++++++++++counts = " + counts);
        *//*if (counts != -1) {

            return true;
        }*//*
        return false;

    }*/

    public static boolean write(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        int read;
        byte[] bytes = new byte[1024 * 1024 * 3];
        read = bis.read(bytes);
        System.out.println("read" + read);
        if (read != -1) {
            bos.write(bytes, 0, read);
        }
        bos.flush();
        System.out.println("++++++++++++++++++++++++counts = " + read);
        return false;
    }

    public static void write1(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        do {
            read = bis.read(bytes);
            if (read != -1)
                bos.write(bytes, 0, read);
        } while (read != -1 && bis.available() != 0);

        bos.flush();
    }


    public static void dataInteraction(SocketBean requestSocketBean, SocketBean socketBean) throws IOException {
        synchronized (socketBean) {//将客户端连接进行锁，防止数据错乱
            System.out.println("-----------------------------接受开始");
            boolean write = write(requestSocketBean.getBis(), socketBean.getBos());
            if (write)
                write(socketBean.getBis(), requestSocketBean.getBos());
            System.out.println("-----------------------------接受结束");
        }
    }

    /*public static void dataInteraction(RequestSocketBean requestSocketBean, SocketBean socketBean) throws IOException {
        System.out.println("-----------------------------接受开始");
        socketBean.write(requestSocketBean.getData());
        write(socketBean.getBis(), requestSocketBean.getRequestSocketBean().getBos());
        System.out.println("-----------------------------接受结束");
    }*/

    public synchronized static void dataInteraction(RequestSocketBean requestSocketBean, SocketBean socketBean) throws IOException {
        System.out.println("-----------------------------接受开始");
        byte[] data = requestSocketBean.getData();
        SocketBean request = requestSocketBean.getRequestSocketBean();
        do {
            System.out.println(new String(data));
            socketBean.write(data);
            write(socketBean.getBis(), request.getBos());
            data = request.readBytes();
        } while (data.length > 0);
        System.out.println("-----------------------------接受结束");
    }


    public static void dataInteractionNew(RequestSocketBean requestSocketBean, SocketBean socketBean) throws IOException {
        System.out.println("-----------------------------接受开始");
        byte[] bytes = requestSocketBean.getData();
        do {
            if (bytes != null) {
                socketBean.write(bytes);
                write(socketBean.getBis(), requestSocketBean.getRequestSocketBean().getBos());
            }
        } while ((bytes = requestSocketBean.getRequestSocketBean().readBytes()) != null);
        System.out.println("-----------------------------接受结束");
    }

}
