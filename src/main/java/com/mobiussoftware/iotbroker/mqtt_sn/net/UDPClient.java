package com.mobiussoftware.iotbroker.mqtt_sn.net;

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
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPClient implements NetworkChannel<SNMessage>
{
	protected final Logger logger = Logger.getLogger(getClass());

	protected InetSocketAddress address;
	protected int workerThreads;

	protected Bootstrap bootstrap;
	protected MultithreadEventLoopGroup loopGroup;
	protected Channel channel;

	public UDPClient(InetSocketAddress address, int workerThreads)
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

	public boolean init(final ConnectionListener<SNMessage> listener)
	{
		if (channel == null)
		{
			loopGroup = new NioEventLoopGroup(workerThreads);
			bootstrap = new Bootstrap();
			bootstrap.group(loopGroup);
			bootstrap.channel(NioDatagramChannel.class);
			bootstrap.handler(getChannelInitializer(listener));
			bootstrap.remoteAddress(address);
			try
			{
				final ChannelFuture future = bootstrap.connect();
				future.addListener(getChannelFutureListener(listener, future));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	protected ChannelInitializer<DatagramChannel> getChannelInitializer(final ConnectionListener<SNMessage> listener) 
	{
		return new ChannelInitializer<DatagramChannel>()
		{
			@Override 
			public void initChannel(DatagramChannel ch) throws InterruptedException
			{
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new SnDecoder());
				pipeline.addLast("handler", new SnHandler(listener));
				pipeline.addLast(new SnEncoder());
				pipeline.addLast(new ExceptionHandler());
			}
		};
	}
	
	protected ChannelFutureListener getChannelFutureListener(final ConnectionListener<SNMessage> listener, final ChannelFuture future) 
	{
		return new ChannelFutureListener()
		{
			@Override 
			public void operationComplete(ChannelFuture channelFuture) throws Exception
			{
				try
				{
					channel = future.channel();
					if (isConnected())
						listener.connected();
					else
						listener.connectFailed();
				}
				catch (Exception e)
				{
					listener.connectFailed();
					return;
				}
			}
		};
	}
	
	public boolean isConnected()
	{
		return channel != null && channel.isOpen();
	}

	@Override
	public void send(SNMessage message)
	{
		if (isConnected())
		{
			logger.info("message " + message + " is being sent");
			channel.writeAndFlush(new DefaultAddressedEnvelope<SNMessage,InetSocketAddress>(message, address));
		}
	}
}
