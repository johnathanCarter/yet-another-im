package com.method.im.mapper;

import com.method.im.pojo.User;
import com.method.im.pojo.vo.FriendRequestVo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface UserMapperExtend extends Mapper<User> {

    List<FriendRequestVo> queryFriendRequestList(String acceptUserId);

}
