package com.mobiussoftware.iotbroker.mqtt.net;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobius.software.mqtt.parser.MQJsonParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public class WSClient implements NetworkChannel<MQMessage>
{
	protected final Logger logger = Logger.getLogger(getClass());

	protected InetSocketAddress address;
	protected int workerThreads;

	protected Bootstrap bootstrap;
	protected MultithreadEventLoopGroup loopGroup;
	protected Channel channel;

	protected ConcurrentLinkedQueue<MQJsonParser> parsers = new ConcurrentLinkedQueue<>();

	// handlers for client connections
	public WSClient(InetSocketAddress address, int workerThreads)
	{
		this.address = address;
		this.workerThreads = workerThreads;
	}

	public void shutdown()
	{

		if (channel != null)
		{
			channel.closeFuture();
			channel = null;
		}

		if (loopGroup != null)
			loopGroup.shutdownGracefully();
	}

	public void close()
	{
		if (channel != null)
		{
			channel.closeFuture();
			channel = null;
		}
		if (loopGroup != null)
		{
			loopGroup.shutdownGracefully();
			loopGroup = null;
		}
	}

	public boolean init(final ConnectionListener<MQMessage> listener)
	{
		if (channel == null)
		{
			bootstrap = new Bootstrap();
			loopGroup = new NioEventLoopGroup(workerThreads);
			bootstrap.group(loopGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			bootstrap.handler(getChannelInitializer(listener));
			bootstrap.remoteAddress(address);
			try
			{
				final ChannelFuture future = bootstrap.connect();
				future.addListener(new ChannelFutureListener()
				{
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception
					{
						try
						{
							channel = future.channel();
						}
						catch (Exception e)
						{
							listener.connectFailed();
							return;
						}

						if (isConnected())
						{
							listener.connected();
						}
						else
						{
							listener.connectFailed();
						}
					}

				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	protected ChannelInitializer<SocketChannel> getChannelInitializer(final ConnectionListener<MQMessage> listener)
	{
		WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(this.getUri(), WebSocketVersion.V13, null, false, EmptyHttpHeaders.INSTANCE, 1280000);
		final WebSocketClientHandler handler = new WebSocketClientHandler(this, handshaker, listener);

		return new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws InterruptedException
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("http-codec", new HttpClientCodec());
				pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
				pipeline.addLast("ws-handler", handler);
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}

	public boolean isConnected()
	{
		return channel != null && channel.isOpen();
	}

	@Override
	public void send(MQMessage message)
	{
		if (isConnected())
		{
			logger.info("message " + message + " is being sent");
			String string = null;
			MQJsonParser parser = getParser();
			try
			{
				string = parser.jsonString(message);
			}
			catch (JsonProcessingException e)
			{
				e.printStackTrace();
			}

			releaseParser(parser);
			channel.writeAndFlush(new TextWebSocketFrame(string));
		}
	}

	protected MQJsonParser getParser()
	{
		MQJsonParser parser = parsers.poll();
		if (parser == null)
			parser = new MQJsonParser();

		return parser;
	}

	public void releaseParser(MQJsonParser parser)
	{
		this.parsers.offer(parser);
	}

	protected URI getUri()
	{
		String type = "ws";
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