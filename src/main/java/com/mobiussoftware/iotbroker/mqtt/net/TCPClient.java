package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

public class TCPClient implements NetworkChannel<MQMessage> {
	private InetSocketAddress address;
	private int workerThreads;

	private Bootstrap bootstrap;
	private NioEventLoopGroup loopGroup;
	private Channel channel;

	// handlers for client connections
	public TCPClient(InetSocketAddress address, int workerThreads) {
		this.address = address;
		this.workerThreads = workerThreads;
	}

	public void shutdown() {
		if (channel != null) {
			channel.closeFuture();
			channel = null;
		}

		if (loopGroup != null)
			loopGroup.shutdownGracefully();
	}

	public void Close() {
		if (channel != null) {
			channel.closeFuture();
			channel = null;
		}
		if (loopGroup != null) {
			loopGroup.shutdownGracefully();
			loopGroup = null;
		}
	}

	public Boolean Init(final ConnectionListener<MQMessage> listener) {
		if (channel == null) {
			bootstrap = new Bootstrap();
			loopGroup = new NioEventLoopGroup(workerThreads);//MultithreadEventLoopGroup(workerThreads);
			bootstrap.group(loopGroup);
			bootstrap.channel(SocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws InterruptedException {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new MQDecoder());
					pipeline.addLast("handler", new MQHandler(listener));
					pipeline.addLast(new MQEncoder());
					pipeline.addLast(new ExceptionHandler());
				}
			});
			bootstrap.remoteAddress(address);

			try {
				final ChannelFuture future = bootstrap.connect().awaitUninterruptibly();
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						try {
							channel = future.channel();
						} catch (Exception e) {
							listener.connectFailed();
							return;
						}

						if (channel != null)
							listener.connected();
						else
							listener.connectFailed();
					}

				});
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	public Boolean isConnected() {
		return channel != null;
	}

	@Override
	public void send(MQMessage message) {
		if (channel != null && channel.isOpen())
			channel.writeAndFlush(message);
	}

}
