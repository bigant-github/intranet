package priv.bigant.intrance.common.communication;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.ChannelStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端与服务端通信的工具
 */
public class Communication extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);

    protected byte[] bytes = new byte[1024];
    protected ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    protected SocketChannel socketChannel;
    private ChannelStream channelStream;

    private static final byte L = '{';
    private static final byte R = '}';

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public Communication(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        this.channelStream = new ChannelStream(socketChannel, 1024);
    }

    public Communication() {

    }

    public synchronized void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            LOGGER.error("communication 关闭失败");
        }
        socketChannel = null;
    }

    @Deprecated
    public synchronized byte[] readN() throws IOException {
        byteBuffer.clear();

        int readNum = socketChannel.read(byteBuffer);
        if (readNum < 0)
            throw new IOException("read -1");

        byte[] subArray = ArrayUtils.subarray(byteBuffer.array(), 0, readNum);
        byteBuffer.flip();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("读取到数据 :" + new String(subArray, StandardCharsets.UTF_8));
        }

        return subArray;
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("write :" + StandardCharsets.UTF_8.decode(byteBuffer).toString());
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

    public synchronized CommunicationResponse readResponse() throws IOException {
        return CommunicationResponse.createCommunicationResponse(readN());
    }


    /**
     * 判断是否断开连接，断开返回true,没有返回false
     */
    public Boolean isClose() {
        try {
            CommunicationRequest.CommunicationRequestTest communicationRequestTest = new CommunicationRequest.CommunicationRequestTest();
            writeN(CommunicationRequest.createCommunicationRequest(communicationRequestTest));
            LOGGER.debug("isClose false");
            return false;
        } catch (Exception se) {
            LOGGER.debug("isClose true");
            return true;
        }
    }
}
