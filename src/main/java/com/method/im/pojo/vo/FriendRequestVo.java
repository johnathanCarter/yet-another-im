package com.method.im.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友请求发送方的信息
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestVo {

    private String sendUserId;

    private String sendUsername;

    private String sendFaceImage;

    private String sendNickname;


}
