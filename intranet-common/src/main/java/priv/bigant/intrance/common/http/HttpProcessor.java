package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
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
        this.socket = socket;
    }


    private void process() {
        boolean ok = true;
        do {
            requestProcessor = new RequestProcessor(socket, config);
            requestProcessor.process();
            if (requestProcessor.isSendAck())
                //TODO send ACK service
                System.out.println("send ack service last");

            if (receiver == null) {
                try {
                    receiver = getSocketBean();
                } catch (IOException e) {
                    LOGGER.error("get socket bean", e);
                    //TODO mutual service
                    e.printStackTrace();
                    ok = false;
                }
            }

            if (receiver == null) {
                LOGGER.error("receiver is null error");
            }

            try {
                SocketMutual.mutual(requestProcessor.getInput(), requestProcessor.getContentLength(), receiver);
            } catch (IOException e) {
                LOGGER.error("request mutual error", e);
                //TODO mutual service
                e.printStackTrace();
                ok = false;
            }

            responseProcessor = new ResponseProcessor(receiver, config);
            responseProcessor.process();

            try {
                SocketMutual.mutual(responseProcessor.getInput(), responseProcessor.getContentLength(), socket);
            } catch (IOException e) {
                LOGGER.error("response mutual error", e);
                //TODO mutual service
                e.printStackTrace();
                ok = false;
            }

        } while (ok && requestProcessor.isKeepAlive() && responseProcessor.isKeepAlive());

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
        process();
    }

}
