package com.blotty.core.models;

import java.util.Collection;

import com.blotty.core.commons.exceptions.RowsModelException;

public interface IRowsModel {

	IRowsModel addRow(Row row) throws RowsModelException;

	int getRowsCount();

	Collection<Row> getAllRows();

}