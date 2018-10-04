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

import java.util.List;

import com.mobius.software.amqp.parser.avps.OutcomeCode;
import com.mobius.software.amqp.parser.avps.RoleCode;
import com.mobius.software.amqp.parser.sections.AMQPData;
import com.mobius.software.amqp.parser.wrappers.AMQPSymbol;

public interface AMQPDevice
{
    void processProto(Integer channel, Integer protocolId);

    void processOpen(Long idleTimeout);

    void processBegin();

    void processAttach(RoleCode role,Long handle);

    void processFlow(Integer channel);

    void processTransfer(AMQPData data, Long handle,Boolean settled, Long deliveryId);

    void processDisposition(Long first,Long last);

    void processDetach(Integer channel,Long handle);

    void processEnd(Integer channel);

    void processClose();

    void processSASLInit(String mechanism,byte[] initialResponse,String hostName) throws CoreLogicException;

    void processSASLChallenge(byte[] challenge) throws CoreLogicException;

    void processSASLMechanism(List<AMQPSymbol> mechanisms,Integer channel, Integer headerType);

    void processSASLOutcome(byte[] additionalData,OutcomeCode outcomeCode);

    void processSASLResponse(byte[] response) throws CoreLogicException;

    void processPing();
}