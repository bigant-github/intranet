import priv.bigant.intrance.common.coyote.Response;
import priv.bigant.intrance.common.coyote.http11.Http11ResponseInputBuffer;
import priv.bigant.intrance.common.util.http.parser.HttpParser;
import priv.bigant.intrance.common.util.net.NioChannel;
import priv.bigant.intrance.common.util.net.NioSocketWrapper;
import priv.bigant.intrance.common.util.net.SocketBufferHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ResponseTest {
    private static String chunkedHttp = "GET / HTTP/1.1\r\n" +
            "Host: ccc.asdf.com\r\n" +
            "Connection: keep-alive\r\n" +
            "Cache-Control: max-age=0\r\n" +
            "Upgrade-Insecure-Requests: 1\r\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\n" +
            "Accept-Encoding: gzip, deflate\r\n" +
            "Accept-Language: zh-CN,zh;q=0.9\r\n" +
            "Cookie: UM_distinctid=16a531054665f6-08653ea956f516-e323069-232800-16a531054676dc; Hm_lvt_3bd9d3afc0f5cf8f7bfe4db04ba6487b=1556171741; CNZZDATA1271206451=174289155-1556167354-%7C1556170057; sidebar_collapsed=false\r\n" +
            "\r\n";

    public static void main(String[] args) throws IOException {
        SocketChannel open = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
        ByteBuffer allocate = ByteBuffer.allocate(4096);
        allocate.put(chunkedHttp.getBytes());
        open.write(allocate);

        Response response = new Response();
        HttpParser httpParser = new HttpParser(null, null);
        Http11ResponseInputBuffer http11ResponseInputBuffer = new Http11ResponseInputBuffer(response, 1026, httpParser);

        /*Request response = new Request();
        HttpParser httpParser = new HttpParser(null, null);
        Http11InputBuffer http11ResponseInputBuffer = new Http11InputBuffer(response, 1026, false, httpParser);*/

        NioChannel nioChannel = new NioChannel(open, new SocketBufferHandler(2048, 2048, true));
        NioSocketWrapper nioSocketWrapper = null;//new NioSocketWrapper(nioChannel, nioEndpoint);
        http11ResponseInputBuffer.init(nioSocketWrapper);

        boolean b = http11ResponseInputBuffer.parseResponseLine(false);
        boolean b1 = http11ResponseInputBuffer.parseHeaders();
        ByteBuffer byteBuffer = http11ResponseInputBuffer.getByteBuffer();
        while (byteBuffer.position() < byteBuffer.limit()) {
            byteBuffer.get();
        }
        byteBuffer.position(0);
        int read = nioSocketWrapper.read(false, byteBuffer);
        System.out.println(response);
    }
}
