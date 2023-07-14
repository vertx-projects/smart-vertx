package com.smart.vertx.messagecodes;

import com.smart.vertx.messagecodes.model.ProtoCommonMsg;
import com.smart.vertx.messagecodes.util.ProtostuffUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author peng.bo
 * @date 2022/5/20 11:57
 * @desc 节省内存空间的序列化方式
 */
public class ProtoMessageCodec implements MessageCodec<ProtoCommonMsg, ProtoCommonMsg> {
    /**
     * 将消息实体封装到Buffer用于传输
     * 实现方式：使用对象流从对象中获取Byte数组然后追加到Buffer
     */
    @Override
    public void encodeToWire(Buffer buffer, ProtoCommonMsg protoCommonMsg) {
        buffer.appendBytes(ProtostuffUtils.serialize(protoCommonMsg));
    }

    //从Buffer中获取消息对象
    @Override
    public ProtoCommonMsg decodeFromWire(int pos, Buffer buffer) {
        return ProtostuffUtils.deserialize(buffer.getBytes(pos, buffer.length()), ProtoCommonMsg.class);
    }

    @Override
    public ProtoCommonMsg transform(ProtoCommonMsg protoCommonMsg) {
        return protoCommonMsg;
    }

    @Override
    public String name() {
        return "protoMessage";
    }

    //识别是否是用户自定义编解码器,通常为-1
    @Override
    public byte systemCodecID() {
        return -1;
    }
}
