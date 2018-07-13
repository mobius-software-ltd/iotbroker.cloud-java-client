package com.mobiussoftware.iotbroker.mqtt;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.api.CountableMessage;
import com.mobius.software.mqtt.parser.header.impl.*;
import com.mobiussoftware.iotbroker.mqtt.net.TCPClient;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimersMap implements TimersMapInterface<MQMessage> {
	private static int MAX_VALUE = 65535;
	private static int MIN_VALUE = 1;

	private TCPClient listener;
	private long resendPeriod;
	private long keepalivePeriod;
	private MqttClient client;

	private ConcurrentHashMap<Integer,MessageResendTimer<MQMessage>> timersMap = new ConcurrentHashMap<>();
	private int packetIDCounter = MIN_VALUE;

	private MessageResendTimer<MQMessage> pingTimer;
	private MessageResendTimer<MQMessage> connectTimer;
	
	private ScheduledExecutorService scheduledservice = Executors.newScheduledThreadPool(5);

	public TimersMap(MqttClient client, TCPClient listener, long resendPeriod, long keepalivePeriod) 
	{
		this.listener = listener;
		this.resendPeriod = resendPeriod;
		this.keepalivePeriod = keepalivePeriod;
		this.client = client;
	}
	
	public void store(MQMessage message)
	{
		Boolean isConnect = false;
		if(message.getType() == MessageType.CONNECT)
			isConnect = true;
		MessageResendTimer<MQMessage> timer = new MessageResendTimer<MQMessage>(message,listener,this,isConnect);
		Boolean added = false;
		Integer packetID = null;
		if(message instanceof CountableMessage) {
			if(((CountableMessage) message).getPacketID()==null)
			{
				packetID = packetIDCounter;
				while(!added) 
				{
					
					packetID = (packetID + 1) % MAX_VALUE; 
					try{
						timersMap.put(packetID,timer);
						added = true;
					}catch(Exception ex){
						//already exists
					}
				}
				
				CountableMessage countable = (CountableMessage) message;
				countable.setPacketID(packetID);
				
			}else 
			{
				packetID = ((CountableMessage) message).getPacketID();
				timersMap.put(packetID, timer);
			}
				
		
			ScheduledFuture<?> timer_future  =  scheduledservice.schedule(timer, resendPeriod, TimeUnit.SECONDS);
			timer.setFuture(timer_future);
		}
	}
	
	public void store(Integer packetID,MQMessage message) 
	{
		Boolean isConnect = false;
		if(message.getType() == MessageType.CONNECT)
			isConnect = true;
		MessageResendTimer<MQMessage> timer = new MessageResendTimer<MQMessage>(message,listener,this,isConnect);
		timersMap.put(packetID,timer);
		ScheduledFuture<?> timer_future  = (ScheduledFuture<?>) scheduledservice.schedule(timer, resendPeriod, TimeUnit.SECONDS);
		timer.setFuture(timer_future);
	}
	
	@Override
	public void refreshTimer(MessageResendTimer<MQMessage> timer) 
	{
		MQMessage message = timer.getMessage();
		if(message instanceof CountableMessage) {
			Integer packetID = ((CountableMessage) message).getPacketID();
			ScheduledFuture<?> timer_future = null;
			switch(timer.getMessage().getType()){
				case PINGREQ:
					timer_future  = (ScheduledFuture<?>) scheduledservice.schedule(timer, keepalivePeriod, TimeUnit.SECONDS);
					break;
				default:
					timer_future  = (ScheduledFuture<?>) scheduledservice.schedule(timer, resendPeriod, TimeUnit.SECONDS);
					break;
			}
			
			timer.setFuture(timer_future);
		}
	}
	
	public MQMessage remove(Integer packetID) 
	{
		MessageResendTimer<MQMessage> timer = timersMap.get(packetID);
		timersMap.remove(packetID);
		if(timer != null)
		{
			timer.getFuture().cancel(true);
			return timer.getMessage();
		}
		
		return null;
	}
	
	public void stopAllTimers() 
	{
		if(connectTimer != null)
			connectTimer.getFuture().cancel(true);
		
		if(pingTimer != null)
			pingTimer.getFuture().cancel(true);
		
		for(Integer key : timersMap.keySet()) 
			timersMap.get(key).getFuture().cancel(true);
		
		timersMap.clear();
	}
	
	public void storeConnectTimer(MQMessage message){
		if(connectTimer != null)
			connectTimer.getFuture().cancel(true);
		
		connectTimer = new MessageResendTimer<MQMessage>(message,listener,this,true);
		ScheduledFuture<?> connectTimer_future  = (ScheduledFuture<?>) scheduledservice.schedule(connectTimer, resendPeriod, TimeUnit.SECONDS);
		connectTimer.setFuture(connectTimer_future);
	}
	
	@Override
	public void cancelConnectTimer() {
		if(connectTimer != null){
			connectTimer.getFuture().cancel(true);
			client.CancelConnection();	
		}			
	}

	public MessageResendTimer<MQMessage> getConnectTimer() 
	{
		return connectTimer;
	}

	public void startPingTimer() {
		if(pingTimer != null)
			pingTimer.getFuture().cancel(true);
		
		pingTimer = new MessageResendTimer<MQMessage>(new Pingreq(),listener,this,false);
		ScheduledFuture<?> pingTimer_future  = (ScheduledFuture<?>) scheduledservice.schedule(pingTimer, resendPeriod, TimeUnit.SECONDS);
		pingTimer.setFuture(pingTimer_future);
	}

	public void stopConnectTimer() {
		if(connectTimer != null) {
			connectTimer.getFuture().cancel(true);
			client.CancelConnection();
		}
	}
}
