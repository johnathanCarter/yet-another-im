package com.method.im.component.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 监听channel心跳handler
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        
        // 判断evt是否为IdleStateEvent (用于触发用户事件, 包含 读空闲/写空闲/读写空闲)
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("进入读空闲...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("进入写空闲...");
            } else if (event.state() == IdleState.ALL_IDLE) {
                //int size = ChatHandler.users.size();
                System.out.println("before closing: " + ChatHandler.users.size());
                Channel channel =  ctx.channel();
                channel.close(); // 客户端进入飞行模式,关闭此Channel
                System.out.println("after closing: " + ChatHandler.users.size());
            }

        }
    }
    
}
