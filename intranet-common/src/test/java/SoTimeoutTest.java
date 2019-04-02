import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SoTimeoutTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        new TestThread().start();
        createSocket();
    }

    static class TestThread extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = createServerSocket(1);
                while (true) {
                    Socket accept = serverSocket.accept();
                    System.out.println("1");
                    accept.setSoTimeout(100);
                    //Thread.sleep(2000);
                    InputStream inputStream = accept.getInputStream();
                    System.out.println("2");
                    int read = inputStream.read();
                    System.out.println(read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ServerSocket createServerSocket(int backlog) throws IOException {
            ServerSocket serverSocket = new ServerSocket(7890, backlog);
            return serverSocket;
        }
    }

    static void createSocket() throws IOException, InterruptedException {
        Socket localhost = new Socket("localhost", 7890);
        OutputStream outputStream = localhost.getOutputStream();
        Thread.sleep(200);
        outputStream.write(1);
    }
}
