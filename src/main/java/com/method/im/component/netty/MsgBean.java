package com.method.im.component.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgBean implements Serializable {

    private static final long serialVersionUID = -7059799010711071533L;
    private String senderId;
    private String receiverId;
    private String msg;
    private String msgId; // 用于消息的签收

}
