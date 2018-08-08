package com.mobiussoftware.iotbroker.mqtt_sn.net;

import com.mobius.software.mqttsn.parser.Parser;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class SnEncoder
		extends MessageToMessageEncoder<AddressedEnvelope<SNMessage, InetSocketAddress>>
{
	@Override protected void encode(ChannelHandlerContext context, AddressedEnvelope<SNMessage, InetSocketAddress> message, List<Object> output)
			throws Exception
	{
		ByteBuf buffer = Parser.encode(message.content());
		output.add(new DatagramPacket(buffer, message.sender(), message.recipient()));
	}
}
