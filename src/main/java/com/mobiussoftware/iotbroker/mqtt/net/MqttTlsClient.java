package com.mobiussoftware.iotbroker.mqtt.net;

import java.net.InetSocketAddress;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.TLSHelper;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

public class MqttTlsClient extends TCPClient
{
	private String certPath;
	private String certPwd;

	public MqttTlsClient(InetSocketAddress address, int workerThreads, String certPath, String certPwd)
	{
		super(address, workerThreads);
		this.certPath = certPath;
		this.certPwd = certPwd;
	}

	@Override
	protected ChannelInitializer<SocketChannel> getChannelInitializer(final ConnectionListener<MQMessage> listener)
	{
		return new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws Exception
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("ssl", new SslHandler(TLSHelper.getClientEngine(certPath, certPwd)));
				pipeline.addLast(new MQDecoder());
				pipeline.addLast("handler", new MQHandler(listener));
				pipeline.addLast(new MQEncoder());
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}
}
