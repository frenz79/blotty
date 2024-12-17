
# blotty
A library for importing, filtering and sorting data from static sources (ex: csv) and live feeds (ex: kafka).
Mapped data is stored in an in memory tabular object called **DataModel** structured according its own **ColumnModel** descriptor.

## Import and search csv data
Example taken from  "**SimpleCvsLoader**"

 1. **ColumnModel** descriptor creation:
```
	ColumnsModel colModel = new ColumnsModelBuilder()
		.add( "Organization Id", FieldType.STRING_TYPE )
		.add( "Name", FieldType.STRING_TYPE )
		.add( "Website", FieldType.STRING_TYPE )
		.add( "Country", FieldType.STRING_TYPE )
		.add( "Description", FieldType.STRING_TYPE )
		.add( "Founded", FieldType.STRING_TYPE )
		.add( "Industry", FieldType.STRING_TYPE )
		.add( "Number of employees", FieldType.INTEGER_TYPE )				
		.build();	
```
 2. **DataModel** creation with RowBuilder helper used for easy row creation and insertion:
```
    DataModel dataModel = new DataModel( colModel );   
    RowBuilder rowBuilder = dataModel.getRowBuilder();
```
3. Than line by line, we read the csv record putting data into our model
  ```
		Reader in = new FileReader("src/main/resources/organizations-10000.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int rowNumber = 0;
		for (CSVRecord record : records) {
			if ( rowNumber>0 ) {
				rowBuilder.newRow(record.get(0))
				.set(colModel.getColumn(0), record.get(1))
				.set(colModel.getColumn(1), record.get(2))
				.set(colModel.getColumn(2), record.get(3))
				.set(colModel.getColumn(3), record.get(4))
				.set(colModel.getColumn(4), record.get(5))
				.set(colModel.getColumn(5), record.get(6))
				.set(colModel.getColumn(6), record.get(7))
				.set(colModel.getColumn(7), record.get(8))
				.addToModel();
			}
			rowNumber++;
		}		
		in.close();
		System.out.println("Model size:"+dataModel.getRowsCount());
```

4. Searching data is done using a **FilterExpression** object and chaining all conditions: 
  ```
		FilterExpression filter = new FilterExpressionBuilder()
			.begin( BinaryCondition.Equal(Operand.of(colModel.getColumn("Country")), Operand.of("Italy"))) 
			.and( BinaryCondition.GreaterThanEqual(Operand.of(colModel.getColumn("Number of employees")), Operand.of(5000))) 
			.and( BinaryCondition.Like(Operand.of(colModel.getColumn("Industry")), Operand.of("Engineering")))
			.build();
```

5. We create a **view** on our model applying the previous filter:
```
		DataModelView view = dataModel.createView("ItalianOrganizations", filter);
```
...and finally pretty print view content:
```
		System.out.println(view.dumpToString());
```
That's the final output:
```
    Model name:ItalianOrganizations
    |Organization Id    |Name        |Website            |Country   |Description              |Founded  |Industry                             |Number of employees|
    ---------------------------------------------------------------------------------------------------------------------------------------------------------------
    |83Ce9d2f643Db95    |Mays PLC    |https://crosby.com/|Italy     |Switchable 24/7 model    |1977     |Mechanical or Industrial Engineering |5800               |
    |8a14ebD20eA8D7A    |Watson LLC  |http://noble.com/  |Italy     |Secured modular attitude |2000     |Computer Software / Engineering      |8775               |
```
But filters can be created using a simpler **SQL** like **syntax** too, like following:
```
DataModelView sqlFilteredView = dataModel.createView("ItalianOrganizations_1", 
	new SQLQueryParser().parse(colModel, 
		  " \"Country\" = 'Italy' 
		AND \"Number of employees\" >= '5000' 
		AND \"Industry\" LIKE 'Engineering' "));
```
