package com.blotty.core.modifiers.sorters;

import com.blotty.core.models.AbstractRowsModel;

public interface ISorterCondition {

	public boolean apply( AbstractRowsModel rows );
}
