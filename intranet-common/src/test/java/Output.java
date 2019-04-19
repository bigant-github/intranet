import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Output {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(12344);

        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12344);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.close();
                socket.close();
                System.out.println("123123");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).run();

        Socket accept = serverSocket.accept();

        while (true) {
            OutputStream outputStream = accept.getOutputStream();
            System.out.println(accept.isOutputShutdown());
            outputStream.write("asdfasdf".getBytes());
            System.out.println("vvvv");
        }

       /* InputStream inputStream = accept.getInputStream();
        int read = inputStream.read();
        System.out.println("bbbb");


        accept.sendUrgentData(0);*/
    }


}
