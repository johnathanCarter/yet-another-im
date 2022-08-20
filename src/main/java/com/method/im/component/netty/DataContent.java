package com.method.im.component.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataContent implements Serializable {

    private static final long serialVersionUID = 5566722374308407521L;
    private int action; // 动作类型
    private MsgBean msgBean; // 用户聊天内容
    private String extend; // 扩展字段

}
