package com.method.im.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后端传前端的用户实体类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {

    private String id;

    private String username;

    private String faceImage;

    private String faceImageBig;

    private String nickname;

    private String qrcode;

}
