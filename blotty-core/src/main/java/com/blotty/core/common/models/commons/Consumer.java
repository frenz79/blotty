package com.blotty.core.common.models.commons;

public interface Consumer<T> {

	public boolean stream( T item );
}
