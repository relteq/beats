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

/** Signal class.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class Signal extends edu.berkeley.path.beats.jaxb.Signal {
	
	private HashMap<NEMA,SignalPhase> nema2phase;
	private Scenario myScenario;
	private Node myNode;
	private PhaseController myPhaseController;	// used to control capacity on individual links
	private SignalPhase [] phase;	
	
	// local copy of the command, subject to checks
	private boolean [] hold_approved;
	private boolean [] forceoff_approved;
	
	private ArrayList<PhaseData> completedPhases = new ArrayList<PhaseData>(); // used for output

	/** Commands that may be issued by a controller to each signal phase.  */
	public static enum CommandType {
		/** Activate this phase **/		hold,
		/** Deactivate this phase **/ 	forceoff };
	
	/** Phase indications. */	
	protected static enum BulbColor {
		/** Green light **/		GREEN,
		/** Yellow light **/	YELLOW,
		/** Red light **/		RED,
		/** Bulb off **/		DARK };
		
	/** NEMA phase codes */
	public static enum NEMA {
		/** null **/	NULL,
		/** EB left **/		_1,
		/** WB through **/	_2,
		/** SB left **/		_3,
		/** NB through **/	_4,
		/** WB left **/		_5,
		/** EB through **/	_6,
		/** NB left **/		_7,
		/** SB through **/	_8};
				
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;
		this.myNode = myScenario.getNodeWithId(getNodeId());
		
		if(myNode==null)
			return;
		
//		myNode.mySignal = this;

		int i;
		int totalphases = getPhase().size();
		
		if(totalphases==0)
			return;
		
		// ignore phases without targets, or !permissive && !protected
		// (instead of throwing validation error, because network editor 
		// generates phases like this).
		boolean [] isvalid = new boolean[totalphases];
		int numlinks;
		int numvalid = 0;
		for(i=0;i<getPhase().size();i++){
			edu.berkeley.path.beats.jaxb.Phase p = getPhase().get(i);
			isvalid[i] = true;
			isvalid[i] &= p.isPermissive() || p.isProtected();
			if (null == p.getLinkReferences())
				numlinks = 0;
			else
				numlinks = p.getLinkReferences().getLinkReference().size();
			isvalid[i] &= numlinks>0;
			numvalid += isvalid[i] ? 1 : 0;
		}
		
		phase = new SignalPhase[numvalid];
		nema2phase = new HashMap<NEMA,SignalPhase>(numvalid);
		int c = 0;
		for(i=0;i<getPhase().size();i++){
			if(!isvalid[i])
				continue;
			phase[c] = new SignalPhase(myNode,this,myScenario.getSimdtinseconds());
			phase[c].populateFromJaxb(myScenario,getPhase().get(i));
			nema2phase.put(phase[c].getNEMA(),phase[c]);
			c++;
		}
		
		hold_approved = new boolean[phase.length];
		forceoff_approved = new boolean[phase.length];
		
		// create myPhaseController. This is used to implement flow control on target links
		myPhaseController = new PhaseController(this);
		
		// nema2phase

		
	}

	protected void reset() {
		if(myNode==null)
			return;
		for(SignalPhase p : phase)
			p.reset();
	}
	
	protected void validate() {
		
		if(myNode==null){
			BeatsErrorLog.addWarning("Unknow node id=" + getNodeId() + " in signal id=" + getId());
			return; // this signal will be ignored
		}
		
		if(phase==null)
			BeatsErrorLog.addError("Signal id=" + getId() + " contains no valid phases.");

		if(phase!=null)	
			for(SignalPhase p : phase)
				p.validate();
		
	}

	protected void update() {

		if(myNode==null)
			return;
		
		int i;
		
		// 0) Advance all phase timers ...........................................
		for(SignalPhase p:phase)
			p.getBulbtimer().advance();
		
		// 1) Update detector stations ............................................
		/*
		for(i=0;i<8;i++)
			phase.get(i).UpdateDetectorStations();
		*/
		
		// 2) Read phase calls .....................................................
/*
		// Update stopline calls
		for(i=0;i<8;i++){
			if( phase.get(i).Recall() ){
				hasstoplinecall[i] = true;
				continue;
			}
			if( phase.get(i).StoplineStation()!=null && phase.get(i).StoplineStation().GotCall() )
				hasstoplinecall[i] = true;
			else
				hasstoplinecall[i] = false;
		}

		// Update approach calls
		for(i=0;i<8;i++){
			if( phase.get(i).ApproachStation()!=null && phase.get(i).ApproachStation().GotCall() )
				hasapproachcall[i] = true;
			else
				hasapproachcall[i] = false;
		}

		// Update conflicting calls
		boolean[] currentconflictcall = new boolean[8];
		for(i=0;i<8;i++)
			currentconflictcall[i] = CheckForConflictingCall(i);
		for(i=0;i<8;i++){
			if(  !hasconflictingcall[i] && currentconflictcall[i] )
				conflictingcalltime[i] = (float)(myNode.getMyNetwork().getSimTime()*3600f);
			hasconflictingcall[i] = currentconflictcall[i];
		}	
*/	

		for(SignalPhase pA:phase)
			pA.updatePermitOpposingHold();
		
		// 3) Update permitted holds ............................................
		for(SignalPhase pA:phase){
			pA.setPermithold(true);
			for(SignalPhase pB:phase)
				if(!isCompatible(pA,pB) && !pB.isPermitopposinghold() )
					pA.setPermithold(false);
		}
		
		// 4) Update signal commands ...................................................
		
		// Throw away conflicting hold pairs 
		// (This is purposely drastic to create an error)
		for(SignalPhase pA:phase){
			if(pA.isHold_requested()){
				for(SignalPhase pB:phase){
					if( pB.isHold_requested() && !isCompatible(pA,pB) ){
						pA.setHold_requested(false);
						pB.setHold_requested(false);
					}
				}
			}
		}

		// Deal with simultaneous hold and forceoff (RHODES needs this)
		for(SignalPhase pA:phase){
			if( pA.isHold_requested() && pA.isForceoff_requested() ){
				pA.setForceoff_requested(false);
			}
		}

		// Make local relaying copy
		for(i=0;i<phase.length;i++){
			hold_approved[i]     = phase[i].isHold_requested();
			forceoff_approved[i] = phase[i].isForceoff_requested();
		}

		// No transition if no permission
		for(i=0;i<phase.length;i++)
			if( !phase[i].isPermithold() )
				hold_approved[i] = false;

		// No transition if green time < mingreen
		for(i=0;i<phase.length;i++)
			if( phase[i].getBulbColor().compareTo(BulbColor.GREEN)==0  && BeatsMath.lessthan(phase[i].getBulbtimer().getT(),phase[i].getMingreen()) )
				forceoff_approved[i] = false;
		
		// Update all phases
		for(i=0;i<phase.length;i++)
			phase[i].update(hold_approved[i],forceoff_approved[i]);

		// Remove serviced commands 
		for(SignalPhase pA: phase){
			if(pA.getBulbColor().compareTo(Signal.BulbColor.GREEN)==0)
				pA.setHold_requested(false);
			if(pA.getBulbColor().compareTo(Signal.BulbColor.YELLOW)==0 || pA.getBulbColor().compareTo(Signal.BulbColor.RED)==0 )
				pA.setForceoff_requested(false);
		}
	
		// Set permissive opposing left turn to yellow
		// opposing is yellow if I am green or yellow, and I am through, and opposing is permissive
		// opposing is red if I am red and it is not protected
		for(i=0;i<phase.length;i++){
			SignalPhase p = phase[i];
			SignalPhase o = phase[i].getOpposingPhase();
			if(o==null)
				continue;
			switch(p.getBulbColor()){
				case GREEN:
				case YELLOW:
					if(p.isIsthrough() && o.isPermissive())
						o.setPhaseColor(Signal.BulbColor.YELLOW);
					break;
				case RED:
					if(!o.isProtected())
						o.setPhaseColor(Signal.BulbColor.RED);
					break;
			case DARK:
				break;
			default:
				break;
			}
		}
		
	}

	/////////////////////////////////////////////////////////////////////
	// protected
	/////////////////////////////////////////////////////////////////////
	
	protected boolean register(){
		if(myNode==null)
			return true;
		else
			return myPhaseController.register();
	}

	protected SignalPhase getPhaseForNEMA(NEMA nema){
		for(SignalPhase p:phase){
			if(p!=null)
				if(p.getNEMA().compareTo(nema)==0)
					return p;
		}
		return null;
	}
	
	protected PhaseController getMyPhaseController() {
		return myPhaseController;
	}

	protected java.util.List<PhaseData> getCompletedPhases() {
		return completedPhases;
	}
	
	/////////////////////////////////////////////////////////////////////
	// public methods
	/////////////////////////////////////////////////////////////////////
	
	public SignalPhase getPhaseByNEMA(Signal.NEMA nema){
		if(nema==null)
			return null;
		return nema2phase.get(nema);
	}

	public Scenario getMyScenario() {
		return myScenario;
	}

	public void requestCommand(ArrayList<Signal.Command> command){
		for(Signal.Command c : command){
			SignalPhase p = nema2phase.get(c.nema);
			if(p==null)
				continue;
			
			switch(c.type){
			case forceoff:
				p.setForceoff_requested(true);
				p.setActualyellowtime( c.yellowtime>=0 ? c.yellowtime : p.getYellowtime() ); 	
				p.setActualredcleartime( c.redcleartime>=0 ? c.redcleartime : p.getRedcleartime() ); 
				break;

			case hold:
				p.setHold_requested(true);
				break;
				
			}
		}
	}
		
	/////////////////////////////////////////////////////////////////////
	// static NEMA methods
	/////////////////////////////////////////////////////////////////////
	
	public static Signal.NEMA String2NEMA(String str){
		if(str==null)
			return Signal.NEMA.NULL;
		if(str.isEmpty())
			return Signal.NEMA.NULL;
		if(!str.startsWith("_"))
			str = "_"+str;
		Signal.NEMA nema;
		try{
			nema = Signal.NEMA.valueOf(str);
		}
		catch(IllegalArgumentException  e){
			nema = Signal.NEMA.NULL;
		}
		return nema;
	}
	
	public static boolean isCompatible(SignalPhase pA,SignalPhase pB)
	{
		Signal.NEMA nemaA = pA.getNEMA();
		Signal.NEMA nemaB = pB.getNEMA();
		
		if(nemaA.compareTo(nemaB)==0)
			return true;

		if( !pA.isProtected() || !pB.isProtected() )
			return true;

		switch(nemaA){
		case _1:
		case _2:
			if(nemaB.compareTo(NEMA._5)==0 || nemaB.compareTo(NEMA._6)==0)
				return true;
			else
				return false;
		case _3:
		case _4:
			if(nemaB.compareTo(NEMA._7)==0 || nemaB.compareTo(NEMA._8)==0 )
				return true;
			else
				return false;
		case _5:
		case _6:
			if(nemaB.compareTo(NEMA._1)==0 || nemaB.compareTo(NEMA._2)==0 )
				return true;
			else
				return false;
		case _7:
		case _8:
			if(nemaB.compareTo(NEMA._3)==0 || nemaB.compareTo(NEMA._4)==0 )
				return true;
			else
				return false;
		case NULL:
			break;
		default:
			break;
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////

	/** XXX. 
	 * YYY
	 *
	 * @author Gabriel Gomes (gomes@path.berkeley.edu)
	 */
	@SuppressWarnings("rawtypes")
	public static class Command implements Comparable {
		public Signal.CommandType type;
		public Signal.NEMA nema;
		public double time;
		public double yellowtime;
		public double redcleartime;

		public Command(Signal.CommandType type,Signal.NEMA phase,double time){
			this.type = type;
			this.nema = phase;
			this.time = time;
			this.yellowtime = -1f;
			this.redcleartime = -1f;
		}
		
		public Command(Signal.CommandType type,Signal.NEMA phase,double time,double yellowtime,double redcleartime){
			this.type = type;
			this.nema = phase;
			this.time = time;
			this.yellowtime = yellowtime;
			this.redcleartime = redcleartime;
		}
		
		@Override
		public int compareTo(Object arg0) {
			
			if(arg0==null)
				return 1;
			
			int compare;
			Command that = (Command) arg0;
			
			// first ordering by time stamp
			Double thiststamp = this.time;
			Double thattstamp = that.time;
			compare = thiststamp.compareTo(thattstamp);
			if(compare!=0)
				return compare;

			// second ordering by phase
			Signal.NEMA thistphase = this.nema;
			Signal.NEMA thattphase = that.nema;
			compare = thistphase.compareTo(thattphase);
			if(compare!=0)
				return compare;
			
			// third ordering by type
			CommandType thisttype = this.type;
			CommandType thatttype = that.type;
			compare = thisttype.compareTo(thatttype);
			if(compare!=0)
				return compare;

			// fourth ordering by yellowtime
			Double thistyellowtime = this.yellowtime;
			Double thattyellowtime = that.yellowtime;
			compare = thistyellowtime.compareTo(thattyellowtime);
			if(compare!=0)
				return compare;

			// fifth ordering by redcleartime
			Double thistredcleartime = this.redcleartime;
			Double thattredcleartime = that.redcleartime;
			compare = thistredcleartime.compareTo(thattredcleartime);
			if(compare!=0)
				return compare;
			
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj==null)
				return false;
			else
				return this.compareTo((Command) obj)==0;
		}
		
	}
	
	/** XXX. 
	 * YYY
	 *
	 * @author Gabriel Gomes (gomes@path.berkeley.edu)
	 */
	public class PhaseData{
		public NEMA nema;
		public double starttime;
		public double greentime;
		public PhaseData(NEMA nema, double starttime, double greentime){
			this.nema = nema;
			this.starttime = starttime;
			this.greentime = greentime;
		}
	}
	
	// Each signal communicates with links via a PhaseController.
	// Phase controller does two things: a) it registers the signal control,
	// and b) it implements the phase indication. 
	/** XXX. 
	 * YYY
	 *
	 * @author Gabriel Gomes (gomes@path.berkeley.edu)
	 */
	protected class PhaseController extends Controller {

		private HashMap<Link,Integer> target2index;
		private HashMap<Signal.NEMA,Integer[]> nema2indices;
		
		public PhaseController(Signal mySignal){
			
			super();
			
			int i,j;
			
			// populate target2index
			int index = 0;
			target2index = new HashMap<Link,Integer>();
			for(i=0;i<mySignal.phase.length;i++)
				for(j=0;j<mySignal.phase[i].getTargetlinks().length;j++)
					target2index.put(mySignal.phase[i].getTargetlinks()[j],index++);
			
			// populate nema2indices
			nema2indices = new HashMap<Signal.NEMA,Integer[]>();
			for(i=0;i<mySignal.phase.length;i++){
				Integer [] indices = new Integer[mySignal.phase[i].getTargetlinks().length];
				for(j=0;j<mySignal.phase[i].getTargetlinks().length;j++)
					indices[j] = target2index.get(mySignal.phase[i].getTargetlinks()[j]);
				nema2indices.put(mySignal.phase[i].getNEMA(),indices);
			}
			
			control_maxflow = new double[target2index.size()];
		}
		
		@Override 
		public void populate(Object jaxbobject) {}
		
		@Override 
		public void update() throws BeatsException {}

		@Override
		public boolean register() {
	        for(Link link : target2index.keySet())
	        	if (null != link && !link.registerFlowController(this,target2index.get(link)))
	        		return false;
			return true;
		}
		
		@Override
		public boolean deregister() {
	        for(Link link : target2index.keySet())
	        	if(!link.deregisterFlowController(this))
	        		return false;
			return true;
		}
		
		protected void setPhaseColor(Signal.NEMA nema,Signal.BulbColor color){
			
			Integer [] indices = nema2indices.get(nema);
			if(indices==null)
				return;
			
			double maxflow;
			switch(color){
				case GREEN:
				case YELLOW:
					maxflow = Double.POSITIVE_INFINITY;
					break;
				case RED:
				case DARK:
					maxflow = 0d;
					break;
				default:
					maxflow = 0d;
					break;			
			}
			
			for(Integer index:indices)
				this.setControl_maxflow(index, maxflow);

		}
		
	}

}



