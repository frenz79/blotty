/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.rows;

public abstract class AbstractRowEvent {

	public static enum RowAction {
		ADD,
		UPD,
		DEL		
	}

	private final RowAction action;
	private final long eventTime = System.currentTimeMillis();
	
	public AbstractRowEvent(RowAction action) {
		super();
		this.action = action;
	}

	public RowAction getAction() {
		return action;
	}

	public long getEventTime() {
		return eventTime;
	}
	
	
}
