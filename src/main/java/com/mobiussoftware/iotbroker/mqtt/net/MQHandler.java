package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MQHandler extends SimpleChannelInboundHandler<MQMessage> {

	private ConnectionListener<MQMessage> listener;

	public MQHandler(ConnectionListener<MQMessage> listener)
	{
		this.listener = listener;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, MQMessage mqMessage) throws Exception {
		if (this.listener != null)
			this.listener.packetReceived(mqMessage);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}
}
