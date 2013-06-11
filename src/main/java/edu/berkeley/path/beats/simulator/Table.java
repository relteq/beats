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
import java.util.HashSet;

/** 
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
final public class Table {
	
	protected ArrayList<String> ColumnNames;			// List of Column names
	protected ArrayList<ArrayList<String>> Rows;		// List of Rows. Each row contains a list of strings denoting different columns
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public Table(edu.berkeley.path.beats.jaxb.Table T1){	
		Rows = new ArrayList<ArrayList<String>>();
		ColumnNames=new ArrayList<String>();
		for (edu.berkeley.path.beats.jaxb.Row row : T1.getRow())
			Rows.add((ArrayList<String>) row.getColumn());
		for (edu.berkeley.path.beats.jaxb.ColumnName colname : T1.getColumnNames().getColumnName())
			ColumnNames.add(colname.getName());
	}

	public Table(ArrayList<String> columnNames,	ArrayList<ArrayList<String>> rows) {
		super();
		ColumnNames = columnNames;
		Rows = rows;
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	/** Checks that each row has the same number of columns as the column_names. Also checks for unique column names*/  
	public boolean checkTable(){
		if (ColumnNames.size() !=(new HashSet<String>(ColumnNames)).size())
			return false;
		for (ArrayList<String> r: Rows){
			if (r.size()!=ColumnNames.size())
				return false;
		}
		return true;
	}
	
	/** Returns number of rows in the table*/
	public int getNoRows(){
		return Rows.size();
	}
	
	/** Returns the number of columns in the table*/ 
	public int getNoColumns(){		
		return 	ColumnNames.size();
	}
	
	/** Returns the column number corresponding to the given column_name*/ 
	public int getColumnNo(String cname){		
		return 	ColumnNames.indexOf((Object) cname);
	}
	
	/** Returns an element in the table, indexed by row and column numbers*/
	public String getTableElement(int RowNo,int ColumnNo){
		try{
			return (Rows.get(RowNo)).get(ColumnNo);
		}
		catch(IndexOutOfBoundsException  e){
			return null;
		}
	}
	
	/** Returns an element in the table, indexed by row number and column name*/
	public String getTableElement(int RowNo,String cname){
		try{
			return (Rows.get(RowNo)).get(this.getColumnNo(cname));
		}
		catch(IndexOutOfBoundsException  e){
			return null;
		}
	}
	
}


