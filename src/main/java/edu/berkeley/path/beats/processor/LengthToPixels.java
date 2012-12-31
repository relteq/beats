package edu.berkeley.path.beats.processor;

import java.util.ArrayList;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

public class LengthToPixels {
	
	private int totalPixels;
	private double totalLength;
	private double[] leftBoundary;
	private double increment;
	private double scale;
	private ArrayList<Long> idList;
	private ArrayList<Double> length;
	
	LengthToPixels(int n) {
		totalPixels = 1000;
		idList = new ArrayList<Long>(n);
		length = new ArrayList<Double>(n);
		leftBoundary = new double[n];
		totalLength = 0.0;
		increment = 10.0;
		scale = 1.0;

	}
	
	public void setScale(double s) { scale = s; }
	
	public void addElement(Long id) {
		
		String query = "SELECT length FROM links WHERE length>0 AND id="+id;
		Double l;
		try {
			 l = ((Record) BasePeer.executeQuery(query).get(0)).getValue(1).asDouble();
		} catch (TorqueException e) {
			
			l = 1.0;
			e.printStackTrace();
		} catch (DataSetException e) {
			
			l = 1.0;
			e.printStackTrace();
		}
		
		idList.add(id);
		length.add(l);
		
	}
	
	public void setBoundaries() {
		
		leftBoundary[0] = 0.0;
		for (int i=1; i<idList.size(); i++) {
			leftBoundary[i] = leftBoundary[i-1] + length.get(i-1);
		}
		
		totalLength = leftBoundary[idList.size()-1] + length.get(idList.size()-1);
		
		if ( totalLength > 0 )
			increment = totalPixels/totalLength; 
	}
	
	public double getLength(Long id) { return length.get(idList.indexOf(id)); }
	
	public void setXYZ(ContourChart cc, Long id, double y, double z) {
		
		int pos = idList.indexOf(id);
		double len = length.get(pos);
		int n = (int) (len*increment);
		
		
		for (int i=0; i<n; i++) {
			cc.setXYZ( ( leftBoundary[pos] + (double)i*len / (double)n )*scale, y, z);
		}
	}

}

