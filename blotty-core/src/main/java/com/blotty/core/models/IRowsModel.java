package com.blotty.core.models;

import java.util.List;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public interface IRowsModel {

	IRowsModel addRow(Row row) throws RowsModelException;

	int getRowsCount();

	List<Row> getAllRows();

	Row getRow(int position);

	Row getRow(IGenericField key);

}