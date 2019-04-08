import org.apache.commons.lang3.ArrayUtils;

public class ArrayTest {
    public static void main(String[] args) {
        byte[] bytes = new byte[1];
        byte[] bytes1 = {1, 2, 3};
        byte[] bytes2 = ArrayUtils.addAll(bytes1, bytes);
        System.out.println(bytes2.length + "" + bytes1.length);
    }
}
