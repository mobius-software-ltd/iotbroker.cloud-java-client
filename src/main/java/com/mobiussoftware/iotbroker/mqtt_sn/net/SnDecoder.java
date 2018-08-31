package com.mobiussoftware.iotbroker.mqtt_sn.net;

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
import com.mobius.software.mqttsn.parser.Parser;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class SnDecoder extends MessageToMessageDecoder<DatagramPacket>
{
	@Override 
	protected void decode(ChannelHandlerContext context, DatagramPacket message, List<Object> output) throws Exception
	{
		SNMessage result = Parser.decode(message.content());
		output.add(result);
	}
}