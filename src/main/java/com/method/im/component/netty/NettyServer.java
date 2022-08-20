package com.method.im.component.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class NettyServer {

    /**
     * 获取NettyServer的单例
     */
    private static class SingletonNettyServer {
        static final NettyServer instance = new NettyServer();
    }

    public static NettyServer getInstance() {
        return SingletonNettyServer.instance;
    }

    private EventLoopGroup masterGroup;
    private EventLoopGroup slaveGroup;
    private ServerBootstrap serverBootstrap;

    public NettyServer() {
        // 定义一对主线程组，接收客户端的连接
        masterGroup = new NioEventLoopGroup();
        // 从线程组
        slaveGroup = new NioEventLoopGroup();
        // 服务端启动类
        serverBootstrap = new ServerBootstrap();
        // 把主从线程组放进服务端启动类中
        serverBootstrap.group(masterGroup, slaveGroup)
                       .channel(NioServerSocketChannel.class)       //设置NIO的双向通道
                       .childHandler(new ChatServerInitializer());      // 针对从线程组操作              
    }

    public void start() {
        // 启动server
        serverBootstrap.bind(8088);
        System.err.println("netty start...");
    }

//    /**
//     * a netty example
//     * @param args
//     * @throws InterruptedException
//     */
//    public static void main(String[] args) throws InterruptedException {
//        // 定义一对主线程组，接收客户端的连接
//        EventLoopGroup masterGroup = new NioEventLoopGroup();
//        // 从线程组
//        EventLoopGroup slaveGroup = new NioEventLoopGroup();
//
//        try {
//            // 服务端启动类
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            // 把主从线程组放进服务端启动类中
//            serverBootstrap.group(masterGroup, slaveGroup)
//                    .channel(NioServerSocketChannel.class)   //设置NIO的双向通道
//                    .childHandler(new ChatServerInitializer());      // 针对从线程组操作
//
//            // 启动server，方式同步
//            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();
//            channelFuture.channel().closeFuture().sync();// 获取某一个客户端所监听的管道并关闭
//        } finally {
//            masterGroup.shutdownGracefully();
//            slaveGroup.shutdownGracefully();
//        }
//    }
}
