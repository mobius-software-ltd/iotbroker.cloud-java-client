package com.mobiussoftware.iotbroker.mqttsn.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.crypto.tls.ProtocolVersion;

import com.mobius.software.iot.dal.crypto.AsyncDtlsClient;
import com.mobius.software.iot.dal.crypto.AsyncDtlsClientHandler;
import com.mobius.software.iot.dal.crypto.AsyncDtlsClientProtocol;
import com.mobius.software.iot.dal.crypto.DtlsStateHandler;
import com.mobius.software.iot.dal.crypto.HandshakeHandler;
import com.mobius.software.iot.dal.crypto.MessageType;
import com.mobius.software.mqttsn.parser.Parser;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.TLSHelper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;

public class SnDtlsClient extends UDPClient implements DtlsStateHandler
{
	private String certPath;
	private String certPwd;

	private AsyncDtlsClientProtocol protocol;

	private CountDownLatch handshakeLatch;

	public SnDtlsClient(InetSocketAddress address, int workerThreads, String certPath, String certPwd)
	{
		super(address, workerThreads);
		this.certPath = certPath;
		this.certPwd = certPwd;
	}

	@Override
	public boolean init(final ConnectionListener<SNMessage> listener)
	{
		handshakeLatch = new CountDownLatch(1);
		return super.init(listener);
	}

	@Override
	protected ChannelInitializer<DatagramChannel> getChannelInitializer(final ConnectionListener<SNMessage> listener)
	{
		final DtlsStateHandler client = this;
		return new ChannelInitializer<DatagramChannel>()
		{
			@Override
			public void initChannel(DatagramChannel socketChannel) throws Exception
			{

				AsyncDtlsClient dtlsClient = new AsyncDtlsClient(readKeystore(), certPwd, null);
				HandshakeHandler handshakeHandler = new HandshakeHandler()
				{
					@Override
					public void postProcessHandshake(MessageType messageType, ByteBuf data) throws IOException
					{
					}

					@Override
					public void handleHandshake(MessageType messageType, ByteBuf data) throws IOException
					{
					}
				};
				protocol = new AsyncDtlsClientProtocol(dtlsClient, new SecureRandom(), socketChannel, handshakeHandler, client, address, true, ProtocolVersion.DTLSv12);
				AsyncDtlsClientHandler sslhandler = new AsyncDtlsClientHandler(protocol, client);
				socketChannel.pipeline().addLast("ssl", sslhandler);
				socketChannel.pipeline().addLast("decoder", new SnDecoder());
				socketChannel.pipeline().addLast("handler", new SnHandler(listener));
			}
		};
	}

	@Override
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
					{
						protocol.initHandshake(null);
						awaitHandshake();
						listener.connected();
					}
					else
						listener.connectFailed();
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					listener.connectFailed();
				}
			}
		};
	}

	private boolean awaitHandshake()
	{
		try
		{
			boolean handshakeComplete = handshakeLatch.await(3, TimeUnit.SECONDS);
			if (!handshakeComplete)
				logger.warn("handshake didn't complete within timeout");
			return handshakeComplete;
		}
		catch (InterruptedException e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void handshakeStarted(InetSocketAddress address, Channel channel)
	{
	}

	@Override
	public void handshakeCompleted(InetSocketAddress address, Channel channel)
	{
		handshakeLatch.countDown();
	}

	@Override
	public void errorOccured(InetSocketAddress address, Channel channel)
	{
	}

	@Override
	public void send(SNMessage message)
	{
		ByteBuf buf = Parser.encode(message);
		try
		{
			protocol.sendPacket(buf);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private KeyStore readKeystore() throws Exception
	{
		KeyStore ks = TLSHelper.getKeyStore(certPath, certPwd);
		return ks;
	}
}
