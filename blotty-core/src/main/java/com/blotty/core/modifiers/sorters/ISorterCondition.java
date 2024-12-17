package com.blotty.core.modifiers.sorters;

import com.blotty.core.models.IRowsModel;

public interface ISorterCondition {

	public boolean apply( IRowsModel rows );
}
