# Blotty

**Blotty** is a lightweight in-memory tabular data engine designed for importing, filtering, sorting and viewing data from static or streaming sources.

Data is stored inside a **DataModel**, whose structure is defined by a **ColumnsModel** schema. A `DataModel` may generate one or more derived **DataModelView** instances through filtering and sorting operations.

Views are automatically synchronized with their parent model. Whenever rows are added, updated or removed, change notifications are propagated to all dependent views.

## Main Features

- Strongly typed columns (`String`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Boolean`)
- Primary key support
- In-memory storage
- Incremental view updates
- Row-level event propagation
- Column-level change tracking
- SQL-like filtering language
- AST-based filter model
- Multi-column sorting
- Query planner with index selection
- Incremental index maintenance
- Pluggable indexing infrastructure
- Built-in column statistics
- Zero-dependency core module

---

# Architecture Overview

The core components are:

```text
DataModel
    |
    +-- RowsModel
    |       |
    |       +-- InMemoryRowsModel
    |
    +-- Indexes
    |       |
    |       +-- HashIndex
    |
    +-- Views
            |
            +-- DataModelView
```

Filtering is internally represented as an Abstract Syntax Tree (AST):

```text
                OR
              /    \
           AND      EQ
          /   \       \
        EQ    GT     COUNTRY='FR'
```

The query planner may leverage available indexes to reduce the candidate row set before evaluating the residual filter expression.

---

# Creating a DataModel

## Define the schema

```java
ColumnsModel columns = ColumnsModel.newBuilder()
    .add(
        Column.newBuilder("ID", true)
            .type(FieldType.STRING_TYPE))
    .add(
        Column.newBuilder("NAME")
            .type(FieldType.STRING_TYPE))
    .add(
        Column.newBuilder("COUNTRY")
            .type(FieldType.STRING_TYPE))
    .add(
        Column.newBuilder("EMPLOYEES")
            .type(FieldType.INTEGER_TYPE))
    .build();
```

The schema defines:

- Column names
- Column types
- The primary key column

---

## Create a DataModel

```java
DataModel model =
    new DataModel(
        new InMemoryRowsModel(columns));
```

---

## Add rows

```java
model.addRow(

    Row.newBuilder(columns)

        .set("ID", "ORG001")
        .set("NAME", "Acme Ltd")
        .set("COUNTRY", "Italy")
        .set("EMPLOYEES", "5000")

        .build()
);
```

Multiple rows can be inserted using:

```java
model.addRows(
    row1,
    row2,
    row3
);
```

---

# Filtering Data

## Programmatic API

Filters are represented internally as an AST and can be built directly.

```java
FilterExpression filter =

    new FilterExpression(

        new AndCondition(

            BinaryCondition.Equal(

                Operand.of(
                    columns.getColumn("COUNTRY")),

                Operand.of("Italy")
            ),

            BinaryCondition.GreaterThanEqual(

                Operand.of(
                    columns.getColumn("EMPLOYEES")),

                Operand.of(5000)
            )
        )
    );
```

Example with nested logical operators:

```java
FilterExpression filter =

    new FilterExpression(

        new OrCondition(

            BinaryCondition.Equal(
                Operand.of(columns.getColumn("COUNTRY")),
                Operand.of("Italy")),

            new AndCondition(

                BinaryCondition.Equal(
                    Operand.of(columns.getColumn("COUNTRY")),
                    Operand.of("France")),

                BinaryCondition.GreaterThan(
                    Operand.of(columns.getColumn("EMPLOYEES")),
                    Operand.of(5000))
            )
        )
    );
```

---

## SQL-like Syntax

Blotty includes a SQL-inspired parser that converts textual expressions into AST filters.

```java
QueryDefinition query =

    new SQLQueryParser().parse(

        columns,

        "\"COUNTRY\" = 'Italy' "
      + "AND "
      + "\"EMPLOYEES\" >= 5000"
    );
```

### Supported Operators

```sql
=
!=
>
>=
<
<=
LIKE
IS NULL
IS NOT NULL
```

### Supported Logical Operators

```sql
AND
OR
NOT
```

### Nested Expressions

```sql
(
    "COUNTRY" = 'Italy'
    OR
    "COUNTRY" = 'France'
)
AND
"EMPLOYEES" > 5000
```

### Column-to-Column Comparisons

```sql
"FROM_QTY" < "TO_QTY"
```

### Numeric Literals

```sql
"AGE" > 18
"PRICE" >= 123.45
"DELTA" > -10
```

### Boolean Literals

```sql
"ACTIVE" = true
"ACTIVE" != false
```

### NULL Checks

```sql
"COUNTRY" IS NULL

"COUNTRY" IS NOT NULL
```

---

# Creating Views

A filtered view can be created from any model.

```java
DataModelView view =

    model.createView(

        "ItalianOrganizations",

        query
    );
```

Views contain only rows matching the associated filter.

Any change occurring in the parent model is automatically propagated to all dependent views.

---

# Sorting

Sort expressions can be chained.

```java
SorterConditionChain sorter =

    new SorterExpressionBuilder(columns)

        .DescNullLast("EMPLOYEES")

        .AscNullLast("NAME")

        .build();
```

Apply the sorter:

```java
view.sort(sorter);
```

### Supported Sorting Modes

```java
AscNullFirst
AscNullLast
DescNullFirst
DescNullLast
```

### Example

```java
view.sort(

    new SorterExpressionBuilder(columns)

        .DescNullLast("EMPLOYEES")

        .AscNullLast("COUNTRY")

        .build()
);
```

---

# Query Planner

Blotty includes a lightweight query planner.

When indexes are available:

1. The filter AST is analyzed.
2. Indexable conditions are extracted.
3. The most selective index is chosen.
4. A candidate row set is fetched.
5. The remaining residual filter is evaluated.

Example:

```sql
"STATUS" = 'OPEN'
AND
"QTY" > 100
```

If a `HashIndex(STATUS)` exists:

```text
Candidate rows:
    STATUS='OPEN'

Residual filter:
    QTY > 100
```

This reduces the number of rows that must be evaluated.

---

# Indexes

Blotty supports pluggable indexing strategies to reduce the amount of data scanned during filtering.

Current implementation:

```java
HashIndex
```

Example:

```java
model.addIndex(

    new HashIndex(
        columns.getColumn("COUNTRY")
    )
);
```

Indexes are automatically maintained when rows are:

- inserted
- updated
- deleted

---

## Index Statistics

Indexes may expose statistics used by the query planner.

Example:

```java
long distinctValues =
    hashIndex
        .getStatistics()
        .getDistinctValues();
```

---

# Column Statistics

Columns automatically maintain statistics.

Currently available:

```java
column.getStatistics()
      .getNullValues();

column.getStatistics()
      .getNotNullValues();
```

These statistics are updated incrementally during inserts, updates and deletes.

Future planner optimizations may leverage these values for predicates like:

```sql
IS NULL
IS NOT NULL
```

---

# Incremental Updates

Rows are mutable and support change tracking.

During an update Blotty automatically tracks:

- Modified columns
- Previous values
- Updated values

The information is propagated through row events and consumed by:

- indexes
- views
- UI components

This allows efficient incremental updates without rebuilding dependent structures.

---

# Events

Data models generate row-level events.

```java
RowEvent
```

Supported actions:

```java
ADD
UPD
DEL
```

Bulk operations are represented through:

```java
RowEventBatch
```

---

# Full Query Example

```java
QueryDefinition query =

    new SQLQueryParser().parse(

        columns,

        "(\"COUNTRY\" = 'Italy' "
      + "OR "
      + "\"COUNTRY\" = 'France') "

      + "AND "

      + "\"EMPLOYEES\" >= 5000"
    );

DataModelView view =
    model.createView(
        "EuropeanOrganizations",
        query
    );
```

---

# Debugging

Any model or view can be dumped as a formatted table.

```java
System.out.println(
    view.dumpToString());
```

Example output:

```text
Model name:EuropeanOrganizations

|ID    |NAME      |COUNTRY |EMPLOYEES|
--------------------------------------
|ORG01 |Acme Ltd  |Italy   |5000     |
|ORG02 |Beta Spa  |France  |7500     |
```

---

# Swing Demo

The examples module includes a simple Swing-based data explorer showcasing:

- CSV loading
- Filtering
- Sorting
- Live view updates

![CSVExplorer](https://github.com/user-attachments/assets/905f0184-00fc-41b5-ad80-f83e2bf22fc7)

---

# Roadmap

Planned improvements include:

- Compound indexes
- Range indexes
- Query plan explain support
- Statistics-driven planning
- SQL `ORDER BY` parsing
- SQL `LIMIT` support
- Additional storage implementations
- Large dataset storage engines
- Cost-based query optimization

---

# License

Copyright (c) 2026 Francesco Bianchi

Licensed under the Apache License, Version 2.0.

See the `LICENSE` file for details.
