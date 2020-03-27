package priv.bigant.test.Integral;

public class HttpTest {
    private static String chunkedHttp = "GET /test HTTP/1.1\r\n" +
            "Host: www.bigant.club\r\n" +
            "Accept-Encoding: gzip\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "\r\n" +
            "b\r\n" +
            "01234567890\r\n" +
            "5\r\n" +
            "12345\r\n" +
            "0\r\n" +
            "\r\n";


}
