package com.blotty.core.commons;

import com.blotty.core.commons.exceptions.RowsModelException;

public interface IConsumer<T> {

	public boolean stream( T item ) throws RowsModelException;
}
