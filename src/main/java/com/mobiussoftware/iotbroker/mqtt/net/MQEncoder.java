package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class MQEncoder extends MessageToByteEncoder<MQMessage> {
	private MQParser parser;

	public MQEncoder() {
	}

	public MQEncoder(MQParser parser) {
		this.parser = parser;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MQMessage message, ByteBuf out) throws Exception {
		ByteBuf buf = MQParser.encode(message);
		out.writeBytes(buf);
		try {
			parser.getCache().returnMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setParser(MQParser parser) {
		this.parser = parser;
	}
}