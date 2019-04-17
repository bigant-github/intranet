package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public abstract class HttpProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpProcessor.class);

    protected Socket socket;
    protected Config config;
    protected RequestProcessor requestProcessor;
    protected ResponseProcessor responseProcessor;
    protected Socket receiver;

    public HttpProcessor(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
    }

    public HttpProcessor(Socket socket) {
        this.config = Config.getConfig();
        this.socket = socket;
    }


    private void process() throws IOException {
        do {
            requestProcessor = new RequestProcessor(socket, config);
            requestProcessor.process();
            if (requestProcessor.isSendAck())
                //TODO send ACK service
                LOGGER.warn("send ack service last");

            if (receiver == null) {
                try {
                    receiver = getSocketBean();
                } catch (IOException e) {
                    LOGGER.error("get socket bean", e);
                    throw e;
                    //TODO mutual service
                }

                if (receiver == null) {
                    LOGGER.error("receiver is null error");
                }
            }

            try {
                mutual(requestProcessor.getInput(), requestProcessor.getContentLength(), receiver);
            } catch (IOException e) {
                LOGGER.error("request mutual error", e);
                //TODO mutual service
                throw e;
            }

            responseProcessor = new ResponseProcessor(receiver, config);
            responseProcessor.process();
            try {
                mutual(responseProcessor.getInput(), responseProcessor.getContentLength(), socket);
            } catch (IOException e) {
                LOGGER.error("response mutual error", e);
                //TODO mutual service
                throw e;
            }
            if (requestProcessor.isKeepAlive() && responseProcessor.isKeepAlive()) {
                LOGGER.debug("保持连接");
            }
        } while (requestProcessor.isKeepAlive() && responseProcessor.isKeepAlive());

        try {
            close();
        } catch (Throwable e) {
            LOGGER.error("process close", e);
            e.printStackTrace();
        }
        receiver = null;
        socket = null;
    }

    protected abstract Socket getSocketBean() throws IOException;

    protected abstract void close() throws IOException;


    @Override
    public void run() {
        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void mutual(SocketInputStream socketInputStream, int contentLength, Socket socket) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(socketInputStream.byteBuffer);
        LOGGER.debug("write:" + new String(socketInputStream.byteBuffer));
        byte[] bytes = new byte[1024];
        int readSize = socketInputStream.getCount() - socketInputStream.getPos();
        if (contentLength > 0) {
            int by;
            do {
                by = socketInputStream.is.read(bytes);
                readSize += by;
                os.write(bytes, 0, by);
                LOGGER.debug("write:" + new String(bytes));
            } while (readSize < contentLength);
        }
        System.out.println();
    }
}
