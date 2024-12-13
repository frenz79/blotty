# blotty
Blotter Library


Examples

SimpleCvsLoader


Column model descriptor creation:

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

Data model creation with RowBuilder helper used for easy row creation and insertion:

    DataModel dataModel = new DataModel( colModel );   
    RowBuilder rowBuilder = dataModel.getRowBuilder();

Than line by line, we read the csv record putting data into our model
  
		Reader in = new FileReader("src/main/resources/organizations-10000.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int rowNumber = 0;
		for (CSVRecord record : records) {
			if ( rowNumber>0 ) {
				rowBuilder.newRow(record.get(0))
					.set("Organization Id", record.get(1))
					.set("Name", record.get(2))
					.set("Website", record.get(3))
					.set("Country", record.get(4))
					.set("Description", record.get(5))
					.set("Founded", record.get(6))
					.set("Industry", record.get(7))
					.set("Number of employees", record.get(8))	
				.addToModel();
			}
			rowNumber++;
		}		
		
		in.close();
		
		System.out.println("Model size:"+dataModel.getRowsCount());

Searching data is done using a FilterExpression object and chaining all conditions: 
  
		FilterExpression filter = new FilterExpressionBuilder()
				.begin(	new Equal(colModel.getColumn("Country"), StringField.of("Italy")) )
				.and( new GreaterThanEqual(colModel.getColumn("Number of employees"), IntegerField.of(5000)) )
				.and( new Like(colModel.getColumn("Industry"), StringField.of("Engineering")) )
				.build();

We create a view on our model applying the previous filter:

		DataModelView view = dataModel.createView("ItalianOrganizations", filter);

...and finally pretty print view content:

		System.out.println(view.dumpToString());

That's the final output:

    Model name:ItalianOrganizations
    |Organization Id    |Name        |Website            |Country   |Description              |Founded  |Industry                             |Number of employees|
    ---------------------------------------------------------------------------------------------------------------------------------------------------------------
    |83Ce9d2f643Db95    |Mays PLC    |https://crosby.com/|Italy     |Switchable 24/7 model    |1977     |Mechanical or Industrial Engineering |5800               |
    |8a14ebD20eA8D7A    |Watson LLC  |http://noble.com/  |Italy     |Secured modular attitude |2000     |Computer Software / Engineering      |8775               |

