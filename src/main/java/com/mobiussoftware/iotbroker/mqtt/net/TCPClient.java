package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


import java.net.InetSocketAddress;

public class TCPClient implements NetworkChannel<MQMessage> {
	private InetSocketAddress address;
	private int workerThreads;

	private Bootstrap bootstrap;
	private MultithreadEventLoopGroup loopGroup;
	private Channel channel;
//	private ChannelFuture channelConnect;

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

	public void close() {
		if (channel != null) {
			channel.closeFuture();
			channel = null;
		}
		if (loopGroup != null) {
			loopGroup.shutdownGracefully();
			loopGroup = null;
		}
	}

	public Boolean init(final ConnectionListener<MQMessage> listener) {
		System.out.println("init started, channel=" + channel);
		if (channel == null) {
			bootstrap = new Bootstrap();
			loopGroup = new NioEventLoopGroup(workerThreads);
			bootstrap.group(loopGroup);
			bootstrap.channel(NioSocketChannel.class);
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
				final ChannelFuture future = bootstrap.connect();
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						try {
							channel = future.channel();
							System.out.println("operation complete, channel is open=" + channel.isOpen());
						} catch (Exception e) {
							listener.connectFailed();
							return;
						}

						if (channel != null) {
							System.out.println("channel established?");
							listener.connected();
						}
						else {
							System.out.println("channel failed");
							listener.connectFailed();
						}
					}

				});
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

//	public boolean init(final ConnectionListener<MQMessage> listener) {
//		if (channel == null) {
//
//
//
//			bootstrap = new Bootstrap();
//			loopGroup = new NioEventLoopGroup(workerThreads);
//			bootstrap.group(loopGroup);
//			bootstrap.channel(NioSocketChannel.class);
//			bootstrap.option(ChannelOption.TCP_NODELAY, true);
//			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
//
//			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
//				@Override
//				protected void initChannel(SocketChannel socketChannel) throws Exception {
//
//					socketChannel.pipeline().addLast(new MQDecoder());
//					socketChannel.pipeline().addLast("handler", new MQHandler(listener));
//					socketChannel.pipeline().addLast(new MQEncoder());
//					socketChannel.pipeline().addLast(new ExceptionHandler());
//				}
//			});
//			bootstrap.remoteAddress(address);
//
//			try {
//				channelConnect = bootstrap.connect().sync();
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//				return false;
//			}
//			catch (Exception ex) {
//				ex.printStackTrace();
//				return false;
//			}
//		}
//
//		listener.connected();
//
//		return true;
//	}


	public Boolean isConnected() {
		return channel != null;
	}

	@Override
	public void send(MQMessage message) {
		System.out.println("channel is " + channel + ", isOpen=" + channel.isOpen());
		if (channel != null /*&& channel.isOpen()*/) {
			System.out.println("message " + message + " is being sent");
			channel.writeAndFlush(message);
		}
	}

}
