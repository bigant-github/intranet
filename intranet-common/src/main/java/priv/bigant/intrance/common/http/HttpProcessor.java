package priv.bigant.intrance.common.http;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.exception.ServletException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * http 通信核心处理
 */
public abstract class HttpProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpProcessor.class);

    protected Config config;
    protected RequestProcessor requestProcessor;
    protected ResponseProcessor responseProcessor;
    protected SocketBean receiver;
    protected SocketBean socketBean;

    public HttpProcessor(SocketBean socketBean) {
        this.config = Config.getConfig();
        this.socketBean = socketBean;
    }


    protected void process() throws IOException, ServletException {
        try {
            do {
                requestProcessor = new RequestProcessor(socketBean, config);
                requestProcessor.process();
                if (requestProcessor.isSendAck())
                    //TODO send ACK service
                    LOGGER.warn("send ack service last");

                if (receiver != null) {
                    boolean b = receiver.sendUrgentData();
                    if (!b) {
                        LOGGER.info("客户端已关闭。。。。。。。。。。。。。。。。。。。。。。。。");
                        close();
                    }
                }

                if (receiver == null) {
                    try {
                        receiver = getSocketBean();
                    } catch (IOException e) {
                        LOGGER.error("get socket bean", e);
                    }

                    if (receiver == null) {
                        LOGGER.error("receiver is null error");
                    }
                }

                try {
                    mutual(requestProcessor.getInput(), requestProcessor.getContentLength(), receiver.getOs(), requestProcessor.isChunked());
                } catch (IOException e) {
                    LOGGER.error("request mutual error", e);
                    //TODO mutual service
                    throw e;
                }

                responseProcessor = new ResponseProcessor(receiver, config);
                responseProcessor.process();
                try {
                    mutual(responseProcessor.getInput(), responseProcessor.getContentLength(), socketBean.getOs(), responseProcessor.isChunked());
                } catch (IOException e) {
                    LOGGER.error("response mutual error", e);
                    //TODO mutual service
                    throw e;
                }
                if (requestProcessor.isKeepAlive() && responseProcessor.isKeepAlive()) {
                    LOGGER.debug("保持连接");
                }
            } while (requestProcessor.isKeepAlive() && responseProcessor.isKeepAlive());
        } finally {
            close();
        }
    }

    protected abstract SocketBean getSocketBean() throws IOException;

    protected abstract void close() throws IOException;


    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static final byte[] chunkedEndByte = "0\r\n\r\n".getBytes(StandardCharsets.UTF_8);

    //数据传输使用
    private void mutual(SocketInputStream socketInputStream, int contentLength, OutputStream os, boolean chunked) throws IOException {
        os.write(socketInputStream.byteBuffer);
        LOGGER.debug("write:" + new String(socketInputStream.byteBuffer));
        if (chunked) {
            byte[] bytes = new byte[1024];
            byte[] subArray = null;
            int by = 0;
            do {
                by = socketInputStream.is.read(bytes);
                os.write(bytes, 0, by);
                //LOGGER.debug("write:" + new String(bytes));
                subArray = ArrayUtils.subarray(bytes, by - 5, by);
            } while (!Arrays.equals(subArray, chunkedEndByte));
        } else {
            int readSize = socketInputStream.getCount() - socketInputStream.getPos();
            if (contentLength > 0) {
                byte[] bytes = new byte[4096];
                int by;
                do {
                    by = socketInputStream.is.read(bytes);
                    readSize += by;
                    os.write(bytes, 0, by);
                    LOGGER.debug("write:" + new String(bytes));
                } while (readSize < contentLength);
            }
        }

    }

}
