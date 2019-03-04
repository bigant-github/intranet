package priv.bigant.intrance.common.thread;


import java.io.IOException;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/25.
 */
public class HttpResponseThread extends Thread {

    private SocketBean socketBean;

    public HttpResponseThread(Socket socket) throws IOException {
        this.socketBean = new SocketBean(socket);
    }

    /*@Override
    public void run() {
        SocketBean requestSocketBean = null;
        String key = null;
        try {

            key = new String(socketBean.readBytes(), "UTF-8").replace("\r\n", "UTF-8");
            requestSocketBean = ThroughManager.getRequestThroughMap(key);

            SocketWrite.dataInteraction(requestSocketBean, socketBean);

        } catch (IOException e) {
            try {
                requestSocketBean.write(("<h1>ERROR 500</h1>").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (requestSocketBean != null)
                try {
                    requestSocketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (socketBean != null)
                try {
                    socketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            ThroughManager.removeRequestThroughMap(key);
        }
    }*/

    @Override
    public void run() {
        RequestSocketBean requestSocketBean = null;
        String key = null;
        try {
            key = new String(socketBean.readBytes(), "UTF-8").replace("\r\n", "UTF-8");
            requestSocketBean = ThroughManager.getRequestThroughMap(key);

            SocketWrite.dataInteraction(requestSocketBean, socketBean);
        } catch (IOException e) {
            try {
                requestSocketBean.getRequestSocketBean().write(("<h1>ERROR 500</h1>").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (requestSocketBean != null)
                try {
                    requestSocketBean.getRequestSocketBean().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (socketBean != null)
                try {
                    socketBean.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            ThroughManager.removeRequestThroughMap(key);
        }
    }
}
