package com.method.im.pojo.enums;

/**
 * 消息签收状态 枚举
 */
public enum  MsgSignFlagEnum {
    UNSIGNED(0, "未签收"),
    SIGNED(1, "已签收");

    public final Integer type;
    public final String content;

    MsgSignFlagEnum(Integer type, String content) {
        this.type = type;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public String getContent(Integer type) {
        for (MsgSignFlagEnum msgSignFlagEnum: MsgSignFlagEnum.values()) {
            if (msgSignFlagEnum.getType() == type) {
                return msgSignFlagEnum.content;
            }
        }
        return null;
    }
}
