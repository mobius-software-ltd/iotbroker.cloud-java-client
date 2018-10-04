package com.mobiussoftware.iotbroker.amqp;

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
import com.mobius.software.amqp.parser.avps.HeaderCode;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.parser.header.impl.AMQPPing;
import com.mobius.software.amqp.parser.header.impl.AMQPTransfer;
import com.mobiussoftware.iotbroker.amqp.net.TCPClient;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class TimersMap implements TimersMapInterface<AMQPHeader>
{
	private static int MAX_VALUE = 65535;
	private static int MIN_VALUE = 1;

	private TCPClient listener;
	private long resendPeriod;
	private long keepalivePeriod;
	private AmqpClient client;

	private ConcurrentHashMap<Integer, MessageResendTimer<AMQPHeader>> timersMap = new ConcurrentHashMap<>();
	private int packetIDCounter = MIN_VALUE;

	private MessageResendTimer<AMQPHeader> pingTimer;
	private ServerConnectTimer<AMQPHeader> connectTimer;

	private ScheduledExecutorService scheduledservice = Executors.newScheduledThreadPool(5);

	public TimersMap(AmqpClient client, TCPClient listener, long resendPeriod, long keepalivePeriod)
	{
		this.client = client;
		this.listener = listener;
		this.resendPeriod = resendPeriod;
		this.keepalivePeriod = keepalivePeriod;
	}

	public void store(AMQPHeader message)
	{
		if(message.getCode()!=null && message.getCode() == HeaderCode.TRANSFER)
        {
			MessageResendTimer<AMQPHeader> timer = new MessageResendTimer<AMQPHeader>(message, listener, this, false);
			Boolean added = false;
			Integer packetID = null;
			if (message instanceof AMQPTransfer)
			{
				if (((AMQPTransfer) message).getDeliveryId() == null)
				{
					packetID = packetIDCounter;
					while (!added)
					{
	
						packetID = (packetID + 1) % MAX_VALUE;
						try
						{
							timersMap.put(packetID, timer);
							added = true;
						}
						catch (Exception ex)
						{
							// already exists
						}
					}
	
					AMQPTransfer transfer = (AMQPTransfer) message;
					transfer.setDeliveryId(packetID.longValue());
	
				}
				else
				{
					packetID = ((AMQPTransfer) message).getDeliveryId().intValue();
					timersMap.put(packetID, timer);
				}
	
				ScheduledFuture<?> timer_future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
				timer.setFuture(timer_future);
			}
        }
	}

	@Override 
	public void refreshTimer(MessageResendTimer<AMQPHeader> timer)
	{
		ScheduledFuture<?> timer_future = null;
		switch (timer.getMessage().getCode())
		{
			case PING:
				timer_future = (ScheduledFuture<?>) scheduledservice.schedule(timer, keepalivePeriod, TimeUnit.MILLISECONDS);
				break;
			default:
				timer_future = (ScheduledFuture<?>) scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
				break;
		}

		timer.setFuture(timer_future);
	}

	public AMQPHeader remove(Integer packetID)
	{
		return cancelTimer(timersMap.remove(packetID));
	}

	public void stopAllTimers()
	{

		cancelConnectTimer();
		cancelTimer(pingTimer);

		Iterator<Entry<Integer, MessageResendTimer<AMQPHeader>>> iterator = timersMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			cancelTimer(iterator.next().getValue());
			iterator.remove();
		}
	}

	public void storeConnectTimer()
    {
        if (connectTimer != null)
            connectTimer.stop();

        connectTimer = new ServerConnectTimer<AMQPHeader>(this,scheduledservice);
        connectTimer.execute(resendPeriod);
    }

	@Override 
	public void cancelConnectTimer()
	{
		if (connectTimer != null)
		{
            connectTimer.stop();
            client.cancelConnection();
		}
		
		connectTimer=null;
	}

	private AMQPHeader cancelTimer(MessageResendTimer<AMQPHeader> timer)
	{
		AMQPHeader pendingMessage = null;
		if (timer != null)
		{
			timer.getFuture().cancel(true);
			pendingMessage = timer.getMessage();
			timer = null;
		}
		return pendingMessage;
	}

	public ServerConnectTimer<AMQPHeader> getConnectTimer()
	{
		return connectTimer;
	}

	public void startPingTimer()
	{
		cancelTimer(pingTimer);

		pingTimer = new MessageResendTimer<AMQPHeader>(new AMQPPing(), listener, this, false);
		ScheduledFuture<?> pingTimer_future = (ScheduledFuture<?>) scheduledservice.schedule(pingTimer, keepalivePeriod, TimeUnit.MILLISECONDS);
		pingTimer.setFuture(pingTimer_future);
	}
}
