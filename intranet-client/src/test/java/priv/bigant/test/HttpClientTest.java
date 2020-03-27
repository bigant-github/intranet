package priv.bigant.test;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class HttpClientTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        URL url = new URL("http://www.baidu.com");
        URLConnection urlConnection = url.openConnection();
        String headerField = urlConnection.getHeaderField("Content-Length");
        System.out.println(headerField);
        //int available = inputStream.available();
        //byte[] bytes = new byte[available];
        //int read = inputStream.read(bytes);
        //System.out.println(new String(bytes, StandardCharsets.UTF_8));
    }
}
