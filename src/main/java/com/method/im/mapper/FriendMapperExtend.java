package com.method.im.mapper;

import com.method.im.pojo.Friend;
import com.method.im.pojo.vo.FriendListVo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface FriendMapperExtend extends Mapper<Friend> {

    List<FriendListVo> queryFriendList(String userId);

}
