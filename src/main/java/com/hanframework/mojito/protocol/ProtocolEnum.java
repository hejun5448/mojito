package com.hanframework.mojito.protocol;

import com.hanframework.mojito.protocol.http.HttpProtocol;
import com.hanframework.mojito.protocol.mojito.MojitoProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能优先级
 * 1. ProtostuffObjectSerializer
 * 2. NettyCompactObjectSerializer
 * 3. Hession2ObjectSerializer
 * 4. NettyObjectSerializer
 * 5. HessionObjectSerializer
 */

public enum ProtocolEnum {

    /**
     * ECHO协议
     */
    ECHO(1, null),

    /**
     * 注册协议
     */
    MQ_REG(3, null),

    /**
     * 注册协议-发消息
     */
    MQ_SEND(4, null),

    /**
     * mojito协议
     */
    MOJITO(2, MojitoProtocol.class),

    /**
     * http、https协议
     */
    HTTP(5, HttpProtocol.class);

    private byte type;

    private Class<? extends Protocol> protocol;

    private static Map<Byte, ProtocolEnum> cache = new HashMap<>();

    static {
        for (ProtocolEnum protocolEnum : values()) {
            cache.put(protocolEnum.type, protocolEnum);
        }
    }

    public static ProtocolEnum byType(byte type) {
        return cache.get(type);
    }

    ProtocolEnum(int type, Class<? extends Protocol> protocol) {
        this.type = (byte) type;
        this.protocol = protocol;
    }

    public byte getType() {
        return type;
    }

    public Class<? extends Protocol> getProtocol() {
        return protocol;
    }
}
