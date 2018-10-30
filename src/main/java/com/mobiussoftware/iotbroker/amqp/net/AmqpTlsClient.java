package com.mobiussoftware.iotbroker.amqp.net;

import java.net.InetSocketAddress;

import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.TLSHelper;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

public class AmqpTlsClient extends TCPClient
{
	private String certPath;
	private String certPwd;
	
	public AmqpTlsClient(InetSocketAddress address, int workerThreads, String certPath, String certPwd)
	{
		super(address, workerThreads);
		this.certPath = certPath;
		this.certPwd = certPwd;
	}

	@Override
	protected ChannelInitializer<SocketChannel> getChannelInitializer(final ConnectionListener<AMQPHeader> listener)
	{
		return new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws Exception
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("ssl", new SslHandler(TLSHelper.getClientEngine(certPath, certPwd)));
				pipeline.addLast(new AMQPDecoder());
				pipeline.addLast("handler", new AMQPHandler(listener));
				pipeline.addLast(new AMQPEncoder());
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}
}
