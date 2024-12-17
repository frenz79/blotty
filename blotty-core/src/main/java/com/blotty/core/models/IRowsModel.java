package com.blotty.core.models;

import java.util.List;

import com.blotty.core.commons.exceptions.RowsModelException;

public interface IRowsModel {

	IRowsModel addRow(Row row) throws RowsModelException;

	int getRowsCount();

	List<Row> getAllRows();

}