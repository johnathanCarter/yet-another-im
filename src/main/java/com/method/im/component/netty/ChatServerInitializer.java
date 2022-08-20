package com.method.im.component.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 初始化器，channel注册后，会执行里面的初始化方法: channel--->pipeline--->multiple handlers
 * received(inbound) data traverses along the handlers in an order they added on the pipeline
 * send(outbound) doing the exact opposite
 */
public class ChatServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // 通过channel去获取对应的管道
        ChannelPipeline pipeline = channel.pipeline();

        //为管道添加handler
        //1.http编解码需要的handler
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        //2.对写大数据流的支持
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
        //3.对httpMessage进行聚合
        pipeline.addLast("HttpObjectAggregator", new HttpObjectAggregator(1024*64));
        //3.5增加心跳的支持
        //先激活空闲状态
        //针对客户端,如果在1分钟时间没有向服务端发送读写心跳,则主动断开,如果是读或写空闲不做处理
        pipeline.addLast(new IdleStateHandler(8, 10, 12));
        //自定义空闲状态检测
        pipeline.addLast(new HeartbeatHandler());
        //4.websocket服务器处理的协议，用于指定给客户端连接访问的路由/ws
        //websocket是以frame来进行传输数据的,不同的数据类型对应不同的frame
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast("ExampleHandler", new ChatHandler());
    }
}
