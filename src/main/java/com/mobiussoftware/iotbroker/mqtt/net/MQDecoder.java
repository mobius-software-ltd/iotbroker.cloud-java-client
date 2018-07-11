package com.mobiussoftware.iotbroker.mqtt.net;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.exceptions.MalformedMessageException;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MQDecoder extends ByteToMessageDecoder
{
	private MQParser parser;

	public MQDecoder()
	{
	}

	public MQDecoder(MQParser parser)
	{
		this.parser = parser;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws MalformedMessageException, UnsupportedEncodingException
	{
		ByteBuf nextHeader = null;
		do
		{
			if (buf.readableBytes() > 1)
			{
				try
				{
					nextHeader = MQParser.next(buf);
				}
				catch (MalformedMessageException | IndexOutOfBoundsException ex)
				{
					buf.resetReaderIndex();
					if (nextHeader != null)
						nextHeader = null;
				}
			}

			if (nextHeader != null)
			{
				buf.readBytes(nextHeader, nextHeader.capacity());
				try
				{
					MQMessage header = parser.decodeUsingCache(nextHeader);
					out.add(header);
				}
				catch (Exception e)
				{
					buf.resetReaderIndex();
					ctx.channel().pipeline().remove(this);
					throw e;
				}
				finally
				{
					nextHeader.release();
				}
			}
		}
		while (buf.readableBytes() > 1 && nextHeader != null);
	}

	public void setParser(MQParser parser)
	{
		this.parser = parser;
	}
	
}
