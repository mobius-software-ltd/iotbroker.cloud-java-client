package com.mobiussoftware.iotbroker.mqtt_sn.net;

import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobiussoftware.iotbroker.mqtt.net.MQDecoder;
import com.mobiussoftware.iotbroker.mqtt.net.MQEncoder;
import com.mobiussoftware.iotbroker.mqtt.net.MQHandler;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ExceptionHandler;
import com.mobiussoftware.iotbroker.network.NetworkChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class UDPClient implements NetworkChannel<SNMessage> {

	private final Logger logger = Logger.getLogger(getClass());

	private InetSocketAddress address;
	private int workerThreads;

	private Bootstrap bootstrap;
	private MultithreadEventLoopGroup loopGroup;
	private Channel channel;

	public UDPClient(InetSocketAddress address, int workerThreads) {
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

	public Boolean init(final ConnectionListener<SNMessage> listener) {
		if (channel == null) {
			bootstrap = new Bootstrap();
			loopGroup = new NioEventLoopGroup(workerThreads);
			bootstrap.group(loopGroup);
			bootstrap.channel(NioDatagramChannel.class);

			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws InterruptedException {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast(new SnDecoder());
					pipeline.addLast("handler", new SnHandler(listener));
					pipeline.addLast(new SnEncoder());
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
						} catch (Exception e) {
							listener.connectFailed();
							return;
						}

						if (isConnected()) {
							listener.connected();
						} else {
							listener.connectFailed();
						}
					}

				});
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	public Boolean isConnected() {
		return channel != null && channel.isOpen();
	}

	@Override
	public void send(SNMessage message) {
		if (isConnected()) {
			logger.info("message " + message + " is being sent");
			channel.writeAndFlush(new DefaultAddressedEnvelope(message, address));
		}
	}
}
