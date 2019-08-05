package priv.bigant.intrance.common.communication;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
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
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected SocketChannel socketChannel;

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public Communication(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public Communication(SocketChannel socketChannel) throws IOException {
        this(socketChannel.socket());
        this.socketChannel = socketChannel;
    }

    public Communication() {

    }

    public synchronized void close() {
        try {
            LOGGER.debug("communication close");
            if (socket != null)
                socket.close();
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            LOGGER.error("communication close error ", e);
        }
        socket = null;
        inputStream = null;
        outputStream = null;

    }

    public synchronized byte[] readN() throws IOException {
        byteBuffer.clear();
        //socketChannel.socket().sendUrgentData(1);
        int readNum = socketChannel.read(byteBuffer);
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
            LOGGER.debug("write :" + Charset.forName("UTF-8").decode(byteBuffer).toString());
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
        return CommunicationRequest.createCommunicationRequest(readN());
    }

    /**
     * 读取多个请求 自动封装为 CommunicationRequest 对象
     *
     * @throws IOException
     */
    public synchronized List<CommunicationRequest> readRequests() throws IOException {
        byte[] bytes = readN();
        List<CommunicationRequest> list = new ArrayList<>();
        String s = new String(bytes, StandardCharsets.UTF_8);
        int i = s.indexOf("}{");
        while (i > 1) {
            list.add(CommunicationRequest.createCommunicationRequest(s.substring(0, i + 1).getBytes()));
            s = s.substring(i + 1);
            i = s.indexOf("}{");
        }
        list.add(CommunicationRequest.createCommunicationRequest(s.getBytes()));
        return list;
    }

    public static void main(String[] args) {
        String s = "{\"id\":\"17968462-edba-44d0-95d5-b88fa5290c37\",\"type\":\"HTTP_ADD\"}{\"id\":\"755823d1-f5e7-4c32-897d-0fc1ba4faf18\",\"type\":\"HTTP_ADD\"}{\"id\":\"755823d1-f5e7-4c32-897d-0fc1ba4faf18\",\"type\":\"HTTP_ADD\"}";

    }

    public synchronized CommunicationResponse readResponse() throws IOException {
        return CommunicationResponse.createCommunicationResponse(readN());
    }


    /**
     * 判断是否断开连接，断开返回true,没有返回false
     */
    public Boolean isClose() {
        try {
            //socketChannel.socket().sendUrgentData(12123123);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            LOGGER.debug("isClose false");
            return false;
        } catch (Exception se) {
            LOGGER.debug("isClose true");
            return true;
        }
    }
}
