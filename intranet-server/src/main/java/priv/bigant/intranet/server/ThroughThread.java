package priv.bigant.intranet.server;


import priv.bigant.intrance.common.thread.SocketBean;
import priv.bigant.intrance.common.thread.ThroughManager;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class ThroughThread extends Thread {
    // 和本线程相关的Socket
    private SocketBean socketBean;

    private boolean isClose;

    public ThroughThread(Socket socket) throws IOException {
        this.socketBean = new SocketBean(socket);
    }

    @Override
    public void run() {
        try {

            byte[] bytes = socketBean.readBytes();
            if (bytes != null && bytes.length > 0) {
                String s = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "");
                String domainName = s.split("-")[0];
                //验证改域名是否已经注册
                SocketBean socketBean = ThroughManager.get(domainName);
                if (socketBean == null) {
                    this.socketBean.setDomainName(domainName);
                    ThroughManager.add(this.socketBean);
                    isClose = false;
                    this.socketBean.write("10000-链接成功-45556\r\n".getBytes(StandardCharsets.UTF_8));
                } else
                    this.socketBean.write("10001-链接失败-45556\r\n".getBytes(StandardCharsets.UTF_8));

            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (isClose)
                try {
                    socketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /*// 线程执行的操作，响应客户端的请求
    public void run() {


        InetAddress address = socket.getInetAddress();
        System.out.println("新连接，客户端的IP：" + address.getHostAddress() + " ,端口：" + socket.getPort());

        try {
            //pw.write("已有客户端列表：" + server.connections + "\n");

            // 获取输入流，并读取客户端信息
            String info = null;

            while ((info = br.readLine()) != null) {
                // 循环读取客户端的信息
                System.out.println("我是服务器，客户端说：" + info);

                if (info.startsWith("newConn_")) {
                    //接收到穿透消息，通知目标节点
                    String[] infos = info.split("_");
                    //目标节点的外网ip地址
                    String ip = infos[1];
                    //目标节点的外网端口
                    String port = infos[2];

                    System.out.println("打洞到 " + ip + ":" + port);

                    for (ThroughThread server : server.connections) {
                        if (server.socket.getInetAddress().getHostAddress().equals(ip)
                                && server.socket.getPort() == Integer.parseInt(port)) {

                            //发送命令通知目标节点进行穿透连接
                            server.pw.write("autoConn_" + socket.getInetAddress().getHostAddress() + "_" + socket.getPort()
                                    + "\n");
                            server.pw.flush();

                            break;
                        }
                    }
                } else {
                    // 获取输出流，响应客户端的请求
                    pw.write("欢迎您！" + info + "\n");
                    // 调用flush()方法将缓冲输出
                    pw.flush();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("客户端关闭：" + address.getHostAddress() + " ,端口：" + socket.getPort());
            //server.connections.remove(this);
            // 关闭资源
            try {
                if (pw != null) {
                    pw.close();
                }
                if (br != null) {
                    br.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "ThroughThread [socket=" + socket + "]";
    }*/
}
