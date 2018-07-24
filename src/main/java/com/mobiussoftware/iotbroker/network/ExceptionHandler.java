package com.mobiussoftware.iotbroker.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ExceptionHandler extends ChannelDuplexHandler {
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (ctx.channel().isOpen())
			ctx.channel().closeFuture();
	}
}