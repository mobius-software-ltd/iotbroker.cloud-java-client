package com.mobiussoftware.iotbroker.amqp;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.mobiussoftware.iotbroker.network.TimersMapInterface;

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
public class ServerConnectTimer<T> implements Runnable
{
	private TimersMapInterface<T> timersMap;
	private ScheduledFuture<?> future;
	private ScheduledExecutorService scheduledService;
	
    public ServerConnectTimer(TimersMapInterface<T> timersMap,ScheduledExecutorService scheduledService)
    {
        this.timersMap = timersMap;
        this.scheduledService = scheduledService;
    }

    public void execute(Long period)
    {
        if (future != null)
        {
        	future.cancel(true);
        	future = null;
        }

        future = (ScheduledFuture<?>) scheduledService.schedule(this, period, TimeUnit.MILLISECONDS);		
    }
    
	@Override
	public void run() 
	{
		onTimedEvent();
	}
    
	public void onTimedEvent()
    {
		if(future!=null)
			future.cancel(true);
		
		future = null;
        timersMap.cancelConnectTimer();
        return;
    }

    public void stop()
    {
        if (future != null)
        {
        	future.cancel(true);
        	future = null;
        }
    }
}