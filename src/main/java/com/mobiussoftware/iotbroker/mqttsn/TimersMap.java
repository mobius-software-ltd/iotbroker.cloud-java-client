package com.mobiussoftware.iotbroker.mqttsn;

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
import com.mobius.software.mqttsn.parser.avps.SNType;
import com.mobius.software.mqttsn.parser.packet.api.CountableMessage;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobius.software.mqttsn.parser.packet.impl.SNPingreq;
import com.mobiussoftware.iotbroker.mqttsn.net.UDPClient;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimersMap implements TimersMapInterface<SNMessage>
{
	private static int MAX_VALUE = 65535;
	private static int MIN_VALUE = 1;

	private UDPClient listener;
	private long resendPeriod;
	private long keepalivePeriod;

	private ConcurrentHashMap<Integer, MessageResendTimer<SNMessage>> timersMap = new ConcurrentHashMap<>();
	private AtomicInteger packetIDCounter = new AtomicInteger(MIN_VALUE);

	private MessageResendTimer<SNMessage> pingTimer;
	private MessageResendTimer<SNMessage> connectTimer;

	private ScheduledExecutorService scheduledservice = Executors.newScheduledThreadPool(5);

	public TimersMap(UDPClient listener, long resendPeriod, long keepalivePeriod)
	{
		this.listener = listener;
		this.resendPeriod = resendPeriod;
		this.keepalivePeriod = keepalivePeriod;
	}

	public void store(SNMessage message)
	{
		Boolean isConnect = false;
		if (message.getType() == SNType.CONNECT)
			isConnect = true;
		MessageResendTimer<SNMessage> timer = new MessageResendTimer<SNMessage>(message, listener, this, isConnect);
		Boolean added = false;
		Integer packetID = null;
		if (message instanceof CountableMessage)
		{
			if (((CountableMessage) message).getMessageID() == 0)
			{
				while (!added)
				{
					packetID = packetIDCounter.incrementAndGet() % MAX_VALUE;
					if(packetID>=MIN_VALUE)
					{
						MessageResendTimer<SNMessage> previous=timersMap.putIfAbsent(packetID, timer);
						if(previous==null)
							added = true;
					}
				}

				CountableMessage countable = (CountableMessage) message;
				countable.setMessageID(packetID);
			}
			else
			{
				packetID = ((CountableMessage) message).getMessageID();
				timersMap.put(packetID, timer);
			}

			ScheduledFuture<?> timer_future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
			timer.setFuture(timer_future);
		}
	}

	@Override 
	public void refreshTimer(MessageResendTimer<SNMessage> timer)
	{
		ScheduledFuture<?> future = null;
		switch (timer.getMessage().getType())
		{
			case PINGREQ:
				future = scheduledservice.schedule(timer, keepalivePeriod, TimeUnit.MILLISECONDS);
				break;
			default:
				future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
				break;
		}

		timer.setFuture(future);
	}

	public SNMessage remove(Integer packetID)
	{
		return cancelTimer(timersMap.remove(packetID));
	}

	public void stopAllTimers()
	{

		cancelConnectTimer();
		cancelTimer(pingTimer);

		Iterator<Entry<Integer, MessageResendTimer<SNMessage>>> iterator = timersMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			cancelTimer(iterator.next().getValue());
			iterator.remove();
		}
	}

	public void storeConnectTimer(SNMessage message)
	{
		cancelConnectTimer();

		connectTimer = new MessageResendTimer<>(message, listener, this, true);
		ScheduledFuture<?> future = scheduledservice.schedule(connectTimer, resendPeriod, TimeUnit.MILLISECONDS);
		connectTimer.setFuture(future);
	}

	@Override 
	public void cancelConnectTimer()
	{
		cancelTimer(connectTimer);
	}

	private SNMessage cancelTimer(MessageResendTimer<SNMessage> timer)
	{
		SNMessage pendingMessage = null;
		if (timer != null)
		{
			timer.getFuture().cancel(true);
			pendingMessage = timer.getMessage();
		}
		return pendingMessage;
	}

	public MessageResendTimer<SNMessage> getConnectTimer()
	{
		return connectTimer;
	}

	public void startPingTimer()
	{
		cancelTimer(pingTimer);

		pingTimer = new MessageResendTimer<>(new SNPingreq(), listener, this, false);
		ScheduledFuture<?> future = scheduledservice.schedule(pingTimer, keepalivePeriod, TimeUnit.MILLISECONDS);
		pingTimer.setFuture(future);
	}
}