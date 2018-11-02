package com.mobiussoftware.iotbroker.mqtt;

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
import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.CountableMessage;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Pingreq;
import com.mobius.software.mqtt.parser.header.impl.Pubrel;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.NetworkChannel;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimersMap implements TimersMapInterface<MQMessage>
{
	private static int MAX_VALUE = 65535;
	private static int MIN_VALUE = 1;

	private NetworkChannel<MQMessage> listener;
	private long resendPeriod;
	private long keepalivePeriod;

	private ConcurrentHashMap<Integer, MessageResendTimer<MQMessage>> timersMap = new ConcurrentHashMap<>();
	private AtomicInteger packetIDCounter = new AtomicInteger(MIN_VALUE);

	private MessageResendTimer<MQMessage> pingTimer;
	private MessageResendTimer<MQMessage> connectTimer;

	private ScheduledExecutorService scheduledservice = Executors.newScheduledThreadPool(5);

	public TimersMap(NetworkChannel<MQMessage> listener, long resendPeriod, long keepalivePeriod)
	{
		this.listener = listener;
		this.resendPeriod = resendPeriod;
		this.keepalivePeriod = keepalivePeriod;
	}

	public void store(MQMessage message)
	{
		Boolean isConnect = false;
		if (message.getType() == MessageType.CONNECT)
			isConnect = true;
		MessageResendTimer<MQMessage> timer = new MessageResendTimer<MQMessage>(message, listener, this, isConnect);
		Boolean added = false;
		Integer packetID = null;
		if (message instanceof CountableMessage)
		{
			if (((CountableMessage) message).getPacketID() == null)
			{
				while (!added)
				{
					packetID = packetIDCounter.incrementAndGet() % MAX_VALUE;
					if (packetID >= MIN_VALUE)
					{
						MessageResendTimer<MQMessage> previous = timersMap.putIfAbsent(packetID, timer);
						if (previous == null)
							added = true;
					}
				}

				CountableMessage countable = (CountableMessage) message;
				countable.setPacketID(packetID);

			}
			else
			{
				packetID = ((CountableMessage) message).getPacketID();
				timersMap.put(packetID, timer);
			}

			ScheduledFuture<?> timer_future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
			timer.setFuture(timer_future);
		}
	}

	public void store(Pubrel pubrel)
	{
		MessageResendTimer<MQMessage> timer = new MessageResendTimer<MQMessage>(pubrel, listener, this, false);
		timersMap.put(pubrel.getPacketID(), timer);
	}

	@Override
	public void refreshTimer(MessageResendTimer<MQMessage> timer)
	{
		ScheduledFuture<?> timer_future = null;
		switch (timer.getMessage().getType())
		{
		case PINGREQ:
			timer_future = (ScheduledFuture<?>) scheduledservice.schedule(timer, keepalivePeriod, TimeUnit.MILLISECONDS);
			break;
		default:
			timer_future = (ScheduledFuture<?>) scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
			break;
		}

		timer.setFuture(timer_future);
	}

	public MQMessage remove(Integer packetID)
	{
		MessageResendTimer<MQMessage> timer = timersMap.remove(packetID);
		if (timer == null)
			return null;
		return cancelTimer(timer);
	}

	public void stopAllTimers()
	{

		cancelConnectTimer();
		cancelTimer(pingTimer);

		Iterator<Entry<Integer, MessageResendTimer<MQMessage>>> iterator = timersMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			cancelTimer(iterator.next().getValue());
			iterator.remove();
		}
	}

	public void storeConnectTimer(MQMessage message)
	{
		if (connectTimer != null)
			connectTimer.getFuture().cancel(true);

		connectTimer = new MessageResendTimer<MQMessage>(message, listener, this, true);
		ScheduledFuture<?> connectTimer_future = (ScheduledFuture<?>) scheduledservice.schedule(connectTimer, resendPeriod, TimeUnit.MILLISECONDS);
		connectTimer.setFuture(connectTimer_future);
	}

	@Override
	public void cancelConnectTimer()
	{
		cancelTimer(connectTimer);
	}

	private MQMessage cancelTimer(MessageResendTimer<MQMessage> timer)
	{
		MQMessage pendingMessage = null;
		if (timer != null && timer.getFuture() != null)
		{
			timer.getFuture().cancel(true);
			pendingMessage = timer.getMessage();
			timer = null;
		}
		return pendingMessage;
	}

	public MessageResendTimer<MQMessage> getConnectTimer()
	{
		return connectTimer;
	}

	public void startPingTimer()
	{

		cancelTimer(pingTimer);

		pingTimer = new MessageResendTimer<MQMessage>(new Pingreq(), listener, this, false);
		ScheduledFuture<?> pingTimer_future = (ScheduledFuture<?>) scheduledservice.schedule(pingTimer, keepalivePeriod, TimeUnit.MILLISECONDS);
		pingTimer.setFuture(pingTimer_future);
	}
}
