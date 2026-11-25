/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.indexes.HashIndex;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.rows.AbstractRowEvent;
import com.blotty.core.rows.AbstractRowEvent.RowAction;
import com.blotty.core.rows.Row;
import com.blotty.core.rows.Row.RowChange;
import com.blotty.core.rows.RowEvent;
import com.blotty.core.rows.RowEventBatch;
import com.blotty.core.storage.InMemoryRowsModel;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.StringField;

class DataModelEventsTest {

	private ColumnsModel colModel;
	private Column pkCol;
	private Column categoryCol;
	private Column scoreCol;

	@BeforeEach
	void setUp() throws Exception {
		colModel = ColumnsModel.newBuilder()
			.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("CATEGORY").type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("SCORE").type(FieldType.INTEGER_TYPE))
			.build();
		pkCol = colModel.getKey();
		categoryCol = colModel.getColumn("CATEGORY");
		scoreCol = colModel.getColumn("SCORE");
	}

	private Row buildRow(String pk, String category, Integer score) throws Exception {
		Row.Builder builder = Row.newBuilder(colModel)
			.set(pkCol, pk)
			.set(categoryCol, category);
		if (score == null) {
			builder.setNull(scoreCol);
		} else {
			builder.set(scoreCol, score);
		}
		return builder.build();
	}

	@Test
	void testInsertUpdateDeleteEventsAndListenerLifecycle() throws Exception {
		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		HashIndex categoryIndex = new HashIndex(categoryCol);
		model.addIndex(categoryIndex);

		List<AbstractRowEvent> events = Collections.synchronizedList(new ArrayList<>());
		model.addRowsListener("audit", events::add);

		// Unfiltered sorted view
		DataModelView view = model.createView(
			"sortedView",
			new QueryDefinition(null, new SorterExpressionBuilder(colModel).AscNullLast("SCORE").build())
		);

		// 1. INSERT (ADD)
		Row r1 = buildRow("K1", "ALPHA", 30);
		Row r2 = buildRow("K2", "BETA", 10);
		model.addRow(r1);
		model.addRow(r2);

		assertEquals(2, model.getRowsCount());
		assertEquals(2, view.getRowsCount());
		assertEquals(IntegerField.of(10), view.getRow(0).getFields()[scoreCol.getId()]);
		assertEquals(IntegerField.of(30), view.getRow(1).getFields()[scoreCol.getId()]);

		assertEquals(2, events.size());
		assertEquals(RowAction.ADD, events.get(0).getAction());
		assertEquals(RowAction.ADD, events.get(1).getAction());

		// 2. UPDATE (UPD) with trackChanges = true updates index, stats, and view
		Row r1Updated = buildRow("K1", "BETA", 5);
		RowChange change = model.updRow(r1Updated, true);
		assertNotNull(change);
		assertTrue(change.isModified(categoryCol.getId()));
		assertTrue(change.isModified(scoreCol.getId()));

		// Index should no longer contain K1 under ALPHA, and should contain both K1 and K2 under BETA
		assertEquals(0, categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("ALPHA"))).size());
		assertEquals(2, categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("BETA"))).size());

		// View should reflect updated score and re-sort (K1=5 before K2=10)
		assertEquals(IntegerField.of(5), view.getRow(0).getFields()[scoreCol.getId()]);
		assertEquals(IntegerField.of(10), view.getRow(1).getFields()[scoreCol.getId()]);
		assertEquals(3, events.size());
		assertEquals(RowAction.UPD, events.get(2).getAction());
		assertNotNull(((RowEvent) events.get(2)).getRowChange());

		// 3. DELETE (DEL) removes from model, index, and view
		Row storedR2 = model.getRowsModel().getRow(StringField.of("K2"));
		model.delRow(storedR2);

		assertEquals(1, model.getRowsCount());
		assertNull(model.getRowsModel().getRow(StringField.of("K2")));
		assertEquals(1, view.getRowsCount());
		assertEquals(1, categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("BETA"))).size());
		assertEquals(4, events.size());
		assertEquals(RowAction.DEL, events.get(3).getAction());

		// 4. Listener removal stops notifications
		model.delRowsListener("audit");
		model.addRow(buildRow("K3", "GAMMA", 50));
		assertEquals(4, events.size());
		assertEquals(2, view.getRowsCount());
	}

	@Test
	void testFilteredViewAddEventAndBatchInsertEvent() throws Exception {
		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		List<AbstractRowEvent> events = Collections.synchronizedList(new ArrayList<>());
		model.addRowsListener("batchAudit", events::add);

		FilterExpression alphaFilter = FilterExpression.newBuilder()
			.begin(BinaryCondition.Equal(Operand.of(categoryCol), Operand.of("ALPHA")))
			.build();
		DataModelView alphaView = model.createView("alphaOnly", new QueryDefinition(alphaFilter, null));

		model.addRow(buildRow("K1", "ALPHA", 10));
		model.addRow(buildRow("K2", "BETA", 20));
		assertEquals(1, alphaView.getRowsCount());
		assertEquals("K1", alphaView.getRow(0).getFields()[pkCol.getId()].toString());

		// Batch insert fires RowEventBatch
		model.addRows(buildRow("K3", "ALPHA", 30), buildRow("K4", "GAMMA", 40));
		assertEquals(4, model.getRowsCount());
		assertEquals(3, events.size());
		assertTrue(events.get(2) instanceof RowEventBatch);
		assertEquals(RowAction.ADD, events.get(2).getAction());
	}

	@Test
	void testConcurrentInsertsWithIndexAndView() throws Exception {
		int threads = 8;
		int rowsPerThread = 500;
		int totalRows = threads * rowsPerThread;

		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		HashIndex categoryIndex = new HashIndex(categoryCol);
		model.addIndex(categoryIndex);

		AtomicInteger addEvents = new AtomicInteger(0);
		model.addRowsListener("counter", evt -> {
			if (evt.getAction() == RowAction.ADD) {
				addEvents.incrementAndGet();
			}
		});

		DataModelView view = model.createView(
			"cat0View",
			new QueryDefinition(
				FilterExpression.newBuilder()
					.begin(BinaryCondition.Equal(Operand.of(categoryCol), Operand.of("CAT_0")))
					.build(),
				null
			)
		);

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threads);
		AtomicInteger errors = new AtomicInteger(0);

		for (int t = 0; t < threads; t++) {
			final int tid = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < rowsPerThread; i++) {
						String cat = "CAT_" + (i % 4);
						model.addRow(buildRow("T" + tid + "_" + i, cat, i));
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(endLatch.await(30, TimeUnit.SECONDS));
		executor.shutdown();

		assertEquals(0, errors.get());
		assertEquals(totalRows, model.getRowsCount());
		assertEquals(totalRows, addEvents.get());
		assertEquals(totalRows / 4, view.getRowsCount());
		assertEquals(
			totalRows / 4,
			categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("CAT_0"))).size()
		);
	}

	@Test
	void testConcurrentUpdatesWithIndexAndView() throws Exception {
		int threads = 6;
		int rowsPerThread = 200;
		int totalRows = threads * rowsPerThread;

		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		HashIndex categoryIndex = new HashIndex(categoryCol);
		model.addIndex(categoryIndex);

		for (int t = 0; t < threads; t++) {
			for (int i = 0; i < rowsPerThread; i++) {
				model.addRow(buildRow("U_" + t + "_" + i, "INITIAL", i));
			}
		}

		AtomicInteger updEvents = new AtomicInteger(0);
		model.addRowsListener("updCounter", evt -> {
			if (evt.getAction() == RowAction.UPD) {
				updEvents.incrementAndGet();
			}
		});

		DataModelView allView = model.createView("allView", new QueryDefinition());

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threads);
		AtomicInteger errors = new AtomicInteger(0);

		for (int t = 0; t < threads; t++) {
			final int tid = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < rowsPerThread; i++) {
						Row updated = buildRow("U_" + tid + "_" + i, "UPDATED_" + tid, 1000 + i);
						model.updRow(updated, true);
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(endLatch.await(30, TimeUnit.SECONDS));
		executor.shutdown();

		assertEquals(0, errors.get());
		assertEquals(totalRows, updEvents.get());
		assertEquals(totalRows, model.getRowsCount());
		assertEquals(totalRows, allView.getRowsCount());
		assertEquals(0, categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("INITIAL"))).size());
		for (int t = 0; t < threads; t++) {
			assertEquals(
				rowsPerThread,
				categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("UPDATED_" + t))).size()
			);
		}
	}

	@Test
	void testConcurrentDeletesWithIndexAndView() throws Exception {
		int threads = 6;
		int rowsPerThread = 200;
		int totalRows = threads * rowsPerThread;

		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		HashIndex categoryIndex = new HashIndex(categoryCol);
		model.addIndex(categoryIndex);

		Row[][] rowsByThread = new Row[threads][rowsPerThread];
		for (int t = 0; t < threads; t++) {
			for (int i = 0; i < rowsPerThread; i++) {
				Row row = buildRow("D_" + t + "_" + i, "TO_DELETE", i);
				rowsByThread[t][i] = row;
				model.addRow(row);
			}
		}

		AtomicInteger delEvents = new AtomicInteger(0);
		model.addRowsListener("delCounter", evt -> {
			if (evt.getAction() == RowAction.DEL) {
				delEvents.incrementAndGet();
			}
		});

		DataModelView allView = model.createView("allView", new QueryDefinition());
		assertEquals(totalRows, allView.getRowsCount());

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threads);
		AtomicInteger errors = new AtomicInteger(0);

		for (int t = 0; t < threads; t++) {
			final int tid = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < rowsPerThread; i++) {
						model.delRow(rowsByThread[tid][i]);
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(endLatch.await(30, TimeUnit.SECONDS));
		executor.shutdown();

		assertEquals(0, errors.get());
		assertEquals(totalRows, delEvents.get());
		assertEquals(0, model.getRowsCount());
		assertEquals(0, allView.getRowsCount());
		assertEquals(0, categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("TO_DELETE"))).size());
	}

	@Test
	void testConcurrentMixedInsertUpdateDeleteAndReads() throws Exception {
		int writerThreads = 4;
		int readerThreads = 2;
		int opsPerWriter = 200; // inserts 200, updates 100, deletes 50 -> 150 remaining per thread

		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		HashIndex categoryIndex = new HashIndex(categoryCol);
		model.addIndex(categoryIndex);

		Set<String> expectedSurvivingKeys = ConcurrentHashMap.newKeySet();
		ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch writersDone = new CountDownLatch(writerThreads);
		AtomicInteger errors = new AtomicInteger(0);

		for (int t = 0; t < writerThreads; t++) {
			final int tid = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					List<Row> inserted = new ArrayList<>(opsPerWriter);
					for (int i = 0; i < opsPerWriter; i++) {
						Row r = buildRow("M_" + tid + "_" + i, "ACTIVE", i);
						model.addRow(r);
						inserted.add(r);
					}
					for (int i = 0; i < opsPerWriter / 2; i++) {
						Row upd = buildRow("M_" + tid + "_" + i, "PROMOTED", i + 1000);
						model.updRow(upd, true);
					}
					for (int i = 0; i < opsPerWriter / 4; i++) {
						model.delRow(inserted.get(i));
					}
					for (int i = opsPerWriter / 4; i < opsPerWriter; i++) {
						expectedSurvivingKeys.add("M_" + tid + "_" + i);
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				} finally {
					writersDone.countDown();
				}
			});
		}

		for (int r = 0; r < readerThreads; r++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					while (writersDone.getCount() > 0) {
						model.getRowsCount();
						model.stream(row -> true);
					}
				} catch (Exception e) {
					errors.incrementAndGet();
				}
			});
		}

		startLatch.countDown();
		assertTrue(writersDone.await(30, TimeUnit.SECONDS));
		executor.shutdown();
		assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

		assertEquals(0, errors.get());
		int expectedRemaining = writerThreads * (opsPerWriter - opsPerWriter / 4);
		assertEquals(expectedRemaining, model.getRowsCount());
		assertEquals(expectedRemaining, expectedSurvivingKeys.size());

		int expectedPromoted = writerThreads * (opsPerWriter / 2 - opsPerWriter / 4);
		int expectedActive = writerThreads * (opsPerWriter - opsPerWriter / 2);
		assertEquals(
			expectedPromoted,
			categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("PROMOTED"))).size()
		);
		assertEquals(
			expectedActive,
			categoryIndex.lookup(new BinaryCondition(Operand.of(categoryCol), new Equal(), Operand.of("ACTIVE"))).size()
		);
	}
}
