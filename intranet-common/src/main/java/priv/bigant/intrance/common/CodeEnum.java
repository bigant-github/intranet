package priv.bigant.intrance.common;

public enum CodeEnum {


    SUCCESS("10000", "success"),

    //HTTP 系列
    HOST_ALREADY_EXIST("11001", "域名已被使用"),


    ERROR("20000", "错误");

    private String code;
    private String msg;

    static CodeEnum getEnum(String code) {
        for (CodeEnum codeEnum : values()) {
            if (codeEnum.code.equals(code))
                return codeEnum;
        }
        return null;
    }

    CodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
