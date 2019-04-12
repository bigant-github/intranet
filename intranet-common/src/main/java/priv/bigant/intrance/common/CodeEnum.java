package priv.bigant.intrance.common;

public enum CodeEnum {


    SUCCESS("10000", "success");

    public static final String SPLIT = "-";
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

}
