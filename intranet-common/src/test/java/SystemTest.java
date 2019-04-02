import java.util.Properties;
import java.util.TreeSet;

public class SystemTest {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        Properties properties = System.getProperties();
        TreeSet<Object> objects = new TreeSet<>();
        objects.addAll(properties.keySet());
        objects.forEach(x -> {
            System.out.println(x + "   " + System.getProperty(x.toString()));
        });
    }
}
