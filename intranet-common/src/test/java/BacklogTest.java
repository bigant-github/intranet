import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BacklogTest {
    public static void main(String[] args) throws IOException {
        createServerSocket(2);
        createSocket();
    }

    static void createServerSocket(int backlog) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7890, backlog);
    }

    static void createSocket() throws IOException {
        int num = 0;
        while (true) {
            System.out.println(++num);
            new Socket("localhost", 7890);
        }
    }
}
