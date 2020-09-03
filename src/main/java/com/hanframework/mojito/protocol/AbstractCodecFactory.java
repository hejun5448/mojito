package com.hanframework.mojito.protocol;

import com.hanframework.kit.thread.HanThreadPoolExecutor;
import com.hanframework.kit.thread.NamedThreadFactory;
import com.hanframework.mojito.client.Client;
import com.hanframework.mojito.client.handler.ClientPromiseHandler;
import com.hanframework.mojito.client.netty.MojitoNettyClient;
import com.hanframework.mojito.handler.MojitoChannelHandler;
import com.hanframework.mojito.handler.MojitoCoreHandler;
import com.hanframework.mojito.processor.Processor;
import com.hanframework.mojito.processor.RequestProcessor;
import com.hanframework.mojito.processor.ResponseProcessor;
import com.hanframework.mojito.protocol.mojito.model.RpcProtocolHeader;
import com.hanframework.mojito.server.Server;
import com.hanframework.mojito.server.handler.ServerHandler;
import com.hanframework.mojito.server.handler.SubServerHandler;
import com.hanframework.mojito.server.impl.NettyServer;

import java.util.Objects;
import java.util.concurrent.Executor;


/**
 * 1. 自己定义数据模型和服务端和客户端的处理逻辑(数据模型只要继承RpcProtocolHeader即可)
 * 2. 编码器和解码器使用默认即可,已经实现拆包和粘包问题,所以不用担心该问题
 *
 * @author liuxin
 * 2020-08-22 13:27
 */
public abstract class AbstractCodecFactory<T extends RpcProtocolHeader, R extends RpcProtocolHeader> implements CodecFactory<T, R> {

    private Executor executor = new HanThreadPoolExecutor(new NamedThreadFactory("mojimo")).getExecutory();

    private SubServerHandler<T, R> subServerHandler;

    private ClientPromiseHandler<T, R> clientPromiseHandler;

    private RequestProcessor<T>[] requestProcessors;

    private ResponseProcessor<R>[] responseProcessors;

    private Protocol<T, R> protocol;

    public AbstractCodecFactory(Protocol<T, R> protocol) {
        this.protocol = protocol;
    }

    public AbstractCodecFactory(Protocol<T, R> protocol, SubServerHandler<T, R> subServerHandler) {
        this.protocol = protocol;
        this.subServerHandler = subServerHandler;
        protocol.getServerHandler().initWrapper(subServerHandler);
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Protocol<T, R> getProtocol() {
        return protocol;
    }

    @Override
    public MojitoChannelHandler getRequestHandler() {
        return new MojitoCoreHandler(getProtocol());
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public ChannelDecoder getRequestDecoder() {
        return protocol.getRequestDecoder();
    }

    @Override
    public ChannelEncoder getResponseEncoder() {
        return protocol.getResponseEncoder();
    }

    @Override
    public Server getServer() {
        return new NettyServer(protocol);
    }

    @Override
    public Client<T, R> getClient() {
        return (Client<T, R>) new MojitoNettyClient(getProtocol());
    }

    @Override
    public Client<T, R> getClient(String remoteHost, int remotePort) throws Exception {
        Client<T, R> client = getClient();
        client.connect(remoteHost, remotePort);
        return client;
    }

    @Override
    public ServerHandler<T, R> getServerHandler() {
        ServerHandler<T, R> serverHandler = null;
        if (Objects.isNull(this.subServerHandler)) {
            serverHandler = protocol.getServerHandler();
            //报错
            //服务端的逻辑,交给用户自己去写,如果没有实现doServerHandler要去setServerHandler
        }
        return serverHandler;
    }


    public ClientPromiseHandler<T, R> getClientPromiseHandler() {
        if (Objects.isNull(this.clientPromiseHandler)) {
            this.clientPromiseHandler = protocol.getClientPromiseHandler();
            if (this.clientPromiseHandler instanceof Processor) {
                Processor<T, R> processor = (Processor<T, R>) this.clientPromiseHandler;
                processor.setRequestProcessor(this.requestProcessors);
                processor.setResponseProcessor(this.responseProcessors);
            }

        }
        return this.clientPromiseHandler;
    }

    public void setServerHandler(SubServerHandler<T, R> subServerHandler) {
        this.subServerHandler = subServerHandler;
    }

    @Override
    public void setServerHandler(ServerHandler<T, R> serverHandler) {
        //修改协议内容的口子
        protocol.setServerHandler(serverHandler);
    }

    public void setClientPromiseHandler(ClientPromiseHandler<T, R> clientPromiseHandler) {
        this.clientPromiseHandler = clientPromiseHandler;
    }

    @Override
    public void setRequestProcessor(RequestProcessor<T>[] requestProcessors) {
        this.requestProcessors = requestProcessors;
    }

    public void setResponseProcessor(ResponseProcessor<R>[] responseProcessors) {
        this.responseProcessors = responseProcessors;
    }
}