package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MQEncoder extends MessageToByteEncoder<MQMessage> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, MQMessage message, ByteBuf output) throws Exception {
		ByteBuf buf = MQParser.encode(message);
		output.writeBytes(buf);
	}
}
