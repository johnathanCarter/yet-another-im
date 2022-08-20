package com.method.im.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传到前端的好友列表实体类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendListVo {

    private String sendUserId;

    private String sendUsername;

    private String sendFaceImage;

    private String sendFaceImageBig;

    private String sendNickname;

}