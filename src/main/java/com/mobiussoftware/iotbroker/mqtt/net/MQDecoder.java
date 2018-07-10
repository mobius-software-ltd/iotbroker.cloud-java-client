package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.exceptions.MalformedMessageException;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MQDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf input, List<Object> output)
	{
		ByteBuf nextHeader = null;
		do
		{
			if (input.readableBytes() > 1)
			{
				try
				{
					nextHeader = MQParser.next(input);
				}
				catch (Exception ex)
				{
					if (ex instanceof MalformedMessageException || ex instanceof IndexOutOfBoundsException)
					{
						input.resetReaderIndex();
						if (nextHeader != null)
						{
							((ByteBuf) nextHeader).release();
							nextHeader = null;
						}
					}
                        else
					throw ex;
				}
			}

			if (nextHeader != null)
			{
				input.readBytes(nextHeader, nextHeader.capacity());
				try
				{
					MQMessage header = MQParser.decode(nextHeader);
					output.add(header);
				}
				catch (Exception e)
				{
					input.resetReaderIndex();
					context.channel().pipeline().remove(this);
					throw e;
				}
				finally
				{
					nextHeader.release();
				}
			}
		}
		while (input.readableBytes() > 1 && nextHeader != null);
	}
}

