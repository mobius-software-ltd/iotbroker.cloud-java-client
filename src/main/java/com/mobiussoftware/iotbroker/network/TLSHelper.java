package com.mobiussoftware.iotbroker.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class TLSHelper
{
	private static final Logger logger = Logger.getLogger(TLSHelper.class);

	public static boolean isPasswordValid(String ksContent, String ksPassword)
	{
		try (PEMParser reader = new PEMParser(new StringReader(ksContent)))
		{
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);

			Object o;
			while ((o = reader.readObject()) != null)
			{				
				if (o instanceof PEMEncryptedKeyPair)
				{
					try
					{
						PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) o;
						PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(ksPassword.toCharArray());
						converter.getKeyPair(ckp.decryptKeyPair(decProv));
					}
					catch (Throwable e)
					{
						logger.warn("error reading pem key:" + e.getMessage(), e);
						return false;
					}
				}
			}
		}
		catch (IOException e)
		{
			logger.warn("error reading pem key:" + e.getMessage(), e);
			return false;
		}

		return true;
	}

	public static KeyStore getKeyStore(String ksContent, String ksPassword) throws Exception
	{
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.insertProviderAt(new BouncyCastleProvider(), 0);

		PEMParser reader = new PEMParser(new StringReader(ksContent));
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
		JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();

		KeyPair kp = null;
		List<X509Certificate> certificates = new ArrayList<>();

		Object o;
		while ((o = reader.readObject()) != null)
		{
			if (o instanceof PEMEncryptedKeyPair)
			{
				// Encrypted key - we will use provided password
				PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) o;
				PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(ksPassword.toCharArray());
				kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
			}
			else if (o instanceof PEMKeyPair)
				kp = converter.getKeyPair((PEMKeyPair) o);
			else if (o instanceof X509CertificateHolder)
				certificates.add(certConverter.getCertificate((X509CertificateHolder) o));
		}

		reader.close();
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null, null);

		X509Certificate[] chain = new X509Certificate[certificates.size()];
		int index = 0;
		for (X509Certificate curr : certificates)
		{
			ks.setCertificateEntry(curr.getSubjectX500Principal().getName(), curr);
			chain[index++] = curr;
		}

		if (kp != null)
			ks.setKeyEntry("main", kp.getPrivate(), ksPassword.toCharArray(), chain);

		return ks;
	}

	public static SSLEngine getClientEngine(String ksContent, String ksPassword) throws Exception
	{
		SslContext ctx;
		if (ksContent.isEmpty())
			ctx = initEmptyContext();
		else
		{
			KeyStore ks = getKeyStore(ksContent, ksPassword);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, ksPassword.toCharArray());

			ctx = SslContextBuilder.forClient().keyManager(kmf).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		}

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
