package com.mobiussoftware.iotbroker.amqp.net;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

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
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TCPClient implements NetworkChannel<AMQPHeader>
{

	private final Logger logger = Logger.getLogger(getClass());

	private InetSocketAddress address;
	private int workerThreads;

	private Bootstrap bootstrap;
	private MultithreadEventLoopGroup loopGroup;
	private Channel channel;

	// handlers for client connections
	public TCPClient(InetSocketAddress address, int workerThreads)
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

	public boolean init(final ConnectionListener<AMQPHeader> listener)
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

	protected ChannelInitializer<SocketChannel> getChannelInitializer(final ConnectionListener<AMQPHeader> listener)
	{
		return new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws InterruptedException
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new AMQPDecoder());
				pipeline.addLast("handler", new AMQPHandler(listener));
				pipeline.addLast(new AMQPEncoder());
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}

	public boolean isConnected()
	{
		return channel != null && channel.isOpen();
	}

	@Override
	public void send(AMQPHeader message)
	{
		if (isConnected())
		{
			logger.info("message " + message + " is being sent");
			channel.writeAndFlush(message);
		}
	}

}
