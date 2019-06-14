
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpTest {
    static String http = "GET /images/logo.png HTTP/1.1\r\n" +
            "host:ccc.asdf.com\r\n" +
            "user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.366\r\n" +
            "accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b33\r\n" +
            "accept-encoding:gzip, deflatee\r\n" +
            "accept-language:zh-CN,zh;q=0.99\r\n" +
            "cache-control:max-age=00\r\n" +
            "cookie:UM_distinctid=16a531054665f6-08653ea956f516-e323069-232800-16a531054676dc; Hm_lvt_3bd9d3afc0f5cf8f7bfe4db04ba6487b=1556171741; CNZZDATA1271206451=174289155-1556167354-%7C1556170057; sidebar_collapsed=falsee\r\n" +
            "proxy-connection:keep-alivee\r\n" +
            "upgrade-insecure-requests:11\r\n" +
            "x-lantern-version:5.4.11\r\n" +
            "\r\n";

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 80);
        socket.getOutputStream().write(http.getBytes());

        byte[] bytes = new byte[4096];
        int read = socket.getInputStream().read(bytes);
        System.out.print(new String(bytes, StandardCharsets.ISO_8859_1));
    }
}
