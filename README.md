
# Blotty

**Blotty** is a lightweight in-memory tabular data engine designed for importing, filtering, sorting and viewing data from static or streaming sources.

Data is stored inside a **DataModel**, whose structure is defined by a **ColumnsModel** schema. A `DataModel` may generate one or more derived **DataModelView** instances through filtering and sorting operations.

Views are automatically synchronized with their parent model. Whenever rows are added, updated or removed, change notifications are propagated to all dependent views.

## Main Features

- Strongly typed columns (`String`, `Integer`, `Long`, `Float`, `Double`, `Boolean`)
- Primary key support
- In-memory storage
- Incremental view updates
- SQL-like filtering language
- Programmatic filter construction API
- Multi-column sorting
- Support for `AND`, `OR`, `NOT` and nested expressions
- Extensible indexing infrastructure
- Zero-dependency core module

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
model.addRows(row1, row2, row3);
```

---

# Filtering Data

## Programmatic API

Filters can be built directly using the AST-based condition model.

```java
FilterExpression filter =
    new FilterExpression(

        new AndCondition(

            BinaryCondition.Equal(
                Operand.of(columns.getColumn("COUNTRY")),
                Operand.of("Italy")),

            BinaryCondition.GreaterThanEqual(
                Operand.of(columns.getColumn("EMPLOYEES")),
                Operand.of(5000))
        )
    );
```

Logical conditions can be freely combined:

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

Blotty ships with a SQL-inspired query parser.

```java
FilterExpression filter =
    new SQLQueryParser().parse(
        columns,

        "\"COUNTRY\" = 'Italy' "
      + "AND \"EMPLOYEES\" >= 5000");
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

---

# Creating Views

A filtered view can be created from any model.

```java
DataModelView view =
    model.createView(
        "ItalianOrganizations",
        filter);
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

# Full Query Example

```java
DataModelView view =
    model.createView(

        "EuropeanOrganizations",

        new SQLQueryParser().parse(
            columns,

            "(\"COUNTRY\" = 'Italy' "
          + "OR "
          + "\"COUNTRY\" = 'France') "

          + "AND "

          + "\"EMPLOYEES\" >= 5000"
        )
    );
```

---

# Indexes

Blotty supports pluggable indexing strategies to reduce the amount of data scanned during filtering.

Example:

```java
model.addIndex(
    new HashIndex(
        columns.getColumn("COUNTRY")));
```

Indexes are automatically updated when rows are inserted or modified.

When available, a `DataModelView` may leverage indexes to reduce the candidate set before evaluating the full filter expression.

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

# License

Copyright (c) 2026 Francesco Bianchi

Licensed under the Apache License, Version 2.0.
See the `LICENSE` file for details.
