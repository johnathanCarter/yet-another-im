package com.method.im.mapper;

import com.method.im.pojo.ChatMsg;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ChatMsgMapperExtend extends Mapper<ChatMsg> {

    void batchUpdateSighedMsg(List<String> msgIdList);

}
