package com.hanframework.mojito.config;

import com.hanframework.mojito.channel.EnhanceChannel;
import com.hanframework.mojito.client.Client;
import com.hanframework.mojito.exception.RemotingException;
import com.hanframework.mojito.future.MojitoFuture;
import com.hanframework.mojito.future.listener.MojitoListener;
import com.hanframework.mojito.protocol.CodecFactory;
import com.hanframework.mojito.protocol.mojito.model.RpcProtocolHeader;
import com.hanframework.mojito.protocol.mojito.model.RpcRequest;
import com.hanframework.mojito.protocol.mojito.model.RpcResponse;
import com.hanframework.mojito.server.handler.SubServerHandler;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 点对点模式
 * - 支持同步和异步
 *
 * @author liuxin
 * 2020-08-23 21:34
 */
public class InstallerTest implements Serializable {

    class RpcUserRequest extends RpcProtocolHeader {
        private String message;

        public RpcUserRequest(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "RpcUserRequest{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }

    class RpcUserResponse extends RpcProtocolHeader {
        private String message;

        public RpcUserResponse(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "RpcUserResponse{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }


    @Test
    public void mojitoClientTest() throws Exception {
        //使用不匹配的模型,服务端会报错,直接断开连接。
        //TODO 是否服务端可以报错误信息返回给客户端?
        CodecFactory<RpcRequest, RpcResponse> codecFactory = Installer.modules(RpcRequest.class, RpcResponse.class).create();
        Client<RpcRequest, RpcResponse> client = codecFactory.getClient("127.0.0.1", 12306);
        MojitoFuture<RpcResponse> async = client.sendAsync(new RpcRequest());
        System.out.println(async.get(5, TimeUnit.SECONDS));
        client.close();
    }


    @Test
    public void mojitoServerTest() {
        Installer.server(RpcRequest.class, RpcResponse.class).serverHandler(new SubServerHandler<RpcRequest, RpcResponse>() {
            @Override
            public RpcResponse handler(EnhanceChannel channel, RpcRequest rpcRequest) throws RemotingException {
                RpcResponse response = new RpcResponse();
                response.setMessage("hello2");
                return response;
            }
        }).create().start(12306);


    }


    /**
     * 如何快速构建一个客户端
     * <p>
     * 长时间没有心跳,如何处理。自动重连还是放弃。
     *
     * @throws Exception
     */
    @Test
    public void clientTest() throws Exception {
        Client<RpcUserRequest, RpcUserResponse> client = Installer.client(RpcUserRequest.class, RpcUserResponse.class)
                .conncet("127.0.0.1", 12306);
        List<MojitoFuture<RpcUserResponse>> result = new ArrayList<>();
        StopWatch stopWatch = new StopWatch("请求验证");
        stopWatch.start();
        for (int i = 0; i < 10000; i++) {
            MojitoFuture<RpcUserResponse> async = client.sendAsync(new RpcUserRequest("I'am RpcUser_" + i));
            result.add(async);
        }
        stopWatch.stop();
        for (MojitoFuture<RpcUserResponse> rpcUserResponseMojitoFuture : result) {
            System.out.println(rpcUserResponseMojitoFuture.get());
        }
        System.out.println(stopWatch.prettyPrint());
        client.close();
    }

    /**
     * 如何快速创建一个服务端
     * 1. 序列化协议
     * 2. 是否池化
     * 3. 单线程还是多线程
     * 4. 小数据缓存等待还是直接发送
     * 配置信息
     *
     * @throws Exception
     */
    @Test
    public void serverTest() throws Exception {
        Installer.server(RpcUserRequest.class, RpcUserResponse.class)
                .serverHandler((channel, rpcRequest) -> new RpcUserResponse("服务端返回: " + rpcRequest.message))
                .create().start(12306);
    }

    @Test
    public void mqTest() {
        //1. 服务端放一个路由键。当有人往指定的routeKey消息。
        //2. 服务端将消息推送给订阅的人,然后将这个消息的状态指定为已发送状态,等待消费端来确定是否消费成功,如果成功就将消息状态改为已处理，然后移出。
        //3. 如何消费端没有消费成功,或者没有给通知。 定时之后将消息状态改为重试状态。


        //4. 消息的处理逻辑抽离出来。后面可以实现通过机器方式读取,类似kafka方式
    }


    public static void main(String[] args) {
        String str=new String("123");
        String str2 = "123";
        System.out.println(str.intern()==str2);
    }
}