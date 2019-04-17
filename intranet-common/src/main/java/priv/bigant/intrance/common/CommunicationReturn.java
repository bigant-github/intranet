package priv.bigant.intrance.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class CommunicationReturn {

    protected JSONObject jsonObject = new JSONObject();

    protected CommunicationReturn() {
        SerializeConfig config = new SerializeConfig();
        config.configEnumAsJavaBean(CodeEnum.class, CommunicationEnum.class);
    }

    public void add(String key, String value) {
        jsonObject.put(key, value);
    }

    public void add(CommunicationP communicationP) throws Exception {
        String s = JSONObject.toJSONString(communicationP);
        Map<String, Object> jsonObject = JSONObject.parseObject(s).getInnerMap();
        this.jsonObject.putAll(jsonObject);
    }

    public String get(String key) {
        return String.valueOf(jsonObject.get(key));
    }

    public <T> T toJavaObject(Class<T> clazz) {
        return jsonObject.toJavaObject(clazz);
    }

    public byte[] toByte() {
        return jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8);
    }

    public interface CommunicationP {

    }

}


