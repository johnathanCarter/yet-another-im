package com.method.im.component.netty;

import com.method.im.pojo.enums.MsgActionEnum;
import com.method.im.service.UserService;
import com.method.im.service.UserServiceImpl;
import com.method.im.utils.JsonUtils;
import com.method.im.utils.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理消息的自定义handler
 * TextWebSocketFrame:在websocket中处理文本，frame是消息载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private String asShortText;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {

        // 1.获取客户端发来的消息
        System.out.println("received data: " + msg.text());
        String content = msg.text();
        Channel currentChannel = ctx.channel();
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        assert dataContent != null;
        int action = dataContent.getAction();
        // 2.判断消息的类型,根据不同的类型来处理不同的业务
        if (action == MsgActionEnum.CONNECT.type) {
            // 2.1websocket第一次连接的时候,初始化channel,把客户端channel和用户的id关联起来
            String senderId = dataContent.getMsgBean().getSenderId();
            UserChannelRel.put(senderId, currentChannel);

            // 测试
            for (Channel channels:users) {
                System.out.println("channel: " + channels.id().asLongText());
            }
            UserChannelRel.output();

        } else if (action == MsgActionEnum.CHAT.type) {
            // 2.2聊天类型的消息,把聊天记录保存到数据库,同时标记消息的签收状态[未签收]
            MsgBean chatMsg = dataContent.getMsgBean();
            //String contentText = chatMsg.getMsg();
            //String senderId = chatMsg.getSenderId();
            String receivedId = chatMsg.getReceiverId();

            // 保存消息到数据库,标记为未签收
            UserService userService = (UserServiceImpl) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            // 发送消息
            Channel receiverChannel = UserChannelRel.get(receivedId);
            if (receiverChannel == null) {
                // TODO 对方未上线,推送消息
            } else {
                // 对方在线,发消息,从ChannelGroup里查找对应的channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    System.out.println("发送消息");
                    // 对方在线,发消息
                    DataContent dataContentToClient = new DataContent();
                    dataContentToClient.setMsgBean(chatMsg);
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContentToClient)));
                } else {
                    // 对方离线
                    // TODO 推送
                }
            }

        } else if (action == MsgActionEnum.SIGNED.type) {
            // 2.3签收消息类型,针对具体的消息进行签收,修改数据库中对应消息的签收状态[已签收]
            UserService userService = (UserServiceImpl) SpringUtil.getBean("userServiceImpl");
            String msgIdsStr = dataContent.getExtend(); // 需要进行签收的消息id,逗号相隔
            String[] msgIds = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String id : msgIds) {
                if (StringUtils.isNotBlank(id)) {
                    msgIdList.add(id);
                }
            }
            //System.out.println(msgIdList.toString());
            if (!msgIdList.isEmpty()) {
                // 批量签收消息
                userService.updateSighedMsg(msgIdList);
            }

        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            // 2.4心跳类型的消息
            System.out.println("收到来自channel: " + currentChannel + " 的心跳包");
        } else {
            // TODO: 2022/5/14 nothing
        }


//        System.out.println(text);
//        for (Channel channel: users) { // 所有设备识别码
//            System.out.println(channel.id().asShortText());
//        }
        //users.writeAndFlush(new TextWebSocketFrame("response: " + LocalDateTime.now()  + " msg: " + text));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());  //记录每个客户端的channel
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("channel removed: " + channelId);
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常后关闭连接(channel), 接着从ChannelGroup中移除用户
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
