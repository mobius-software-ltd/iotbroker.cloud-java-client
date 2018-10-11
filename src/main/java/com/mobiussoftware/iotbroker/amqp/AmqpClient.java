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

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mobius.software.amqp.parser.avps.OutcomeCode;
import com.mobius.software.amqp.parser.avps.RoleCode;
import com.mobius.software.amqp.parser.avps.SectionCode;
import com.mobius.software.amqp.parser.avps.SendCode;
import com.mobius.software.amqp.parser.avps.TerminusDurability;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.parser.header.impl.AMQPAttach;
import com.mobius.software.amqp.parser.header.impl.AMQPBegin;
import com.mobius.software.amqp.parser.header.impl.AMQPClose;
import com.mobius.software.amqp.parser.header.impl.AMQPDetach;
import com.mobius.software.amqp.parser.header.impl.AMQPDisposition;
import com.mobius.software.amqp.parser.header.impl.AMQPEnd;
import com.mobius.software.amqp.parser.header.impl.AMQPFlow;
import com.mobius.software.amqp.parser.header.impl.AMQPOpen;
import com.mobius.software.amqp.parser.header.impl.AMQPProtoHeader;
import com.mobius.software.amqp.parser.header.impl.AMQPTransfer;
import com.mobius.software.amqp.parser.header.impl.SASLChallenge;
import com.mobius.software.amqp.parser.header.impl.SASLInit;
import com.mobius.software.amqp.parser.header.impl.SASLMechanisms;
import com.mobius.software.amqp.parser.header.impl.SASLOutcome;
import com.mobius.software.amqp.parser.header.impl.SASLResponse;
import com.mobius.software.amqp.parser.sections.AMQPData;
import com.mobius.software.amqp.parser.sections.AMQPSection;
import com.mobius.software.amqp.parser.sections.MessageHeader;
import com.mobius.software.amqp.parser.terminus.AMQPSource;
import com.mobius.software.amqp.parser.terminus.AMQPTarget;
import com.mobius.software.amqp.parser.tlv.impl.AMQPAccepted;
import com.mobius.software.amqp.parser.wrappers.AMQPMessageFormat;
import com.mobius.software.amqp.parser.wrappers.AMQPSymbol;
import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobiussoftware.iotbroker.amqp.net.TCPClient;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.NetworkClient;
import com.mobiussoftware.iotbroker.network.TopicListener;

public class AmqpClient implements ConnectionListener<AMQPHeader>, AMQPDevice, NetworkClient 
{
	private final Logger logger = Logger.getLogger(getClass());
	
	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
    private Integer RESEND_PERIOND = 3000;
    private Integer WORKER_THREADS = 4;
    
    private InetSocketAddress address;
    private ConnectionState connectionState;

    private TimersMap timers;
    private TCPClient client;

    private Account account;
    
    private ClientListener listener;
    private TopicListener topicListener;
	
    private DBInterface dbInterface;

    private int channel;
    private Integer nextHandle = 0;
    private ConcurrentHashMap<String, Long> usedIncomingMappings = new ConcurrentHashMap<String, Long>();
    private ConcurrentHashMap<String, Long> usedOutgoingMappings = new ConcurrentHashMap<String, Long>();
    private ConcurrentHashMap<Long, String> usedMappings = new ConcurrentHashMap<Long, String>();
    private List<AMQPTransfer> pendingMessages = new ArrayList<AMQPTransfer>();

    public AmqpClient(Account account) throws Exception
    {
    	this.dbInterface = DBHelper.getInstance();
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());
		this.account = account;
        client = new TCPClient(address, WORKER_THREADS);
    }
    
    @Override 
	public void setClientListener(ClientListener clientListener)
	{
		this.listener = clientListener;
	}

	@Override 
	public void setTopicListener(TopicListener topicListener)
	{
		this.topicListener = topicListener;
	}

    public void setListener(ClientListener listener)
    {
        this.listener = listener;
    }

    public void setState(ConnectionState state)
    {
        this.connectionState = state;
        if (this.listener != null)
            listener.stateChanged(state);
    }

    public Boolean createChannel()
    {
        setState(ConnectionState.CHANNEL_CREATING);
        Boolean isSuccess = client.init(this);
        if (!isSuccess)
            setState(ConnectionState.CHANNEL_FAILED);            
        else
            timers.storeConnectTimer();
        
        return isSuccess;
    }
    
    public Boolean isConnected()
    {
        return connectionState == ConnectionState.CONNECTION_ESTABLISHED;
    }

    public ConnectionState getConnectionState()
    {
        return connectionState;
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    public void closeChannel()
    {
        if(client!=null)
            client.shutdown();
    }

    public void connect()
    {
        setState(ConnectionState.CONNECTING);

        if (timers != null)
            timers.stopAllTimers();

        AMQPProtoHeader header = new AMQPProtoHeader(3);
        client.send(header);
    }

    public void disconnect()
    {
        if (client.isConnected())
        {
            AMQPEnd end = new AMQPEnd();
            end.setChannel(channel);
            client.send(end);

            if (timers != null)
                timers.stopAllTimers();

            timers = null;
        }

        setState(ConnectionState.NONE);
        return;
    }

    public void subscribe(Topic[] topics)
    {
        for(int i=0;i<topics.length;i++)
        {
            Long currentHandler;
            if (usedIncomingMappings.containsKey(topics[i].getName().toString()))
                currentHandler = usedIncomingMappings.get(topics[i].getName().toString());
            else
            {
                currentHandler = nextHandle.longValue();
                nextHandle++;
                usedIncomingMappings.put(topics[i].getName().toString(),currentHandler);
                usedMappings.put(currentHandler,topics[i].getName().toString());
            }

            AMQPAttach attach = new AMQPAttach();
            attach.setChannel(channel);
            attach.setName(topics[i].getName().toString());
            attach.setHandle(currentHandler);
            attach.setRole(RoleCode.RECEIVER);
            attach.setSndSettleMode(SendCode.MIXED);
            AMQPTarget target = new AMQPTarget();
            target.setAddress(topics[i].getName().toString());
            target.setDurable(TerminusDurability.NONE);
            target.setTimeout(0L);
            target.setDynamic(false);
            attach.setTarget(target);
            client.send(attach);
        }
    }

    public void unsubscribe(String[] topics)
    {
        for(String topic:topics)
        {
            if (usedIncomingMappings.containsKey(topic))
            {
                AMQPDetach detach = new AMQPDetach();
                detach.setChannel(channel);
                detach.setClosed(true);
                detach.setHandle(usedIncomingMappings.get(topic));
                client.send(detach);
            }
            else
            {
            	try
				{
					logger.info("deleting  topic" + topic + " from DB");
					List<com.mobiussoftware.iotbroker.db.Topic> dbTopics=dbInterface.getTopics(account);
					for(com.mobiussoftware.iotbroker.db.Topic dbTopic:dbTopics)
					{
						if(dbTopic.getName().equals(topic))
						{
							dbInterface.deleteTopic(String.valueOf(dbTopic.getId()));
							if (topicListener != null)
								topicListener.finishDeletingTopic(String.valueOf(dbTopic.getId()));
						}
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
            }
        }
    }

    public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup)
    {
        AMQPTransfer transfer = new AMQPTransfer();
        transfer.setChannel(channel);
        transfer.setDeliveryId(0L);
        transfer.setSettled(false);
        transfer.setMore(false);
        transfer.setMessageFormat(new AMQPMessageFormat(0));

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setDurable(true);
        messageHeader.setPriority((short) 3);
        messageHeader.setMilliseconds(1000L);

        AMQPData data = new AMQPData();
        data.setValue(content);

        ConcurrentHashMap<SectionCode, AMQPSection> sections=new ConcurrentHashMap<>();
        sections.put(SectionCode.DATA,data);
        transfer.setSections(sections);

        if (usedOutgoingMappings.containsKey(topic.getName().toString()))
        {
            Long handle = usedOutgoingMappings.get(topic.getName().toString());
            transfer.setHandle(handle);
            timers.store(transfer);
            client.send(transfer);
        }
        else
        {
            Long currentHandler = nextHandle.longValue();
            nextHandle++;
            usedOutgoingMappings.put(topic.getName().toString(),currentHandler);
            usedMappings.put(currentHandler,topic.getName().toString());

            transfer.setHandle(currentHandler);
            pendingMessages.add(transfer);

            AMQPAttach attach = new AMQPAttach();
            attach.setChannel(channel);
            attach.setName(topic.getName().toString());
            attach.setHandle(currentHandler);
            attach.setRole(RoleCode.SENDER);
            attach.setSndSettleMode(SendCode.MIXED);
            AMQPSource source = new AMQPSource();
            source.setAddress(topic.getName().toString());
            source.setDurable(TerminusDurability.NONE);
            source.setTimeout(0L);
            source.setDynamic(false);
            attach.setSource(source);
            client.send(attach);
        }
    }

    public void reinit()
    {
        setState(ConnectionState.CHANNEL_CREATING);

        if (client != null)
            client.shutdown();

        client = new TCPClient(address, WORKER_THREADS);            
    }

    public void closeConnection()
    {
        if (timers != null)
            timers.stopAllTimers();

        if (client != null)
        {
            TCPClient currClient = client;
            client = null;
            currClient.shutdown();
        }
    }

    public void packetReceived(AMQPHeader message)
    {
    	try
		{
	    	switch(message.getCode())
	    	{
				case ATTACH:
					AMQPAttach attach=(AMQPAttach)message;
					processAttach(attach.getRole(),attach.getHandle());
					break;
				case BEGIN:
					processBegin();
					break;
				case CHALLENGE:
					processSASLChallenge(((SASLChallenge)message).getChallenge());
					break;
				case CLOSE:
					processClose();
					break;
				case DETACH:
					AMQPDetach detach=(AMQPDetach)message;
					processDetach(detach.getChannel(), detach.getHandle());
					break;
				case DISPOSITION:
					AMQPDisposition disposition=(AMQPDisposition)message;
					processDisposition(disposition.getFirst(), disposition.getLast());
					break;
				case END:
					processEnd(((AMQPEnd)message).getChannel());
					break;
				case FLOW:
					processFlow(((AMQPFlow)message).getChannel());
					break;
				case INIT:
					break;
				case MECHANISMS:
					SASLMechanisms mechanisms=(SASLMechanisms)message;
					processSASLMechanism(mechanisms.getMechanisms(), mechanisms.getChannel(), mechanisms.getType());
					break;
				case OPEN:
					processOpen(((AMQPOpen)message).getIdleTimeout());
					break;
				case OUTCOME:
					SASLOutcome outcome = ((SASLOutcome)message);
					processSASLOutcome(outcome.getAdditionalData(), outcome.getOutcomeCode());
					break;
				case PING:
					processPing();
					break;
				case PROTO:
					AMQPProtoHeader header = (AMQPProtoHeader)message;
					processProto(header.getChannel(),header.getProtocolId());
					break;
				case RESPONSE:
					processSASLResponse(((SASLResponse)message).getResponse());
					break;
				case TRANSFER:
					AMQPTransfer transfer=(AMQPTransfer)message;
					processTransfer(((AMQPData)transfer.getData()),transfer.getHandle(),transfer.getSettled(),transfer.getDeliveryId());
					break;
				default:
					break;    		
	    	}
		}
    	catch (Exception ex)
		{
			ex.printStackTrace();
			client.shutdown();
		}
    }

    public void cancelConnection()
    {
        client.shutdown();            
    }

    public void connectionLost()
    {
        if (account.isCleanSession())
            clearAccountTopics();

        if(timers!=null)
            timers.stopAllTimers();

        if (client != null)
        {
            client.shutdown();
            setState(ConnectionState.CONNECTION_LOST);
        }
    }

    private void clearAccountTopics()
    {
        dbInterface.deleteAllTopics();
    }
    
    public void connected()
    {
        setState(ConnectionState.CHANNEL_ESTABLISHED);           
    }

    public void connectFailed()
    {
        setState(ConnectionState.CHANNEL_FAILED);
    }

    public void processProto(Integer channel, Integer protocolId)
    {
        if (protocolId == 0)
        	this.channel = channel;                        
    }

    public void processOpen(Long idleTimeout)
    {
        timers = new TimersMap(this, client, RESEND_PERIOND, idleTimeout*1000);
        timers.startPingTimer();
        
        AMQPBegin begin = AMQPBegin.builder().channel(channel).nextOutgoingId(0L).incomingWindow(2147483647L).outgoingWindow(0L).build();
        client.send(begin);
    }

    public void processBegin()
    {
        setState(ConnectionState.CONNECTION_ESTABLISHED);

        if (account.isCleanSession())
            clearAccountTopics();                   
    }

    public void processAttach(RoleCode role,Long handle)
    {
        if (role!=null)
        {
            if (role == RoleCode.SENDER)
            {
                //publish
                if (handle!=null)
                {
                    for (int i = 0; i < pendingMessages.size(); i++)
                    {
                        AMQPTransfer currMessage = pendingMessages.get(i);
                        if (currMessage.getHandle().equals(handle))
                        {
                            pendingMessages.remove(i);
                            i--;

                            timers.store(currMessage);
                            client.send(currMessage);
                        }
                    }
                }
            }
            else
            {
                //subscribe
            }
        }
    }

    public void processFlow(Integer channel)
    {
        //not implemented for now
    }

    public void processTransfer(AMQPData data, Long handle,Boolean settled, Long deliveryId)
    {
    	QoS qos=QoS.AT_LEAST_ONCE;
        if (settled!=null && settled)
            qos = QoS.AT_MOST_ONCE;
        else
        {
            AMQPDisposition disposition = new AMQPDisposition();
            disposition.setChannel(channel);
            disposition.setRole(RoleCode.RECEIVER);
            disposition.setFirst(deliveryId);
            disposition.setLast(deliveryId);
            disposition.setSettled(true);
            disposition.setState(new AMQPAccepted());
            client.send(disposition);
        }

        String topicName=null;
        if (handle==null || !usedMappings.containsKey(handle))
            return;

        topicName  = usedMappings.get(handle);  
        com.mobiussoftware.iotbroker.db.Topic topic=null;
        try
        {
        	topic=dbInterface.getTopic(topicName);
        }
        catch(Exception ex)
        {
        	
        }
        
        if (topic==null)
            return;

        Message message=new Message(account, topicName, new String(data.getData()), true, (byte)qos.getValue(), false, false);
        try
		{
			logger.info("storing publish to DB");
			dbInterface.saveMessage(message);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
        
        if (listener != null)
            listener.messageReceived(message);
    }

    public void processDisposition(Long first, Long last)
    {
        if(first!=null && last!=null)
        {
            for (Long i = first; i < last; i++)
                timers.remove(i.intValue());                
        }
    }

    public void processDetach(Integer channel,Long handle)
    {
        if (handle!=null && usedMappings.containsKey(handle))
        {
            String topicName=usedMappings.get(handle);
            usedMappings.remove(handle);
            if (usedOutgoingMappings.containsKey(topicName))
                usedOutgoingMappings.remove(topicName);
        }
    }

    public void processEnd(Integer channel)
    {
        AMQPClose close = new AMQPClose();
        close.setChannel(channel);
        client.send(close);
    }

    public void processClose()
    {
        if (client.isConnected())
            client.close();
        
        setState(ConnectionState.NONE);
        return;
    }

    public void processSASLInit(String mechanism, byte[] initialResponse, String hostName) throws CoreLogicException
    {
        throw new CoreLogicException("received invalid message init");
    }

    public void processSASLChallenge(byte[] challenge) throws CoreLogicException
    {
        throw new CoreLogicException("received invalid message challenge");
    }

    public void processSASLMechanism(List<AMQPSymbol> mechanisms, Integer channel, Integer headerType)
    {
        AMQPSymbol plainMechanism = null;
        for (AMQPSymbol mechanism:mechanisms)
        {
            if (mechanism.getValue().toLowerCase().equals("plain"))
            {
                plainMechanism = mechanism;
                break;
            }
        }

        //currently supporting only plain
        if (plainMechanism==null)
        {
            timers.stopAllTimers();
            client.shutdown();
            setState(ConnectionState.CONNECTION_FAILED);
            return;
        }

        SASLInit saslInit = new SASLInit();
        saslInit.setType(headerType);
        saslInit.setChannel(channel);
        saslInit.setMechanism(plainMechanism.getValue());

        byte[] userBytes = account.getUsername().getBytes();
        byte[] passwordBytes = account.getPassword().getBytes();
        byte[] challenge = new byte[userBytes.length + 1 + userBytes.length + 1 + passwordBytes.length];
        System.arraycopy(userBytes, 0, challenge, 0, userBytes.length);
        challenge[userBytes.length] = 0x00;
        System.arraycopy(userBytes, 0, challenge, userBytes.length + 1, userBytes.length);
        challenge[userBytes.length + 1 + userBytes.length] = 0x00;
        System.arraycopy(passwordBytes, 0, challenge, userBytes.length + 1 + userBytes.length + 1, passwordBytes.length);            

        saslInit.setInitialResponse(challenge);
        client.send(saslInit);
    }

    public void processSASLOutcome(byte[] additionalData, OutcomeCode outcomeCode)
    {
        if (outcomeCode!=null)
            if(outcomeCode == OutcomeCode.OK)
            {
            	AMQPOpen open = AMQPOpen.builder().channel(channel).containerId(account.getClientId()).build();
                client.send(open);
            }
        }

    public void processSASLResponse(byte[] response) throws CoreLogicException
    {
        throw new CoreLogicException("received invalid message response");
    }

    public void processPing()
    {
       //nothing to be done here
    }
}
