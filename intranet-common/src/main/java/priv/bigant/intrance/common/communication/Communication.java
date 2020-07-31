package priv.bigant.intrance.common.communication;

import priv.bigant.intrance.common.ChannelStream;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.log.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 客户端与服务端通信的工具
 */
public class Communication {
    private Logger log;

    protected byte[] bytes = new byte[1024];
    protected ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    protected SocketChannel socketChannel;
    protected ChannelStream channelStream;
    private Config config;
    private CommunicationDispose communicationDispose;

    /**
     * 协议开始符号
     */
    private static final byte L = '{';
    /**
     * 协议结束符号
     */
    private static final byte R = '}';

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public Communication(SocketChannel socketChannel, CommunicationDispose communicationDispose, Config config) {
        this(socketChannel, config);
        this.communicationDispose = communicationDispose;
    }

    public Communication(SocketChannel socketChannel, Config config) {
        this.socketChannel = socketChannel;
        this.channelStream = new ChannelStream(socketChannel, 1024, config.getLogName());
        this.config = config;
        this.log = LogUtil.getLog(config.getLogName(), this.getClass());
    }

    public synchronized void close() {
        try {
            if (socketChannel != null) socketChannel.close();
            if (channelStream != null) channelStream.close();
        } catch (IOException e) {
            log.severe("communication 关闭失败");
            e.printStackTrace();
        }
        socketChannel = null;
    }

    /**
     * 发送数据 将CommunicationReturn 转换为JSON数组发送
     *
     * @param communicationReturn
     * @throws IOException
     */
    public synchronized void writeN(CommunicationReturn communicationReturn) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(communicationReturn.toByte());
        byteBuffer.flip();
        int write = socketChannel.write(byteBuffer);
        if (log.isLoggable(Level.FINE)) {
            byteBuffer.flip();
            log.fine("write {size:" + write + ",value:" + StandardCharsets.UTF_8.decode(byteBuffer).toString() + "}");
        }
    }

    /**
     * 发送数据 将CommunicationReturn 转换为JSON数组发送
     *
     * @param communicationReturn
     * @throws IOException
     */
    public static void writeN(CommunicationReturn communicationReturn, SocketChannel socketChannel, Config config) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        byteBuffer.clear();
        byteBuffer.put(communicationReturn.toByte());
        byteBuffer.flip();
        Logger log = LogUtil.getLog(config.getLogName(), Communication.class);
        if (log.isLoggable(Level.FINE)) {
            log.fine("write :" + StandardCharsets.UTF_8.decode(byteBuffer).toString());
            byteBuffer.flip();
        }
        socketChannel.write(byteBuffer);
    }

    /**
     * 读取请求 自动封装为 CommunicationRequest 对象
     *
     * @throws IOException
     */
    public synchronized CommunicationRequest readRequest() throws IOException {
        return readRequest(channelStream);
    }

    /**
     * 读取请求 自动封装为 CommunicationRequest 对象
     *
     * @throws IOException
     */
    public static CommunicationRequest readRequest(SocketChannel socketChannel, String logName) throws IOException {
        return readRequest(new ChannelStream(socketChannel, 1024, logName));
    }

    public static CommunicationRequest readRequest(ChannelStream channelStream) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int LN = 0;
        int RN = 0;
        while (true) {
            byte c = channelStream.read();
            byteBuffer.put(c);
            if (c == L) LN++;
            else if (c == R) RN++;
            if (LN == RN) {
                return CommunicationRequest.createCommunicationRequest(new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_8));
            }

        }
    }

    /**
     * {\"id\":\"17968462-edba-44d0-95d5-b88fa5290c37\",\"type\":\"HTTP_ADD\"}{\"id\":\"755823d1-f5e7-4c32-897d-0fc1ba4faf18\",\"type\":\"HTTP_ADD\"}{\"id\":\"755823d1-f5e7-4c32-897d-0fc1ba4faf18\",\"type\":\"HTTP_ADD\"}
     * 读取多个请求 自动封装为 CommunicationRequest 对象
     *
     * @throws IOException
     */
    public synchronized List<CommunicationRequest> readRequests() throws IOException {
        List<CommunicationRequest> list = new ArrayList<>();
        while (channelStream.hasNext()) {
            list.add(readRequest());
        }
        return list;
    }


    /**
     * 读取多个请求并自定处理
     */
    public synchronized void disposeRequests() throws IOException {
        while (channelStream.hasNext()) {
            CommunicationRequest request = readRequest();
            log.fine(request.toString());
            communicationDispose.invoke(request, this);
        }
    }

    /**
     * 读取多个请求并自定处理
     */
    public synchronized void disposeRequest() throws IOException {
        if (!channelStream.hasNext())
            throw new NullPointerException("未找到request");
        CommunicationRequest request = readRequest();
        log.fine(request.toString());
        communicationDispose.invoke(request, this);

    }


    /**
     * 判断是否断开连接，断开返回true,没有返回false
     */
    public Boolean isClose() {
        try {
            CommunicationRequest.CommunicationRequestTest communicationRequestTest = new CommunicationRequest.CommunicationRequestTest();
            writeN(CommunicationRequest.createCommunicationRequest(communicationRequestTest));
            log.fine("isClose false");
            return false;
        } catch (Exception se) {
            log.fine("isClose true");
            return true;
        }
    }

    public CommunicationDispose getCommunicationDispose() {
        return communicationDispose;
    }

    public void setCommunicationDispose(CommunicationDispose communicationDispose) {
        this.communicationDispose = communicationDispose;
    }
}
