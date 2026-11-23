package com.blotty.core.stress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.DataModel;
import com.blotty.core.models.DataModelView;
import com.blotty.core.models.Row;
import com.blotty.core.models.indexes.HashIndex;
import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.parsers.SQLQueryParser;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.IntegerField;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataModelStressTest {

	private static ColumnsModel columnsModel;
	private static final String[] DEPARTMENTS = { "Engineering", "Sales", "Marketing", "HR", "Finance", "Legal" };
	private static final String[] FIRST_NAMES = { "Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Henry" };
	private static final String[] LAST_NAMES = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis" };

	@BeforeAll
	static void initModel() throws Exception {
		columnsModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("ID", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("FIRST_NAME").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("LAST_NAME").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("AGE").type(FieldType.INTEGER_TYPE))
				.add(Column.newBuilder("SALARY").type(FieldType.DOUBLE_TYPE))
				.add(Column.newBuilder("ACTIVE").type(FieldType.BOOLEAN_TYPE))
				.add(Column.newBuilder("DEPARTMENT").type(FieldType.STRING_TYPE))
				.build();
	}

	private static Row createSampleRow(ColumnsModel cm, String id, int index) throws Exception {
		Random rnd = new Random(index);
		String firstName = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
		String lastName = LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
		int age = 20 + (index % 50);
		double salary = 30000.0 + ((index * 37) % 70000);
		boolean active = (index % 2 == 0);
		String dept = DEPARTMENTS[index % DEPARTMENTS.length];

		return new Row(cm)
				.set(cm.getKey(), id)
				.set(cm.getColumn("FIRST_NAME"), firstName)
				.set(cm.getColumn("LAST_NAME"), lastName)
				.set(cm.getColumn("AGE"), age)
				.set(cm.getColumn("SALARY"), salary)
				.set(cm.getColumn("ACTIVE"), active)
				.set(cm.getColumn("DEPARTMENT"), dept);
	}

	@Test
	@Order(1)
	@DisplayName("1. Benchmark Inserimento Sequenziale Singolo Thread")
	void testSingleThreadInsertion() throws Exception {
		int totalRecords = 50_000;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));

		long start = System.nanoTime();
		for (int i = 0; i < totalRecords; i++) {
			model.addRow(createSampleRow(columnsModel, "ST_" + i, i));
		}
		long elapsedNs = System.nanoTime() - start;
		double elapsedMs = elapsedNs / 1_000_000.0;
		double throughput = (totalRecords / (elapsedMs / 1000.0));

		assertEquals(totalRecords, model.getRowsCount());
		printBenchmarkResult("Inserimento Sequenziale (1 thread)", totalRecords, 1, elapsedMs, throughput);
	}

	@Test
	@Order(2)
	@DisplayName("2. Stress Test Inserimento Concorrente Multi-Thread (Thread-Safety & Throughput)")
	void testMultiThreadedConcurrentInsertion() throws Exception {
		int threadsCount = 8;
		int recordsPerThread = 10_000;
		int totalRecords = threadsCount * recordsPerThread;

		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threadsCount);
		AtomicInteger errorCount = new AtomicInteger(0);

		long start = System.nanoTime();
		for (int t = 0; t < threadsCount; t++) {
			final int threadId = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < recordsPerThread; i++) {
						String key = "T" + threadId + "_R" + i;
						model.addRow(createSampleRow(columnsModel, key, threadId * 100_000 + i));
					}
				} catch (Exception e) {
					errorCount.incrementAndGet();
					e.printStackTrace();
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		boolean finished = endLatch.await(30, TimeUnit.SECONDS);
		long elapsedNs = System.nanoTime() - start;
		executor.shutdown();

		assertTrue(finished, "L'inserimento concorrente non è terminato entro il timeout!");
		assertEquals(0, errorCount.get(), "Nessun errore doveva verificarsi durante l'inserimento concorrente");
		assertEquals(totalRecords, model.getRowsCount(), "Il conteggio totale delle righe inserite deve corrispondere");

		double elapsedMs = elapsedNs / 1_000_000.0;
		double throughput = (totalRecords / (elapsedMs / 1000.0));
		printBenchmarkResult("Inserimento Concorrente (8 threads)", totalRecords, threadsCount, elapsedMs, throughput);
	}

	@Test
	@Order(3)
	@DisplayName("3. Stress Collisione Chiavi Concorrente (Atomicità wlock)")
	void testConcurrentDuplicateKeyCollisions() throws Exception {
		int threadsCount = 20;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threadsCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger duplicateCaughtCount = new AtomicInteger(0);

		for (int t = 0; t < threadsCount; t++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					// Tutti i thread tentano di inserire la STESSA chiave contemporaneamente
					model.addRow(createSampleRow(columnsModel, "COLLIDING_PK", 42));
					successCount.incrementAndGet();
				} catch (RowsModelException e) {
					if (e.getMessage() != null && e.getMessage().contains("Duplicated key")) {
						duplicateCaughtCount.incrementAndGet();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		endLatch.await(10, TimeUnit.SECONDS);
		executor.shutdown();

		assertEquals(1, successCount.get(), "Esattamente un solo thread deve riuscire ad inserire la chiave");
		assertEquals(threadsCount - 1, duplicateCaughtCount.get(), "Tutti gli altri thread devono ricevere eccezione di chiave duplicata");
		assertEquals(1, model.getRowsCount(), "Lo storage deve contenere esattamente 1 riga");
	}

	@Test
	@Order(4)
	@DisplayName("4. Benchmark Filtering & SQL Query Parsing su Large Dataset")
	void testFilteringPerformance() throws Exception {
		int totalRecords = 50_000;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));
		for (int i = 0; i < totalRecords; i++) {
			model.addRow(createSampleRow(columnsModel, "ROW_" + i, i));
		}

		// Filtro programmato: Engineering AND Age >= 40
		FilterExpression filter = FilterExpression.newBuilder()
				.begin(BinaryCondition.Equal(Operand.of(columnsModel.getColumn("DEPARTMENT")), Operand.of("Engineering")))
				.and(BinaryCondition.GreaterThanEqual(Operand.of(columnsModel.getColumn("AGE")), Operand.of(40)))
				.build();

		long startFilter = System.nanoTime();
		DataModelView view = model.createView("EngSeniors", new QueryDefinition(filter, null));
		long filterElapsedMs = (System.nanoTime() - startFilter) / 1_000_000;

		assertTrue(view.getRowsCount() > 0);
		assertTrue(view.getRowsCount() < totalRecords);

		// Filtro SQL Parsing
		long startSql = System.nanoTime();
		SQLQueryParser parser = new SQLQueryParser();
		QueryDefinition sqlFilter = parser.parse(columnsModel, " \"DEPARTMENT\" = 'Engineering' AND \"AGE\" >= '40' ");
		DataModelView sqlView = model.createView("EngSeniorsSQL", sqlFilter);
		long sqlElapsedMs = (System.nanoTime() - startSql) / 1_000_000;

		assertEquals(view.getRowsCount(), sqlView.getRowsCount(), "I risultati del filtro programmatico e del filtro SQL devono coincidere");

		printBenchmarkResult("Filtering Programmatico su 50k", view.getRowsCount(), 1, filterElapsedMs, (totalRecords / (filterElapsedMs / 1000.0)));
		printBenchmarkResult("SQL Query Parsing & View su 50k", sqlView.getRowsCount(), 1, sqlElapsedMs, (totalRecords / (sqlElapsedMs / 1000.0)));
	}

	@Test
	@Order(5)
	@DisplayName("5. Benchmark Sorting Multi-Colonna e Verifica Invariante di Ordinamento")
	void testSortingPerformanceAndCorrectness() throws Exception {
		int totalRecords = 20_000;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));
		for (int i = 0; i < totalRecords; i++) {
			model.addRow(createSampleRow(columnsModel, "S_" + i, i));
		}

		// Multi-column sorting: DEPARTMENT AscNullLast, poi AGE DescNullLast
		SorterConditionChain sorter = new SorterExpressionBuilder(columnsModel)
				.AscNullLast("DEPARTMENT")
				.DescNullLast("AGE")
				.build();

		long startSort = System.nanoTime();
		DataModelView view = model.createView("SortView", new QueryDefinition(null, sorter));
		
		long sortElapsedMs = (System.nanoTime() - startSort) / 1_000_000;

		assertEquals(totalRecords, view.getRowsCount());

		// Verifica correttezza ordinamento (Invariante di comparazione)
		for (int i = 0; i < view.getRowsCount() - 1; i++) {
			Row current = view.getRow(i);
			Row next = view.getRow(i + 1);
			int comp = sorter.compare(current, next);
			assertTrue(comp <= 0, "Violazione ordinamento all'indice " + i + ": current > next");
		}

		printBenchmarkResult("Multi-Column Sort (Dept ASC, Age DESC)", totalRecords, 1, sortElapsedMs, (totalRecords / (sortElapsedMs / 1000.0)));
	}

	@Test
	@Order(6)
	@DisplayName("6. Stress Test Concorrenza Massiva Read/Write (Lettori e Scrittori Simultanei con Lock)")
	void testReadWriteConcurrencyStress() throws Exception {
		int numWriters = 4;
		int numReaders = 4;
		int durationSeconds = 3;

		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));

		// Prepopolamento base
		for (int i = 0; i < 5000; i++) {
			model.addRow(createSampleRow(columnsModel, "PRE_" + i, i));
		}

		ExecutorService executor = Executors.newFixedThreadPool(numWriters + numReaders);
		AtomicBoolean running = new AtomicBoolean(true);
		AtomicLong writeOps = new AtomicLong(0);
		AtomicLong readOps = new AtomicLong(0);
		AtomicInteger readErrors = new AtomicInteger(0);
		AtomicInteger writeErrors = new AtomicInteger(0);

		// Writer Threads
		for (int w = 0; w < numWriters; w++) {
			final int writerId = w;
			executor.submit(() -> {
				long counter = 0;
				while (running.get()) {
					try {
						String key = "W_" + writerId + "_" + counter++;
						model.addRow(createSampleRow(columnsModel, key, (int) counter));
						writeOps.incrementAndGet();
					} catch (Exception e) {
						writeErrors.incrementAndGet();
						e.printStackTrace();
					}
				}
			});
		}

		// Reader Threads (Stream concorrenti e getRow)
		for (int r = 0; r < numReaders; r++) {
			final int readerId = r;
			executor.submit(() -> {
				while (running.get()) {
					try {
						// Stream concorrente sotto rLock
						model.stream(new IConsumer<Row>() {
							int count = 0;
							@Override
							public boolean stream(Row item) {
								assertNotNull(item);
								return (++count < 100); // limit scan per iterazione
							}
						});
						// Lettura posizionale
						int size = model.getRowsCount();
						if (size > 0) {
							Row row = model.getRow(size / 2);
							assertNotNull(row);
						}
						readOps.incrementAndGet();
					} catch (Exception e) {
						readErrors.incrementAndGet();
						e.printStackTrace();
					}
				}
			});
		}

		Thread.sleep(durationSeconds * 1000);
		running.set(false);
		executor.shutdown();
		boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);

		assertTrue(terminated, "Tutti i thread reader/writer dovevano terminare regolarmente");
		assertEquals(0, writeErrors.get(), "Nessun errore di scrittura durante concorrenza");
		assertEquals(0, readErrors.get(), "Nessun errore di lettura concorrente (es. ConcurrentModification)");

		System.out.printf("[CONCURRENCY STRESS] Scritture completate: %d | Letture completate: %d | Righe totali nel modello: %d%n",
				writeOps.get(), readOps.get(), model.getRowsCount());
	}

	@Test
	@Order(7)
	@DisplayName("7. Stress Aggiornamento Dinamico della View con Scrittori Paralleli")
	void testDataModelViewDynamicUpdatesConcurrently() throws Exception {
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));

		// Vista con filtro (Solo Active=true) e ordinata per AGE
		FilterExpression filter = FilterExpression.newBuilder()
				.begin(BinaryCondition.Equal(Operand.of(columnsModel.getColumn("ACTIVE")), Operand.of(true)))
				.build();

		SorterConditionChain sorter = new SorterExpressionBuilder(columnsModel)
				.AscNullLast("AGE")
				.build();

		DataModelView view = model.createView("DynamicView", new QueryDefinition(filter, sorter));

		int numWriters = 4;
		int rowsPerWriter = 1000;
		ExecutorService executor = Executors.newFixedThreadPool(numWriters);
		CountDownLatch latch = new CountDownLatch(numWriters);

		for (int w = 0; w < numWriters; w++) {
			final int writerId = w;
			executor.submit(() -> {
				try {
					for (int i = 0; i < rowsPerWriter; i++) {
						String key = "DYN_" + writerId + "_" + i;
						model.addRow(createSampleRow(columnsModel, key, writerId * 10_000 + i));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(15, TimeUnit.SECONDS);
		executor.shutdown();

		assertEquals(numWriters * rowsPerWriter, model.getRowsCount());
		assertTrue(view.getRowsCount() > 0, "La vista deve aver ricevuto gli eventi ADD dinamicamente");

		// Verifica che la vista sia rimasta coerentemente ordinata
		for (int i = 0; i < view.getRowsCount() - 1; i++) {
			int ageCurrent = ((IntegerField) view.getRow(i).getFields()[columnsModel.getColumn("AGE").getId()]).getValue();
			int ageNext = ((IntegerField) view.getRow(i + 1).getFields()[columnsModel.getColumn("AGE").getId()]).getValue();
			assertTrue(ageCurrent <= ageNext, "La vista dinamica deve mantenere l'ordinamento!");
		}

		System.out.printf("[DYNAMIC VIEW STRESS] Righe nel genitore: %d | Righe filtrate e ordinate nella vista: %d%n",
				model.getRowsCount(), view.getRowsCount());
	}
	
	@Test
	@Order(8)
	@DisplayName("8. Benchmark Inserimento Batch Singolo Thread")
	void testSingleThreadBatchInsertion() throws Exception {
		int totalRecords = 50_000;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));

		long start = System.nanoTime();
		Row[] rows = new Row[totalRecords];
		
		for (int i = 0; i < totalRecords; i++) {
			rows[i] = createSampleRow(columnsModel, "ST_" + i, i);
		}
		
		model.addRows(rows);
		long elapsedNs = System.nanoTime() - start;
		double elapsedMs = elapsedNs / 1_000_000.0;
		double throughput = (totalRecords / (elapsedMs / 1000.0));

		assertEquals(totalRecords, model.getRowsCount());
		printBenchmarkResult("Inserimento Sequenziale Batch (1 thread)", totalRecords, 1, elapsedMs, throughput);
	}

	@Test
	@Order(9)
	@DisplayName("9. Benchmark Filtering & SQL Query Parsing su Large Dataset using Index")
	void testFilteringPerformanceWithIndex() throws Exception {
		int totalRecords = 50_000;
		DataModel model = new DataModel(new InMemoryRowsModel(columnsModel));
		for (int i = 0; i < totalRecords; i++) {
			model.addRow(createSampleRow(columnsModel, "ROW_" + i, i));
		}

		model.addIndex( new HashIndex(columnsModel.getColumn("DEPARTMENT")));
		
		// Filtro programmato: Engineering AND Age >= 40
		FilterExpression filter = FilterExpression.newBuilder()
				.begin(BinaryCondition.Equal(Operand.of(columnsModel.getColumn("DEPARTMENT")), Operand.of("Engineering")))
				.and(BinaryCondition.GreaterThanEqual(Operand.of(columnsModel.getColumn("AGE")), Operand.of(40)))
				.build();

		long startFilter = System.nanoTime();
		DataModelView view = model.createView("EngSeniors", new QueryDefinition(filter, null));
		long filterElapsedMs = (System.nanoTime() - startFilter) / 1_000_000;

		assertTrue(view.getRowsCount() > 0);
		assertTrue(view.getRowsCount() < totalRecords);

		// Filtro SQL Parsing
		long startSql = System.nanoTime();
		SQLQueryParser parser = new SQLQueryParser();
		QueryDefinition sqlFilter = parser.parse(columnsModel, " \"DEPARTMENT\" = 'Engineering' AND \"AGE\" >= '40' ");
		DataModelView sqlView = model.createView("EngSeniorsSQL", sqlFilter);
		long sqlElapsedMs = (System.nanoTime() - startSql) / 1_000_000;

		assertEquals(view.getRowsCount(), sqlView.getRowsCount(), "I risultati del filtro programmatico e del filtro SQL devono coincidere");

		printBenchmarkResult("Filtering Programmatico con indici su 50k", view.getRowsCount(), 1, filterElapsedMs, (totalRecords / (filterElapsedMs / 1000.0)));
		printBenchmarkResult("SQL Query Parsing & View su 50k", sqlView.getRowsCount(), 1, sqlElapsedMs, (totalRecords / (sqlElapsedMs / 1000.0)));
	}
	
	private static void printBenchmarkResult(String testName, int records, int threads, double elapsedMs, double throughput) {
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.printf("%-42s | Record: %-7d | Thread: %-2d | Tempo: %8.2f ms | %10.0f ops/s%n",
				testName, records, threads, elapsedMs, throughput);
		System.out.println("-----------------------------------------------------------------------------------------");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("=========================================================================================");
		System.out.println("                         BLOTTY CORE STRESS & BENCHMARK SUITE                            ");
		System.out.println("=========================================================================================");
		initModel();
		DataModelStressTest suite = new DataModelStressTest();
		suite.testSingleThreadInsertion();
		suite.testMultiThreadedConcurrentInsertion();
		suite.testSingleThreadBatchInsertion();
		suite.testConcurrentDuplicateKeyCollisions();
		suite.testFilteringPerformance();
		suite.testFilteringPerformanceWithIndex();
		suite.testSortingPerformanceAndCorrectness();
		suite.testReadWriteConcurrencyStress();				
		suite.testDataModelViewDynamicUpdatesConcurrently();
		System.out.println("=========================================================================================");
		System.out.println("                         TUTTI GLI STRESS TEST COMPLETATI CON SUCCESSO!                  ");
		System.out.println("=========================================================================================");
	}
}
