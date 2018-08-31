package com.mobiussoftware.iotbroker.coap;

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
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.coap.parser.message.options.CoapOption;
import com.mobius.software.coap.parser.message.options.CoapOptionType;
import com.mobius.software.coap.parser.tlv.CoapCode;
import com.mobius.software.coap.parser.tlv.CoapMessage;
import com.mobius.software.coap.parser.tlv.CoapType;
import com.mobiussoftware.iotbroker.coap.net.UDPClient;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

public class TimersMap implements TimersMapInterface<CoapMessage> 
{
	 private static Integer MAX_VALUE = 65535;
     private static Integer MIN_VALUE = 1;

     private UDPClient _listener;
     private Long resendPeriod;
     private Long keepalivePeriod;
     private CoapClient _client;

     private ConcurrentHashMap<byte[], MessageResendTimer<CoapMessage>> timersMap = new ConcurrentHashMap<>();
 	 private AtomicInteger packetIDCounter = new AtomicInteger(MIN_VALUE);

     private MessageResendTimer<CoapMessage> pingTimer;
     private MessageResendTimer<CoapMessage> connectTimer;
     private ScheduledExecutorService scheduledservice = Executors.newScheduledThreadPool(5);

 	 public TimersMap(CoapClient client, UDPClient listener, Long resendPeriod,Long keepalivePeriod)
     {
         this._listener = listener;
         this.resendPeriod = resendPeriod;
         this.keepalivePeriod = keepalivePeriod;
         this._client = client;
     }
     
     public void store(CoapMessage message)
     {
         MessageResendTimer<CoapMessage> timer = new MessageResendTimer<CoapMessage>(message,_listener, this, false);
         Boolean added = false;
         if (message.getToken()==null)
         {
             Integer packetID = packetIDCounter.get();             
             byte[] token=ByteBuffer.allocate(4).putInt(packetID).array();
             while (!added)
             {
                 packetID = packetIDCounter.incrementAndGet() % MAX_VALUE;
            	 MessageResendTimer<CoapMessage> old=timersMap.putIfAbsent(token, timer);
            	 if(old==null)
            		 added = true;
             }

             message.setToken(token);
         }
         else
             timersMap.put(message.getToken(), timer);

         ScheduledFuture<?> timer_future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
		 timer.setFuture(timer_future);
     }
     
     public void store(byte[] token, CoapMessage message)
     {
         MessageResendTimer<CoapMessage> timer = new MessageResendTimer<CoapMessage>(message, _listener, this, false);
         timersMap.put(token, timer);
         ScheduledFuture<?> timer_future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
		 timer.setFuture(timer_future);
     }
     
	 @Override
	 public void refreshTimer(MessageResendTimer<CoapMessage> timer) 
	 {
		 Integer token = ByteBuffer.wrap(timer.getMessage().getToken()).getInt();
		 ScheduledFuture<?> future = null;
		 switch (token)
         {
             case 0:
            	future = scheduledservice.schedule(timer, keepalivePeriod, TimeUnit.MILLISECONDS);
 				break;
             default:
            	future = scheduledservice.schedule(timer, resendPeriod, TimeUnit.MILLISECONDS);
 				break;        
         }
		 
		 timer.setFuture(future);
	 }
	 
	 public CoapMessage remove(byte[] token)
     {
         MessageResendTimer<CoapMessage> timer = timersMap.remove(token);
         if (timer != null)
         {
        	 if (timer != null && timer.getFuture()!=null)
        		 timer.getFuture().cancel(true);

        	 return timer.getMessage();
         }

         return null;
     }

     public void stopAllTimers()
     {
         if (connectTimer != null && connectTimer.getFuture()!=null)
             connectTimer.getFuture().cancel(true);

         if (pingTimer != null && pingTimer.getFuture()!=null)
             pingTimer.getFuture().cancel(true);

         Iterator<Entry<byte[], MessageResendTimer<CoapMessage>>> iterator = timersMap.entrySet().iterator();
 		 while (iterator.hasNext())
 		 {
 			MessageResendTimer<CoapMessage> curr=iterator.next().getValue();
 			if (curr != null && curr.getFuture()!=null)
 				curr.getFuture().cancel(true);
 			
 			iterator.remove();
 		 }
     }
     
     public void storeConnectTimer(CoapMessage message)
     {
    	 if (connectTimer != null && connectTimer.getFuture()!=null)
             connectTimer.getFuture().cancel(true);

         connectTimer = new MessageResendTimer<CoapMessage>(message,_listener, this, true);
         ScheduledFuture<?> timer_future = scheduledservice.schedule(connectTimer, resendPeriod, TimeUnit.MILLISECONDS);
         connectTimer.setFuture(timer_future);
     }

     public void stopConnectTimer()
     {
    	 if (connectTimer != null && connectTimer.getFuture()!=null)
             connectTimer.getFuture().cancel(true);         
     }

     public void cancelConnectTimer()
     {
    	 if (connectTimer != null && connectTimer.getFuture()!=null)
    	 {
             connectTimer.getFuture().cancel(true);
             _client.cancelConnection();
         }
     }

     public void startPingTimer()
     {
    	 if (pingTimer != null && pingTimer.getFuture()!=null)
             pingTimer.getFuture().cancel(true);

         pingTimer = new MessageResendTimer<CoapMessage>(getPingreqMessage(),_listener, this, false);
         ScheduledFuture<?> timer_future = scheduledservice.schedule(pingTimer, keepalivePeriod, TimeUnit.MILLISECONDS);
         pingTimer.setFuture(timer_future);
     }

     private CoapMessage getPingreqMessage()
     {
         byte[] token = ByteBuffer.allocate(4).putInt(0).array();
         byte[] nodeIdBytes = _client.getClientID().getBytes();
         CoapMessage coapMessage = CoapMessage.builder().version(CoapClient.VERSION).type(CoapType.CONFIRMABLE).code(CoapCode.PUT).messageID(0).token(token).payload(new byte[0]).option(new CoapOption(CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes)).build();
         return coapMessage;
     }
}