package com.googlecode.kanbanik.client.modules.editworkflow.workflow;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.FlowPanelDropController;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.kanbanik.client.BaseAsyncCallback;
import com.googlecode.kanbanik.client.KanbanikServerCaller;
import com.googlecode.kanbanik.client.ServerCommandInvokerManager;
import com.googlecode.kanbanik.client.messaging.Message;
import com.googlecode.kanbanik.client.messaging.MessageBus;
import com.googlecode.kanbanik.client.messaging.MessageListener;
import com.googlecode.kanbanik.client.messaging.messages.board.BoardsRefreshRequestMessage;
import com.googlecode.kanbanik.client.messaging.messages.workflowitem.WorkflowitemChangedMessage;
import com.googlecode.kanbanik.client.modules.editworkflow.workflow.WorkflowEditingComponent.Position;
import com.googlecode.kanbanik.dto.WorkflowitemDto;
import com.googlecode.kanbanik.dto.shell.EditWorkflowParams;
import com.googlecode.kanbanik.dto.shell.FailableResult;
import com.googlecode.kanbanik.dto.shell.SimpleParams;
import com.googlecode.kanbanik.shared.ServerCommand;

public class WorkflowEditingDropController extends FlowPanelDropController implements MessageListener<WorkflowitemDto> {
	private WorkflowitemDto contextItem;

	private WorkflowitemDto currentItem;

	private final Position position;

	public WorkflowEditingDropController(FlowPanel dropTarget,
			WorkflowitemDto contextItem, WorkflowitemDto currentItem,
			Position position) {
		super(dropTarget);
		this.contextItem = contextItem;
		this.currentItem = currentItem;
		this.position = position;
		MessageBus.registerListener(WorkflowitemChangedMessage.class, this);
	}

	@Override
	public void onPreviewDrop(DragContext context) throws VetoDragException {
		// veto if dropped before or after himself

		Widget w = context.selectedWidgets.iterator().next();
		if (!(w instanceof WorkflowitemWidget)) {
			return;
		}
		WorkflowitemDto droppedItem = ((WorkflowitemWidget) w)
				.getWorkflowitem();
		if (droppedItem.getId() != null && currentItem != null && currentItem.getId() != null) {
			if (droppedItem.getId().equals(currentItem.getId())) {
				throw new VetoDragException();
			}

			WorkflowitemDto nextItem = findNextItem();
			if (nextItem != null && nextItem.getId() != null
					&& droppedItem.getId().equals(nextItem.getId())) {
				throw new VetoDragException();
			}
		}

		super.onPreviewDrop(context);
	}

	@Override
	public void onDrop(DragContext context) {
		super.onDrop(context);

		if (context.selectedWidgets.size() > 1) {
			throw new UnsupportedOperationException(
					"Only one workflowitem can be dragged at a time");
		}

		Widget w = context.selectedWidgets.iterator().next();
		if (!(w instanceof WorkflowitemWidget)) {
			return;
		}

		final WorkflowitemDto droppedItem = ((WorkflowitemWidget) w)
				.getWorkflowitem();
		WorkflowitemDto nextItem = findNextItem();

		droppedItem.setNextItem(nextItem);

		new KanbanikServerCaller(
				new Runnable() {

					public void run() {
		ServerCommandInvokerManager
				.getInvoker()
				.<EditWorkflowParams, FailableResult<SimpleParams<WorkflowitemDto>>> invokeCommand(
						ServerCommand.EDIT_WORKFLOW,
						new EditWorkflowParams(droppedItem, contextItem),
						new BaseAsyncCallback<FailableResult<SimpleParams<WorkflowitemDto>>>() {

							@Override
							public void success(
									FailableResult<SimpleParams<WorkflowitemDto>> result) {
								MessageBus
										.sendMessage(new BoardsRefreshRequestMessage(
												"", this));
							}
							
							@Override
							public void failure(
									FailableResult<SimpleParams<WorkflowitemDto>> result) {
								MessageBus
								.sendMessage(new BoardsRefreshRequestMessage(
										"", this));
							}
							
						});
		}});
	}

	private WorkflowitemDto findNextItem() {
		if (position == Position.BEFORE) {
			return currentItem;
		} else if (position == Position.AFTER) {
			return currentItem.getNextItem();
		} else {
			// this can happen only if it has no children => has no next item
			return null;
		}

	}

	public void messageArrived(Message<WorkflowitemDto> message) {
		if (contextItem != null && message.getPayload().getId().equals(contextItem.getId())) {
			contextItem = message.getPayload();
		}
		
		if (currentItem != null && message.getPayload().getId().equals(currentItem.getId())) {
			currentItem = message.getPayload();
		}
	}
}
