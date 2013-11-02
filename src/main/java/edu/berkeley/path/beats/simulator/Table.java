/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.berkeley.path.beats.jaxb.ColumnName;

/** 
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
final public class Table {
	
	protected ArrayList<Long> column_ids;
	protected ArrayList<String> column_names;				// list of column ids
	protected Map<String,MyRow> key_row;				// map row key (String) / row 
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public Table(edu.berkeley.path.beats.jaxb.Table T) {	
		
		// check column names has single key
//		int numkey = 0;
//		for(ColumnName cn : T.getColumnNames().getColumnName())
//			numkey += cn.isKey() ? 1 : 0;
		//if(numkey!=1)
		//	throw new BeatsException("Exaclty one column in a table must be a key.");
		
		// populate column_names, column_ids, column_is_key
		column_names = new ArrayList<String>();
		column_ids = new ArrayList<Long>();
		ArrayList<Boolean> column_is_key = new ArrayList<Boolean>();
		for(ColumnName cn : T.getColumnNames().getColumnName()){
			column_ids.add(cn.getId());
			column_is_key.add(cn.isKey());
			column_names.add(cn.getName());
		}

		// populate key_row
		key_row = new HashMap<String,MyRow>();
		for(edu.berkeley.path.beats.jaxb.Row r : T.getRow()){
			MyRow newrow = new MyRow(column_names.size());
			String mykey = "";
			for(edu.berkeley.path.beats.jaxb.Column c : r.getColumn()){
				int index = column_ids.indexOf(c.getId());
				if(index<0)
					continue;
				newrow.set_value(index,c.getContent(),column_is_key.get(index));		
			}
			if(newrow.hasKey())
				key_row.put(mykey,newrow);			
		}
		
	}

//	public Table(ArrayList<String> columnNames,	ArrayList<ArrayList<String>> rows) {
//		ColumnNames = columnNames;
//		Rows = rows;
//	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	/** Checks that each row has the same number of columns as the column_names. Also checks for unique column names*/  
//	public boolean checkTable(){
//		if (ColumnNames.size() !=(new HashSet<String>(ColumnNames)).size())
//			return false;
//		for (ArrayList<String> r: Rows){
//			if (r.size()!=ColumnNames.size())
//				return false;
//		}
//		return true;
//	}
	
	/** Returns number of rows in the table*/
	public int getNoRows(){
		return key_row.size();
	}
	
	/** Returns the number of columns in the table*/ 
	public int getNoColumns(){		
		return 	column_names.size();
	}
	
	/** Returns the column number corresponding to the given column_name*/ 
	public int getColumnNo(String cname){		
		return 	column_names.indexOf(cname);
	}
	
	public String getColumnNameForId(Long id){
		int index = column_ids.indexOf(id);
		if(index<0)
			return null;
		return column_names.get(index);
	}
	
//	/** Returns an element in the table, indexed by row and column numbers*/
//	public String getTableElement(int RowNo,int ColumnNo){
//		try{
//			return (Rows.get(RowNo)).get(ColumnNo);
//		}
//		catch(IndexOutOfBoundsException  e){
//			return null;
//		}
//	}
//	
//	/** Returns an element in the table, indexed by row number and column name*/
//	public String getTableElement(int RowNo,String cname){
//		try{
//			return (Rows.get(RowNo)).get(this.getColumnNo(cname));
//		}
//		catch(IndexOutOfBoundsException  e){
//			return null;
//		}
//	}
	
	
	public class MyRow {
		public boolean haskey = false;;
		public String keyvalue;
		public ArrayList<String> column_name_value; 		// map from column name to value
		
		public MyRow(int num_columns){
			column_name_value = new ArrayList<String>();
			for(int i=0;i<num_columns;i++)
				column_name_value.add("");
		}
		public void set_value(int index,String value,boolean iskey){
			if(iskey){
				keyvalue = value;
				haskey = true;
			}
			column_name_value.set(index, value);
		}
		public boolean hasKey(){
			return haskey;
		}
	}
	
}


