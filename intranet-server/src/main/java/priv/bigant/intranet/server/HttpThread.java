package priv.bigant.intranet.server;


import org.apache.commons.lang3.StringUtils;
import priv.bigant.intrance.common.thread.RequestSocketBean;
import priv.bigant.intrance.common.thread.SocketBean;
import priv.bigant.intrance.common.thread.ThroughManager;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class HttpThread extends Thread {

    // 和本线程相关的Socket
    private SocketBean requestSocketBean;

    public HttpThread(Socket socket) throws IOException {
        this.requestSocketBean = new SocketBean(socket);
    }

    /*@Override
    public void run() {
        try {
            String hostName = this.requestSocketBean.getHostName();
            SocketBean socketBean = ThroughManager.get(hostName);
            if (socketBean == null) {
                this.requestSocketBean.write(("<h1>" + hostName + "未检测到改域名</h1>").getBytes());
                return;
            }
            SocketWrite.dataInteraction(requestSocketBean, socketBean);
        } catch (IOException e) {
            try {
                this.requestSocketBean.write(("<h1>ERROR 500</h1>").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                requestSocketBean.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    boolean isClose = true;

   /* @Override
    public void run() {
        try {
            //String hostName = this.requestSocketBean.getHostName();
            System.out.println("请求协助穿透");
            byte[] bytes = requestSocketBean.readBytes(1024 * 3);
            if (bytes.length > 0) {
                String request = new String(bytes);
                int i = request.indexOf("Host:") + 5;
                if (i != -1) {

                    String s = request.substring(i);
                    String[] split = s.split("\r\n");
                    String hostName = split[0].trim();

                    if (StringUtils.isNotBlank(hostName)) {
                        SocketBean socketBean = ThroughManager.get(hostName);
                        LOGGER.debug("接受到通过穿透过来的Http请求" + hostName);
                        if (socketBean == null) {
                            this.requestSocketBean.write(("<h1>" + hostName + "未检测到改域名</h1>").getBytes());
                            return;
                        }
                        String key = ThroughManager.getKey();
                        ThroughManager.addRequestThroughMap(key, new RequestSocketBean(bytes, requestSocketBean));
                        socketBean.write((key + "\r\n").getBytes());
                        isClose = false;
                    }
                }
            }
        } catch (IOException e) {
            try {
                this.requestSocketBean.write(("<h1>ERROR 500</h1>").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (isClose)
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }*/


    @Override
    public void run() {
        try {
            //String hostName = this.requestSocketBean.getHostName();
            System.out.println("请求协助穿透");
            byte[] bytes = requestSocketBean.readBytes(1024 * 3);
            if (bytes.length > 0) {
                String request = new String(bytes);
                int i = request.indexOf("Host:") + 5;

                String s = request.substring(i);
                String[] split = s.split("\r\n");
                String hostName = split[0].trim();

                if (StringUtils.isNotBlank(hostName)) {
                    SocketBean socketBean = ThroughManager.get(hostName);
                    System.out.println("接受到通过穿透过来的Http请求" + hostName);
                    if (socketBean == null) {
                        this.requestSocketBean.write(("<h1>" + hostName + "未检测到改域名</h1>").getBytes());
                        return;
                    }
                    String key = ThroughManager.getKey();
                    ThroughManager.addRequestThroughMap(key, new RequestSocketBean(bytes, requestSocketBean));
                    socketBean.write((key + "\r\n").getBytes());
                    isClose = false;
                }
            }
        } catch (IOException e) {
            try {
                this.requestSocketBean.write(("<h1>ERROR 500</h1>").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (isClose)
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
