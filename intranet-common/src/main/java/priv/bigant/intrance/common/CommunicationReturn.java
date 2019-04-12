package priv.bigant.intrance.common;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class CommunicationReturn {

    protected JSONObject jsonObject = new JSONObject();

    public void add(String key, String value) {
        jsonObject.put(key, value);
    }

    public void add(CommunicationP communicationP) throws Exception {
        Map<String, Object> stringObjectMap = obj2Map(communicationP);
        jsonObject.putAll(stringObjectMap);
    }

    public String get(String key) {
        return String.valueOf(jsonObject.get(key));
    }

    public Map<String, Object> obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    public byte[] toByte() {
        return jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8);
    }

    public interface CommunicationP {

    }

}
