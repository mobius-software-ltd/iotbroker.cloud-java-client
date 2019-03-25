package com.mobiussoftware.iotbroker.network;

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
import java.util.concurrent.ScheduledFuture;

public class MessageResendTimer<T> implements Runnable
{
	private static int MAX_CONNECT_RESEND = 3;

	private T message;
	private NetworkChannel<T> client;
	private TimersMapInterface<T> timersMap;
	private Integer retriesLeft = null;
	private ScheduledFuture<?> future;

	private boolean isConnect;

	public MessageResendTimer(T message, NetworkChannel<T> client, TimersMapInterface<T> timersMap, boolean isConnect)
	{
		this.isConnect = isConnect;

		if (isConnect)
			retriesLeft = MAX_CONNECT_RESEND;

		this.message = message;
		this.client = client;
		this.timersMap = timersMap;
	}

	@Override
	public void run()
	{
		onTimedEvent();
	}

	public void onTimedEvent()
	{
		if (retriesLeft != null)
		{
			if (--retriesLeft == 0)
			{
				timersMap.cancelConnectTimer();
				return;
			}
		}

		client.send(message);
		timersMap.refreshTimer(this);
	}

	public T getMessage()
	{
		return message;
	}

	public ScheduledFuture<?> getFuture()
	{
		return future;
	}

	public void setFuture(ScheduledFuture<?> future)
	{
		this.future = future;
	}

	public boolean isConnect()
	{
		return isConnect;
	}
}
