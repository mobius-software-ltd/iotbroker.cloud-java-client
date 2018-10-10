package com.mobiussoftware.iotbroker.mqtt.net;

import com.mobius.software.mqtt.parser.MQJsonParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.network.ConnectionListener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> 
{
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final ConnectionListener<MQMessage> listener;
    private WSClient client;
    
    public WebSocketClientHandler(WSClient client,WebSocketClientHandshaker handshaker, ConnectionListener<MQMessage> listener) 
    {
    	this.client=client;
        this.handshaker = handshaker;
        this.listener = listener;
    }

    public ChannelFuture handshakeFuture() 
    {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) 
    {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) 
    {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) 
    {
        listener.connectionLost();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception 
    {

        final Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) 
        {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) 
        {
            final FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        final WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame) 
        {
        	MQJsonParser parser=client.getParser();
        	try
        	{
        		byte[] bytes = new byte[frame.content().readableBytes()];
        		frame.content().readBytes(bytes);
        		MQMessage message=parser.decode(bytes);
        		this.listener.packetReceived(message);
        	}
        	catch(Exception ex)
        	{
        		throw ex;
        	}
        	finally
        	{
        		client.releaseParser(parser);
        	}        	
        } 
        else if (frame instanceof CloseWebSocketFrame)         
            ch.close();        

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception 
    {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) 
            handshakeFuture.setFailure(cause);
        
        ctx.close();
    }
}