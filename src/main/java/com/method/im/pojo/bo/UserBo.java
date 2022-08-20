package com.method.im.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端传到后端的用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBo {

    private String userId;

    private String faceData;

    private String nickname;

}
