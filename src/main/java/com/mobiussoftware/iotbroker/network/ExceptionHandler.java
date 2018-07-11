package com.mobiussoftware.iotbroker.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public class ExceptionHandler extends ChannelDuplexHandler {
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		InetSocketAddress address =(InetSocketAddress) ctx.channel().remoteAddress();
		if (ctx.channel().isOpen())
			ctx.channel().closeFuture();
	}
}