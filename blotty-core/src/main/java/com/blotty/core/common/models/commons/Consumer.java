package com.blotty.core.common.models.commons;

import com.blotty.core.common.exceptions.RowsModelException;

public interface Consumer<T> {

	public boolean stream( T item ) throws RowsModelException;
}
