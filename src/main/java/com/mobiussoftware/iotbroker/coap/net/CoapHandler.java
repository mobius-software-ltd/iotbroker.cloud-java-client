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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.mobius.software.coap.parser.tlv.CoapMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;

public class CoapHandler extends SimpleChannelInboundHandler<CoapMessage>
{

	private ConnectionListener<CoapMessage> listener;

	public CoapHandler(ConnectionListener<CoapMessage> listener)
	{
		this.listener = listener;
	}

	@Override 
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, CoapMessage message) throws Exception
	{
		if (this.listener != null)
			this.listener.packetReceived(message);
	}

	@Override 
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		listener.connectionLost();
	}

	@Override 
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
	{
		ctx.flush();
	}
}
