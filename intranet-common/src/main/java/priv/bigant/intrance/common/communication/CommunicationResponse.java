package priv.bigant.intrance.common.communication;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;

public class CommunicationResponse extends CommunicationReturn {

    public CommunicationResponse(CodeEnum code) {
        jsonObject.put("code", code);
    }

    private CommunicationResponse() {
    }

    private CommunicationResponse(JSONObject jsonObject) {
        super.jsonObject = jsonObject;
    }

    public boolean isSuccess() {
        CommunicationResponseP communicationResponseP = jsonObject.toJavaObject(CommunicationResponseP.class);
        return communicationResponseP.code.equals(CodeEnum.SUCCESS);
    }

    public static CommunicationResponse createCommunicationResponse(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(s);
        return new CommunicationResponse(jsonObject);
    }

    public static CommunicationResponse createSuccess() {
        return new CommunicationResponse(CodeEnum.SUCCESS);
    }

    public static CommunicationResponse create(CodeEnum code) throws Exception {
        return new CommunicationResponse(code);
    }

    public static CommunicationResponse createCommunicationResponse(CommunicationP communicationP) throws Exception {
        CommunicationResponse communicationRequest = new CommunicationResponse();
        communicationRequest.add(communicationP);
        return communicationRequest;
    }

    public static class CommunicationResponseP implements CommunicationP {
        private CodeEnum code;

        public CommunicationResponseP(CodeEnum code) {
            this.code = code;
        }

        public CommunicationResponseP() {
        }

        public boolean isSuccess() {
            return code.equals(CodeEnum.SUCCESS);
        }

        public CodeEnum getCode() {
            return code;
        }

        public void setCode(CodeEnum code) {
            this.code = code;
        }
    }

    public static class CommunicationResponseHttpFirst extends CommunicationResponseP {

        private String msg;

        public CommunicationResponseHttpFirst(CodeEnum code) {
            super(code);
        }


    }

    public static class CommunicationResponseHttpAdd extends CommunicationResponseP {

        public CommunicationResponseHttpAdd() {
        }

        /**
         * 成功时使用
         *
         * @param id
         */
        public CommunicationResponseHttpAdd(String id) {
            super(CodeEnum.SUCCESS);
        }

        /**
         * 失败时使用
         *
         * @param codeEnum
         * @param id
         */
        public CommunicationResponseHttpAdd(CodeEnum codeEnum, String id) {
            super(codeEnum);
            this.id = id;
        }

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}