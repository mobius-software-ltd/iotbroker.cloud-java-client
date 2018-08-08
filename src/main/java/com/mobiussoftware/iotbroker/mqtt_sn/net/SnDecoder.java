package com.mobiussoftware.iotbroker.mqtt_sn.net;

import com.mobius.software.mqttsn.parser.Parser;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class SnDecoder extends MessageToMessageDecoder<DatagramPacket> {
	@Override
	protected void decode(ChannelHandlerContext context, DatagramPacket message, List<Object> output) throws Exception {
		SNMessage result = Parser.decode(message.content());
		output.add(result);
	}
}
