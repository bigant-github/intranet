package priv.bigant.intranet.server.communication;

import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.*;
import priv.bigant.intranet.server.ServerConfig;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 与客户段的交换器
 */
public class ServerCommunication extends HttpCommunication {
    private static final Logger LOG = Logger.getLogger(ServerCommunication.class.getName());
    public static final Map<String, SocketBean> MAP = new ConcurrentHashMap<>();
    private ServerConfig serverConfig;

    public ServerCommunication(SocketChannel socketChannel, ServerConfig serverConfig) throws IOException {
        super(socketChannel, ServerCommunicationDispose.getInstance(), serverConfig);
        this.serverConfig = serverConfig;
    }

    @Override
    public SocketBean getSocketBean() {
        long time = System.currentTimeMillis();
        String id = super.createSocketBean();
        while ((time + serverConfig.getWaitSocketTime()) > System.currentTimeMillis()) {
            SocketBean socketBean = MAP.get(id);
            if (socketBean != null) {
                MAP.remove(id);
                return socketBean;
            }

        }
        LOG.fine("getSocketBean TIMEOUT: createTime=" + time + "    endTime=" + System.currentTimeMillis());
        return null;
    }

    @Override
    public void putSocketBean(SocketBean socketBean) {
        String id = socketBean.getId();
        MAP.put(id, socketBean);
        LOG.fine("put socket id:" + id);
    }

    @Override
    public String createSocketBean() {
        return null;
    }

    /**
     * 统一处理请求
     */
    public static class ServerCommunicationDispose extends CommunicationDispose {

        private static ServerCommunicationDispose serverCommunicationDispose = new ServerCommunicationDispose();

        private ServerCommunicationDispose() {
        }

        @Override
        protected void httpReturn(CommunicationRequest communicationRequest, Communication communication) {
            //服务端没有这个类型请求
        }

        @Override
        protected void test(CommunicationRequest communicationRequest, Communication communication) {
            //test数据暂时不管
        }

        @Override
        protected void http(CommunicationRequest communicationRequest, Communication communication) {
            new ReadProcessThread((ServerCommunication) communication, communicationRequest).run();
        }

        @Override
        protected void httpAdd(CommunicationRequest communicationRequest, Communication communication) {
            //暂不处理客户端此类请求
        }

        public static ServerCommunicationDispose getInstance() {
            return serverCommunicationDispose;
        }


    }

    static class ReadProcessThread implements Runnable {
        final Logger LOG = Logger.getLogger(ReadProcessThread.class.getName());
        private ServerCommunication serverCommunication;
        private CommunicationRequest.CommunicationRequestHttpFirst communicationRequestHttpFirst;

        public ReadProcessThread(ServerCommunication serverCommunication, CommunicationRequest communicationRequest) {
            this.serverCommunication = serverCommunication;
            this.communicationRequestHttpFirst = communicationRequest.toJavaObject(CommunicationRequest.CommunicationRequestHttpFirst.class);
        }

        @Override
        public void run() {

            try {
                //读取客户端配置信息
                String host = communicationRequestHttpFirst.getHost();
                serverCommunication.setHost(host);
                boolean exist = HttpSocketManager.isExist(host);
                if (exist) {
                    boolean b = HttpSocketManager.get(host).isClose();
                    if (b) {//上一个连接已失效
                        HttpSocketManager.get(host).close();
                        HttpSocketManager.remove(host);
                    } else {//域名已存在
                        CommunicationRequest.CommunicationRequestHttpReturn communicationRequestHttpReturn = new CommunicationRequest.CommunicationRequestHttpReturn(CommunicationRequest.CommunicationRequestHttpReturn.Status.DOMAIN_OCCUPIED);
                        serverCommunication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpReturn));
                        serverCommunication.close();
                        LOG.info(host + CodeEnum.HOST_ALREADY_EXIST.getMsg());
                        return;
                    }
                }
                //连接成功
                HttpSocketManager.add(host, serverCommunication);
                CommunicationRequest.CommunicationRequestHttpReturn communicationRequestHttpReturn = new CommunicationRequest.CommunicationRequestHttpReturn(CommunicationRequest.CommunicationRequestHttpReturn.Status.SUCCESS);
                serverCommunication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpReturn));
                LOG.info(host + " 连接成功");
                for (int a = 0; a < 10; a++)
                    serverCommunication.createSocketBean();
            } catch (Exception e) {
                LOG.severe("连接失败" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
