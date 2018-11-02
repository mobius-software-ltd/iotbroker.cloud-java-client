package com.mobiussoftware.iotbroker.mqtt.net;

import java.net.InetSocketAddress;
import java.net.URI;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.TLSHelper;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslHandler;

public class WSSClient extends WSClient
{
	private String certPath;
	private String certPwd;
	
	public WSSClient(InetSocketAddress address, int workerThreads, String certPath, String certPwd)
	{
		super(address, workerThreads);
		this.certPath = certPath;
		this.certPwd = certPwd;
	}
	
	@Override
	protected ChannelInitializer<SocketChannel> getChannelInitializer(final ConnectionListener<MQMessage> listener)
	{
		WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(this.getUri(), WebSocketVersion.V13, null, false, EmptyHttpHeaders.INSTANCE, 1280000);
		final WebSocketClientHandler handler = new WebSocketClientHandler(this, handshaker, listener);

		return new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws Exception
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("ssl", new SslHandler(TLSHelper.getClientEngine(certPath, certPwd)));
				pipeline.addLast("http-codec", new HttpClientCodec());
				pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
				pipeline.addLast("ws-handler", handler);
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}
	
	@Override
	protected URI getUri()
	{
		String type = "wss";
		String url = type + "://" + this.address.getHostName() + ":" + String.valueOf(this.address.getPort()) + "/" + type;
		URI uri;
		try
		{
			uri = new URI(url);
		}
		catch (Exception e)
		{
			return null;
		}
		return uri;
	}
}