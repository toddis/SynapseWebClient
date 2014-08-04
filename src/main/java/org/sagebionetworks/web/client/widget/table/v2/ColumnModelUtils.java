package org.sagebionetworks.web.client.widget.table.v2;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;

/**
 * Utilities for working with ColumnModels
 * 
 * @author John
 *
 */
public class ColumnModelUtils {

	/**
	 * Apply the passed ColumnModel to the passed row
	 * @param model
	 * @param row
	 */
	public static void applyColumnModelToRow(ColumnModel model, ColumnModelTableRow row){
		if(model == null){
			throw new IllegalArgumentException("Model cannot be null");
		}
		if(model.getColumnType() == null){
			throw new IllegalArgumentException("Model.columnType cannot be null");
		}
		if(row == null){
			throw new IllegalArgumentException("Row cannot be null");
		}
		row.setId(model.getId());
		row.setColumnName(model.getName());
		row.setColumnType(ColumnTypeViewEnum.getViewForType(model.getColumnType()));
		if(model.getMaximumSize() != null){
			row.setMaxSize(model.getMaximumSize().toString());
		}
		if(model.getEnumValues() != null){
			row.setEnumValues(model.getEnumValues());
		}
		row.setDefaultValue(model.getDefaultValue());
	}
	
	/**
	 * Treat empty strings as nulls.
	 * 
	 * @param input
	 * @return
	 */
	public static String treatEmptyAsNull(String input){
		if(input == null){
			return null;
		}
		if("".equals(input.trim())){
			return null;
		}
		return input;
	}
	
	/**
	 * Extract a list of ColumnModel from a list of a list of ColumnModelTableRows
	 * @param rows
	 * @return
	 */
	public static List<ColumnModel> extractColumnModels(List<ColumnModelTableRow> rows) {
		List<ColumnModel> list = new LinkedList<ColumnModel>();
		for(ColumnModelTableRow row: rows){
			list.add(extractColumnModel(row));
		}
		return list;
	}

	/**
	 * Extract a ColumnModel from a ColumnModelTableRow
	 * @param row
	 * @return
	 */
	public static ColumnModel extractColumnModel(ColumnModelTableRow row) {
		if(row == null){
			throw new IllegalArgumentException("Row cannot be null");
		}
		if(row.getColumnType() == null){
			throw new IllegalArgumentException("Row.columnType cannot be null");
		}
		ColumnModel model = new ColumnModel();
		model.setName(treatEmptyAsNull(row.getColumnName()));
		model.setColumnType(row.getColumnType().getType());
		model.setId(treatEmptyAsNull(row.getId()));
		String size = treatEmptyAsNull(row.getMaxSize());
		if(size != null){
			model.setMaximumSize(Long.parseLong(size));
		}
		if(row.getEnumValues() != null && !row.getEnumValues().isEmpty()){
			model.setEnumValues(row.getEnumValues());
		}
		model.setDefaultValue(treatEmptyAsNull(row.getDefaultValue()));
		return model;
	}
	
	/**
	 * Convert the passed list of strings to a CSV.
	 * @param list
	 * @return
	 */
	public static String listToCSV(List<String> list){
		StringBuilder builder = new StringBuilder();
		if(list != null){
			int index = 0;
			for(String value: list){
				if(index > 0){
					builder.append(", ");
				}
				builder.append(value);
				index++;
			}
		}
		return builder.toString();
	}
	
	/**
	 * Convert a CSV to a list of strings
	 * @param csv
	 * @return
	 */
	public static List<String> csvToList(String csv){
		List<String> list = new LinkedList<String>();
		if(csv != null){
			String[] split = csv.split(",");
			for(String value: split){
				String trim = value.trim();
				if(!"".equals(trim)){
					list.add(value.trim());
				}
			}
		}
		return list;
	}
}