package com.mobiussoftware.iotbroker.coap.net;

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
import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

import com.mobius.software.coap.parser.CoapParser;
import com.mobius.software.coap.parser.tlv.CoapMessage;

public class CoapEncoder extends MessageToMessageEncoder<AddressedEnvelope<CoapMessage, InetSocketAddress>>
{
	@Override 
	protected void encode(ChannelHandlerContext context, AddressedEnvelope<CoapMessage, InetSocketAddress> message, List<Object> output) throws Exception
	{
		ByteBuf buffer = CoapParser.encode(message.content());
		output.add(new DatagramPacket(buffer, message.recipient()));
	}
}
