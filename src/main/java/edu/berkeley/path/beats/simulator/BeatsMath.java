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
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/** XXX. 
 * YYY
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public final class BeatsMath {
	
	private static Random random = new Random();
	private static final double EPSILON = (double) 1e-4;
	
 	public static Double [] zeros(int n1){
 		if(n1<0)
 			return null;
		Double [] answ = new Double [n1];
		for(int i=0;i<n1;i++)
			answ[i] = 0.0;
		return answ;	
	}
 	
 	public static Double [][] zeros(int n1,int n2){
 		if(n1<0 || n2<0)
 			return null;
		Double [][] answ = new Double [n1][n2];
		int i,j;
		for(i=0;i<n1;i++)
			for(j=0;j<n2;j++)
				answ[i][j] = 0.0;
		return answ;
	}
	
	public static Double sum(Double [] V){
		if(V==null)
			return null;
		Double answ = 0d;
		for(int i=0;i<V.length;i++)
			if(V[i]!=null)
				answ += V[i];
		return answ;
	}
	
	public static Double sum(Collection<Double> V) {
		if (null == V) 
			return null;
		Double ans = .0d;
		Iterator<Double> iter = V.iterator();
		while (iter.hasNext()) ans += iter.next();
		return ans;
	}

	public static Double [] sum(Double [][] V,int dim){
		if(V==null)
			return null;
		if(V.length==0)
			return null;
		if(V[0].length==0)
			return null;
		Double [] answ;
		int i,j;
		int n1 = V.length;
		int n2 = V[0].length;
		switch(dim){
		case 1:
			answ = new Double[n2];
			for(i=0;i<V.length;i++)
				for(j=0;j<V[i].length;j++){
					if(answ[j]==null)
						answ[j]=0d;
					if(V[i][j]!=null)
						answ[j] += V[i][j];
				}
			return answ;
		case 2:
			answ = new Double[n1];
			for(i=0;i<V.length;i++){
				answ[i]=0d;
				for(j=0;j<V[i].length;j++)
					if(V[i][j]!=null)
						answ[i] += V[i][j];
			}
			return answ;
		default:
			return null;
		}
	}

	public static Double [] times(Double [] V,double a){
		if(V==null)
			return null;
		Double [] answ = new Double [V.length];
		for(int i=0;i<V.length;i++)
			answ[i] = a*V[i];
		return answ;
	}
	
	public static int ceil(double a){
		return (int) Math.ceil(a-BeatsMath.EPSILON);
	}
	
	public static int floor(double a){
		return (int) Math.floor(a+BeatsMath.EPSILON);
	}
	
	public static int round(double a){
		return (int) Math.round(a);
	}
	
	public static boolean any (boolean [] x){
		if(x==null)
			return false;
		if(x.length==0)
			return false;
		for(int i=0;i<x.length;i++)
			if(x[i])
				return true;
		return false;
	}
	
	public static boolean all (boolean [] x){
		if(x==null)
			return false;
		if(x.length==0)
			return false;
		for(int i=0;i<x.length;i++)
			if(!x[i])
				return false;
		return true;
	}
	
	public static boolean[] not(boolean [] x){
		if(x==null)
			return null;
		if(x.length==0)
			return null;
		boolean [] y = x.clone();
		for(int i=0;i<y.length;i++)
			y[i] = !y[i];
		return y;
	}
	
	public static int count(boolean [] x){
		if(x==null)
			return 0;
		if(x.length==0)
			return 0;
		int s = 0;
		for(int i=0;i<x.length;i++)
			if(x[i])
				s++;
		return s;
	}
	
	public static ArrayList<Integer> find(boolean [] x){
		if(x==null)
			return null;
		ArrayList<Integer> r = new ArrayList<Integer>();
		for(int i=0;i<x.length;i++)
			if(x[i])
				r.add(i);
		return r;
	}
	
	public static boolean isintegermultipleof(Double A,Double a){
		if(A.isInfinite())
			return true;
		if(A==0)
			return true;
		if(a==0)
			return false;
		boolean result;
		result = BeatsMath.equals( BeatsMath.round(A/a) , A/a );
		result &=  A/a>0;
		return result;
	}
	
	public static boolean equals(double a,double b){
		return Math.abs(a-b) < BeatsMath.EPSILON;
	}	
	
	public static boolean equals1D(ArrayList<Double> a,ArrayList<Double> b){
		if(a==null || b==null)
			return false;
		if(a.size()!=b.size())
			return false;
		for(int i=0;i<a.size();i++){
			if(a.get(i).isNaN() && b.get(i).isNaN())
				continue;
			if(a.get(i).isInfinite() && b.get(i).isInfinite())
				continue;
			if( !BeatsMath.equals(a.get(i), b.get(i)) )
				return false;
		}
		return true;
	}
	
	public static boolean equals2D(ArrayList<ArrayList<Double>> a,ArrayList<ArrayList<Double>> b){
		if(a==null || b==null)
			return false;
		if(a.size()!=b.size())
			return false;
		for(int i=0;i<a.size();i++)
			if( !BeatsMath.equals1D(a.get(i), b.get(i)) )
				return false;
		return true;
	}
	
	public static boolean equals3D(ArrayList<ArrayList<ArrayList<Double>>> a,ArrayList<ArrayList<ArrayList<Double>>> b){
		if(a==null || b==null)
			return false;
		if(a.size()!=b.size())
			return false;
		for(int i=0;i<a.size();i++)
			if( !BeatsMath.equals2D(a.get(i), b.get(i)) )
				return false;
		return true;
	}
	
	public static boolean greaterthan(double a,double b){
		return a > b + BeatsMath.EPSILON;
	}

	public static boolean greaterorequalthan(double a,double b){
		return !lessthan(a,b);
	}
	
	public static boolean lessthan(double a,double b){
		return a < b - BeatsMath.EPSILON;
	}

	public static boolean lessorequalthan(double a,double b){
		return !greaterthan(a,b);
	}
	
	// greatest common divisor of two integers
	public static int gcd(int p, int q) {
		if (q == 0) {
			return p;
		}
		return gcd(q, p % q);
	}

	// deep copy a double array
	public static Double[][] makecopy(Double [][]x){
		if(x==null)
			return null;
		if(x.length==0)
			return null;
		if(x[0].length==0)
			return null;
		int n1 = x.length;
		int n2 = x[0].length;
		Double [][] y = new Double[n1][n2];
		int i,j;
		for(i=0;i<n1;i++)
			for(j=0;j<n2;j++)
				y[i][j]=x[i][j];
		return y;
	}

	public static double sampleZeroMeanUniform(double std_dev){
		return std_dev*Math.sqrt(3)*(2*BeatsMath.random.nextDouble()-1);
	}
	
	public static double sampleZeroMeanGaussian(double std_dev){
		return std_dev*BeatsMath.random.nextGaussian();
	}
}
