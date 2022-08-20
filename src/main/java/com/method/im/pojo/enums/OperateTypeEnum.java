package com.method.im.pojo.enums;

public enum OperateTypeEnum {
    IGNORE(0, "忽略"),
    PASS(1, "通过");

    public final Integer type;
    public final String msg;

    OperateTypeEnum(Integer type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Integer getType() {
        return type;
    }

    public static String getMsgByType(Integer type) {
        for (OperateTypeEnum operateType: OperateTypeEnum.values()) {
            if (operateType.getType() == type) {
                return operateType.msg;
            }
        }
        return null;
    }
}
