package com.googlecode.kanbanik.client.messaging.messages.task;

import com.googlecode.kanbanik.client.messaging.BaseMessage;
import com.googlecode.kanbanik.dto.TaskDto;


public class TaskEditedMessage extends BaseMessage<TaskDto> {

	public TaskEditedMessage(TaskDto payload, Object source) {
		super(payload, source);
	}

}