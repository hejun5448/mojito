package com.hanframework.mojito.handler;


import com.hanframework.mojito.channel.EnhanceChannel;
import com.hanframework.mojito.exception.RemotingException;
import com.hanframework.mojito.signature.service.SignatureManager;

/**
 * 逻辑层尽量不在netty的API中进行调用,而在自己定义的处理器中处理
 *
 * @author liuxin
 * @version Id: ChannelHandler.java, v 0.1 2019-05-11 14:47
 */
public interface MojitoChannelHandler {

    /**
     * 服务端
     */
    MojitoChannelHandler server();

    /**
     * 客户端
     */
    MojitoChannelHandler client();

    /**
     * 接受连接
     *
     * @param channel channel.
     * @throws RemotingException 远程调用异常
     */
    void connected(EnhanceChannel channel) throws RemotingException;

    /**
     * 断开连接
     *
     * @param channel channel.
     * @throws RemotingException 远程调用异常
     */
    void disconnected(EnhanceChannel channel) throws RemotingException;

    /**
     * 发送消息
     *
     * @param channel channel.
     * @param message message.
     * @throws RemotingException 远程调用异常
     */
    void write(EnhanceChannel channel, Object message) throws RemotingException;

    /**
     * 接受消息
     *
     * @param channel channel.
     * @param message message.
     * @throws RemotingException 远程调用异常
     */
    void read(EnhanceChannel channel, Object message) throws RemotingException;

    /**
     * 捕捉异常
     *
     * @param channel   channel.
     * @param exception exception.
     * @throws RemotingException 远程调用异常
     */
    void caught(EnhanceChannel channel, Throwable exception) throws RemotingException;

}
