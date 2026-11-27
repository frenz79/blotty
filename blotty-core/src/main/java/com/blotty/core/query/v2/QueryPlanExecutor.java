package com.blotty.core.query.v2;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.query.v2.models.IntersectionSource;
import com.blotty.core.query.v2.models.QuerySource;
import com.blotty.core.query.v2.models.UnionSource;

public class QueryPlanExecutor {

	public RowSource execute(QueryPlan plan, AbstractDataModel model ){
		return execute(plan.getSource(), model);
	}

	private RowSource execute(QuerySource source, AbstractDataModel model) {
		if ( source instanceof UnionSource union ) {
			RowSource left = execute(union.getLeft(), model);
			RowSource right = execute(union.getRight(), model);
			return left.union(right);
		}
		if ( source instanceof IntersectionSource inter ) {
			RowSource left = execute(inter.getLeft(), model);
			RowSource right = execute(inter.getRight(), model);
			return left.intersect(right);
		}

		return null;
	}
}
