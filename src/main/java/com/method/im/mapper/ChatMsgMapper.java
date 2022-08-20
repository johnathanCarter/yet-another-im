package com.method.im.mapper;

import com.method.im.pojo.ChatMsg;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ChatMsgMapper extends Mapper<ChatMsg> {
}
