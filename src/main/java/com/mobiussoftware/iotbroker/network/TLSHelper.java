package com.mobiussoftware.iotbroker.network;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class TLSHelper
{
	public static SSLEngine getClientEngine(String ksPath, String ksPassword) throws Exception
	{
		SslContext ctx = getClientContext(ksPath, ksPassword);
		SSLEngine sslEngine = ctx.newEngine(ByteBufAllocator.DEFAULT);
		sslEngine.setUseClientMode(true);
		return sslEngine;
	}

	public static SslContext getClientContext(String ksPath, String ksPassword) throws Exception
	{
		if (ksPath.isEmpty())
			return initEmptyContext();
		else if (ksPath.endsWith("jks"))
			return initContextJks(ksPath, ksPassword);
		else if (ksPath.endsWith(".pfx"))
			return initContextPfx(ksPath, ksPassword);
		else
			throw new IllegalArgumentException("unsupported keystore " + ksPath);
	}

	private static SslContext initEmptyContext() throws Exception
	{
		return SslContextBuilder.forClient().keyManager(null).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	}

	private static SslContext initContextJks(String ksPath, String ksPassword) throws Exception
	{
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(ksPath), ksPassword.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, ksPassword.toCharArray());

		return SslContextBuilder.forClient().keyManager(kmf).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	}

	private static SslContext initContextPfx(String ksPath, String ksPassword) throws Exception
	{
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(new FileInputStream(ksPath), ksPassword.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, ksPassword.toCharArray());

		return SslContextBuilder.forClient().keyManager(kmf).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	}
}
