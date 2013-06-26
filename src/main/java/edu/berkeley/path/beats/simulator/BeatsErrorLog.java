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

/**
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public final class BeatsErrorLog {
	
	private static boolean haserror;
	private static boolean haswarning;
	private static enum level {Warning,Error};
	private static ArrayList<BeatsError> error = new ArrayList<BeatsError>();

	public static void clearErrorMessage(){
		error.clear();
		haserror = false;
		haswarning = false;
	}
	
	public static boolean haserror(){
		return haserror;
	}
	
	public static boolean haswarning(){
		return haswarning;
	}
	
	public static boolean hasmessage(){
		return !error.isEmpty();
	}

	public static String format(){
		String str = "";
		int c;
		if(haserror){
			str += "----------------------------------------\n";
			str += "ERRORS\n";
			str += "----------------------------------------\n";
			c=0;
			for(int i=0;i<error.size();i++){
				BeatsError e = error.get(i);
				if(e.mylevel.compareTo(BeatsErrorLog.level.Error)==0)
					str += ++c + ") " + e.description +"\n";
			}
		}
		if(haswarning){
			str += "----------------------------------------\n";
			str += "WARNINGS\n";
			str += "----------------------------------------\n";
			c=0;
			for(int i=0;i<error.size();i++){
				BeatsError e = error.get(i);
				if(e.mylevel.compareTo(BeatsErrorLog.level.Warning)==0)
					str += ++c + ") " + e.description + "\n";
			}
			
		}
		if (haserror || haswarning)
			str += "----------------------------------------\n";
		return str;
	}
	
	public static void print(){
		System.out.println(BeatsErrorLog.format());
	}

	public static void addError(String str){
		error.add(new BeatsError(str,BeatsErrorLog.level.Error));
		haserror = true;
	}

	public static void addWarning(String str){
		error.add(new BeatsError(str,BeatsErrorLog.level.Warning));
		haswarning = true;
	}
	
	/** XXX. 
	 * YYY
	 *
	 * @author Gabriel Gomes (gomes@path.berkeley.edu)
	 */
	public static class BeatsError {
		String description;
		BeatsErrorLog.level mylevel;
		public BeatsError(String description,BeatsErrorLog.level mylevel){
			this.description = description;
			this.mylevel = mylevel;
		}
	}

}
