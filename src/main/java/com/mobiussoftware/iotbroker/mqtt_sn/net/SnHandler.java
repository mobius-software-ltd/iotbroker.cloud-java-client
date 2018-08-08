package com.mobiussoftware.iotbroker.mqtt_sn.net;

import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SnHandler extends SimpleChannelInboundHandler<SNMessage> {

	private ConnectionListener<SNMessage> listener;

	public SnHandler(ConnectionListener<SNMessage> listener) {
		this.listener = listener;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, SNMessage message) throws Exception {
		if (this.listener != null)
			this.listener.packetReceived(message);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		listener.connectionLost();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
}
