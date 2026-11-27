/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.DataModel;
import com.blotty.core.DataModelView;
import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.indexes.HashIndex;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.parsers.SQLQueryParser;
import com.blotty.core.query.v1.QueryPlannerV1;
import com.blotty.core.rows.Row;
import com.blotty.core.storage.InMemoryRowsModel;
import com.blotty.core.types.FieldType;

class ComplexQueryTest {

	private ColumnsModel colModel;
	private Column pkCol;
	private Column deptCol;
	private Column roleCol;
	private Column ageCol;
	private Column salaryCol;
	private Column activeCol;
	private Column notesCol;

	@BeforeEach
	void setUp() throws Exception {
		colModel = ColumnsModel.newBuilder()
			.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("DEPT").type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("ROLE").type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("AGE").type(FieldType.INTEGER_TYPE))
			.add(Column.newBuilder("SALARY").type(FieldType.DOUBLE_TYPE))
			.add(Column.newBuilder("ACTIVE").type(FieldType.BOOLEAN_TYPE))
			.add(Column.newBuilder("NOTES").type(FieldType.STRING_TYPE))
			.build();

		pkCol = colModel.getKey();
		deptCol = colModel.getColumn("DEPT");
		roleCol = colModel.getColumn("ROLE");
		ageCol = colModel.getColumn("AGE");
		salaryCol = colModel.getColumn("SALARY");
		activeCol = colModel.getColumn("ACTIVE");
		notesCol = colModel.getColumn("NOTES");
	}

	private DataModel createPopulatedModel(boolean withIndexes) throws Exception {
		DataModel model = new DataModel(new InMemoryRowsModel(colModel));

		model.addRow(buildRow("1", "ENG", "DEV", 24, 55000.0, true, "Junior Java"));
		model.addRow(buildRow("2", "ENG", "PRINCIPAL", 42, 120000.0, true, "Tech Lead"));
		model.addRow(buildRow("3", "ENG", "DEV", 58, 95000.0, false, null));
		model.addRow(buildRow("4", "ENG", "QA", 31, 60000.0, true, "Automation"));
		model.addRow(buildRow("5", "SALES", "REP", 29, 50000.0, true, "EMEA"));
		model.addRow(buildRow("6", "SALES", "PRINCIPAL", 45, 110000.0, true, "Enterprise"));
		model.addRow(buildRow("7", "HR", "MANAGER", 38, 70000.0, false, null));
		model.addRow(buildRow("8", "ENG", "DEV", 22, 48000.0, true, "Intern"));

		if (withIndexes) {
			model.addIndex(new HashIndex(deptCol));
			model.addIndex(new HashIndex(roleCol));
		}
		return model;
	}

	private Row buildRow(String pk, String dept, String role, int age, double salary, boolean active, String notes) throws Exception {
		Row.Builder b = Row.newBuilder(colModel)
			.set(pkCol, pk)
			.set(deptCol, dept)
			.set(roleCol, role)
			.set(ageCol, age)
			.set(salaryCol, salary)
			.set(activeCol, active);
		if (notes == null) {
			b.setNull(notesCol);
		} else {
			b.set(notesCol, notes);
		}
		return b.build();
	}

	private Set<String> extractKeys(DataModelView view) {
		Set<String> keys = new HashSet<>();
		for (int i = 0; i < view.getRowsCount(); i++) {
			keys.add(view.getRow(i).getFields()[pkCol.getId()].toString());
		}
		return keys;
	}

	@Test
	void testComplexAndOrNotQueryWithoutAndWithIndexes() throws Exception {
		DataModel noIdxModel = createPopulatedModel(false);
		DataModel idxModel = createPopulatedModel(true);

		// DEPT = 'ENG' AND (AGE < 25 OR AGE > 55) AND NOT ROLE = 'QA' AND NOTES IS NOT NULL
		FilterExpression filter = FilterExpression.newBuilder()
			.begin(BinaryCondition.Equal(Operand.of(deptCol), Operand.of("ENG")))
			.and(new OrCondition(
				BinaryCondition.LesserThan(Operand.of(ageCol), Operand.of(25)),
				BinaryCondition.GreaterThan(Operand.of(ageCol), Operand.of(55))
			))
			.andNot(BinaryCondition.Equal(Operand.of(roleCol), Operand.of("QA")))
			.and(new UnaryCondition(Operand.of(notesCol), new IsNotNull()))
			.build();

		SorterConditionChain sorter = new SorterExpressionBuilder(colModel)
			.DescNullLast("AGE")
			.build();

		QueryDefinition query = new QueryDefinition(filter, sorter);
		DataModelView noIdxView = noIdxModel.createView("noIdx", query);
		DataModelView idxView = idxModel.createView("idx", query);

		// Expected: PK=1 (ENG, 24, DEV, "Junior Java") and PK=8 (ENG, 22, DEV, "Intern")
		// PK=3 has NOTES = null so excluded
		assertEquals(Set.of("1", "8"), extractKeys(noIdxView));
		assertEquals(extractKeys(noIdxView), extractKeys(idxView));
		assertEquals(2, idxView.getRowsCount());
		assertEquals("1", idxView.getRow(0).getFields()[pkCol.getId()].toString());
		assertEquals("8", idxView.getRow(1).getFields()[pkCol.getId()].toString());
	}

	@Test
	void testComplexQueryWithLikeNotLikeAndNullChecksWithoutIndexes() throws Exception {
		DataModel model = createPopulatedModel(false);

		// (DEPT = 'ENG' AND NOTES LIKE 'Tech') OR (NOTES IS NULL AND ACTIVE = false)
		FilterExpression filter = new FilterExpression(
			new OrCondition(
				new AndCondition(
					BinaryCondition.Equal(Operand.of(deptCol), Operand.of("ENG")),
					BinaryCondition.Like(Operand.of(notesCol), Operand.of("Tech"))
				),
				new AndCondition(
					new UnaryCondition(Operand.of(notesCol), new IsNull()),
					BinaryCondition.Equal(Operand.of(activeCol), Operand.of(false))
				)
			)
		);

		DataModelView view = model.createView("complexOr", new QueryDefinition(filter, null));
		// Expected: PK=2 (Tech Lead), PK=3 (notes=null, active=false), PK=7 (notes=null, active=false)
		assertEquals(Set.of("2", "3", "7"), extractKeys(view));
	}

	@Test
	void testSqlComplexQueryWithAndWithoutIndexes() throws Exception {
		DataModel noIdxModel = createPopulatedModel(false);
		DataModel idxModel = createPopulatedModel(true);

		SQLQueryParser parser = new SQLQueryParser();
		QueryDefinition query = parser.parse(
			colModel,
			"\"DEPT\" = 'ENG' AND (\"SALARY\" >= 90000.0 OR \"AGE\" < 25) AND \"ACTIVE\" = true"
		);
		query.setSorter(new SorterExpressionBuilder(colModel).AscNullLast("PK").build());

		DataModelView noIdxView = noIdxModel.createView("sqlNoIdx", query);
		DataModelView idxView = idxModel.createView("sqlIdx", query);

		// Expected: PK=1 (24, true), PK=2 (120000, true), PK=8 (22, true); PK=3 is active=false
		assertEquals(Set.of("1", "2", "8"), extractKeys(noIdxView));
		assertEquals(extractKeys(noIdxView), extractKeys(idxView));
		assertEquals(3, idxView.getRowsCount());
		assertEquals("1", idxView.getRow(0).getFields()[pkCol.getId()].toString());
		assertEquals("2", idxView.getRow(1).getFields()[pkCol.getId()].toString());
		assertEquals("8", idxView.getRow(2).getFields()[pkCol.getId()].toString());
	}

	@Test
	void testQueryPlannerSelectsMostSelectiveIndexAndComputesResidualFilter() throws Exception {
		DataModel noIdxModel = createPopulatedModel(false);
		DataModel idxModel = createPopulatedModel(true);
		AbstractQueryPlanner planner = new QueryPlannerV1();

		// DEPT='ENG' matches 5 rows; ROLE='PRINCIPAL' matches 2 rows -> planner should pick HashIndex(ROLE)
		FilterExpression filter = FilterExpression.newBuilder()
			.begin(BinaryCondition.Equal(Operand.of(deptCol), Operand.of("ENG")))
			.and(BinaryCondition.Equal(Operand.of(roleCol), Operand.of("PRINCIPAL")))
			.build();
		QueryDefinition query = new QueryDefinition(filter, null);

		QueryPlan noIdxPlan = planner.plan(query, noIdxModel);
		List<Row> list = new ArrayList<>();
		noIdxPlan.getSource().apply( r -> {list.add(r);return true;});
		assertEquals(1, list.size());
		assertNull(noIdxPlan.getResidualFilter());

		QueryPlan idxPlan = planner.plan(query, idxModel);
		list.clear();
		idxPlan.getSource().apply( r -> {list.add(r);return true;});
		assertEquals(2, list.size());
		assertNotNull(idxPlan.getResidualFilter());

		DataModelView view = idxModel.createView("selectiveView", query);
		assertEquals(Set.of("2"), extractKeys(view));
	}

	@Test
	void testQueryPlannerIgnoresNotSubtreeForIndexing() throws Exception {
		DataModel idxModel = createPopulatedModel(true);
		AbstractQueryPlanner planner = new QueryPlannerV1();

		// NOT DEPT='ENG' must NOT use HashIndex(DEPT)='ENG'
		FilterExpression filter = FilterExpression.newBuilder()
			.beginNot(BinaryCondition.Equal(Operand.of(deptCol), Operand.of("ENG")))
			.build();
		QueryDefinition query = new QueryDefinition(filter, null);

		QueryPlan plan = planner.plan(query, idxModel);
		List<Row> list = new ArrayList<>();
		plan.getSource().apply( r -> {list.add(r);return true;});
		assertEquals(3, list.size());
		assertNull(plan.getResidualFilter());

		DataModelView view = idxModel.createView("notEngView", query);
		assertEquals(Set.of("5", "6", "7"), extractKeys(view));
	}

	@Test
	void testQueryPlannerSingleIndexedConditionClearsResidualFilter() throws Exception {
		DataModel idxModel = createPopulatedModel(true);
		AbstractQueryPlanner planner = new QueryPlannerV1();

		FilterExpression filter = FilterExpression.newBuilder()
			.begin(BinaryCondition.Equal(Operand.of(roleCol), Operand.of("PRINCIPAL")))
			.build();
		QueryPlan plan = planner.plan(new QueryDefinition(filter, null), idxModel);
		List<Row> list = new ArrayList<>();
		plan.getSource().apply( r -> {list.add(r);return true;});
		assertEquals(2, list.size());
		assertNull(plan.getResidualFilter());
	}

	@Test
	void testQueryPlannerV2AndOrNotWithAndWithoutIndexes() throws Exception {
		DataModel noIdxModel = createPopulatedModel(false);
		DataModel idxModel = createPopulatedModel(true);
		AbstractQueryPlanner v2 = new com.blotty.core.query.v2.QueryPlannerV2();

		// OR with two indexed branches: DEPT = 'HR' OR ROLE = 'PRINCIPAL' -> PK 2, 6, 7
		FilterExpression orFilter = new FilterExpression(
			new OrCondition(
				BinaryCondition.Equal(Operand.of(deptCol), Operand.of("HR")),
				BinaryCondition.Equal(Operand.of(roleCol), Operand.of("PRINCIPAL"))
			)
		);
		QueryDefinition orQuery = new QueryDefinition(orFilter, null);
		DataModelView v2NoIdxOr = new DataModelView("v2NoIdxOr", orQuery, noIdxModel, v2);
		DataModelView v2IdxOr = new DataModelView("v2IdxOr", orQuery, idxModel, v2);
		assertEquals(Set.of("2", "6", "7"), extractKeys(v2NoIdxOr));
		assertEquals(extractKeys(v2NoIdxOr), extractKeys(v2IdxOr));

		// Complex AND + OR + NOT with V2: DEPT = 'ENG' AND (AGE < 25 OR AGE > 55) AND NOT ROLE = 'QA'
		FilterExpression complexFilter = FilterExpression.newBuilder()
			.begin(BinaryCondition.Equal(Operand.of(deptCol), Operand.of("ENG")))
			.and(new OrCondition(
				BinaryCondition.LesserThan(Operand.of(ageCol), Operand.of(25)),
				BinaryCondition.GreaterThan(Operand.of(ageCol), Operand.of(55))
			))
			.andNot(BinaryCondition.Equal(Operand.of(roleCol), Operand.of("QA")))
			.build();
		QueryDefinition complexQuery = new QueryDefinition(complexFilter, null);
		DataModelView v2NoIdxComplex = new DataModelView("v2NoIdxComplex", complexQuery, noIdxModel, v2);
		DataModelView v2IdxComplex = new DataModelView("v2IdxComplex", complexQuery, idxModel, v2);
		assertEquals(Set.of("1", "3", "8"), extractKeys(v2NoIdxComplex));
		assertEquals(extractKeys(v2NoIdxComplex), extractKeys(v2IdxComplex));
	}
}
