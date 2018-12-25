# iotbroker.cloud-java-client

### Project description

IoTBroker.cloud Java Client is an application that allows you to connect to the server using MQTT, MQTT-SN, 
AMQP or COAP protocols. IoTBroker.cloud Java Client gives the opportunity to exchange messages using protocols mentioned above. Your data can be also encrypted with TLS or DTLS secure protocols.   

Below you can find a brief description of each protocol that can help you make your choice. 
If you need to get more information, you can find it in our [blog](https://www.iotbroker.cloud/clientApps/Java/MQTT).
 
**MQTT** is a lightweight publish-subscribe based messaging protocol built for use over TCP/IP.  
MQTT was designed to provide devices with limited resources an easy way to communicate effectively. 
You need to familiarize yourself with the following MQTT features such as frequent communication drops, low bandwidth, 
low storage and processing capabilities of devices. 

Frankly, **MQTT-SN** is very similar to MQTT, but it was created for avoiding the potential problems that may occur at WSNs. 

Creating large and complex systems is always associated with solving data exchange problems between their various nodes. 
Additional difficulties are brought by such factors as the requirements for fault tolerance, 
the geographical diversity of subsystems, the presence a lot of nodes interacting with each others. 
The **AMQP** protocol was developed to solve all these problems, which has three basic concepts: 
exchange, queue and routing key. 

If you need to find a simple solution, it is recommended to choose the **COAP** protocol. 
The CoAP is a specialized web transfer protocol for use with constrained nodes and constrained (e.g., low-power, lossy) 
networks. It was developed to be used in very simple electronics devices that allows them to communicate interactively 
over the Internet. It is particularly targeted for small low power sensors, switches, valves and similar components 
that need to be controlled or supervised remotely, through standard Internet networks.     
 
### Prerequisites

In order to launch IoTBroker.Cloud Java Client you do not need to install any libraries or tools except for 
[Eclipse IDE](https://www.eclipse.org/).

### Installing

* To install IoTBroker.Cloud Java Client, first you should clone iotbroker.cloud-java-client.  

 * Next open IoTBroker.Cloud Java Client, in Eclipse, you should go to **File > Import > Maven > Existing Maven Projects**. 
Then choose cloned file in Root Directory and click Finish.   

 * Finally, you can launch the application. You should a right - click **Run As > Java application> Main - com.mobiussoftware.ui**. 

If the procedure is successful, you will see the Login page in the form of pop-up window.  
Please note that at this stage it is not possible to register as a client. You can only log in to your existing account. 


IoTBroker.Cloud Java Client is developed by [Mobius Software](https://www.mobius-software.com/)

## [License](LICENSE.md)

