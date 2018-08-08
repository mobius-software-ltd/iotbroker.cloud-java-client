package com.mobiussoftware.iotbroker.network;

public interface TopicListener
{
	void finishAddingTopic(String topicName, int qosVal);

	void finishAddingTopicFailed();

	void finishDeletingTopic(String id);
}
