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

import edu.berkeley.path.beats.simulator.Signal.NEMA;

/** XXX. 
 * YYY
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
final public class SignalPhase {
	
	// references ....................................................
	protected Node myNode;
	protected Signal mySignal;
	protected Link [] targetlinks;	// THIS SHOULD BE TARGET INDICES TO THE SIGNAL PHASE CONTROLLER
	
	// properties ....................................................
	
	protected boolean protectd		= false;
	protected boolean isthrough		= false;
	protected boolean recall		= false;
	protected boolean permissive	= false;
	protected boolean lag 			= false;

	// dual ring structure
	protected int myRingGroup		= -1;
	protected SignalPhase opposingPhase;
	protected Signal.NEMA myNEMA   = Signal.NEMA.NULL;
	
	// Basic timing parameters
	protected float mingreen 			= 0f;
	protected float yellowtime 			= 0f;
	protected float redcleartime 		= 0f;
	protected float actualyellowtime 	= 0f;
	protected float actualredcleartime 	= 0f;

	// timers
	protected Clock bulbtimer;

	// State
	protected Signal.BulbColor bulbcolor;
	
	//protected int [] myControlIndex;

	// Detectors
	//protected DetectorStation ApproachStation = null;
	//protected DetectorStation StoplineStation = null;
	//protected Vector<Integer> ApproachStationIds;
	//protected Vector<Integer> StoplineStationIds;
	
	// Detector memory
	protected boolean hasstoplinecall		= false;
	protected boolean hasapproachcall		= false;
	protected boolean hasconflictingcall	= false;
	protected float conflictingcalltime		= 0f;

	// Controller memory
	protected boolean hold_requested 		= false;
	protected boolean forceoff_requested	= false;

	// Safety
	protected boolean permitopposinghold 	= true;
	protected boolean permithold			= true;

	protected int numapproachloops = 0;	
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public SignalPhase(Node myNode,Signal mySignal,double dt){
		this.myNode = myNode;
		this.mySignal = mySignal;
		this.bulbtimer = new Clock(0d,Double.POSITIVE_INFINITY,dt);		
	}

	/////////////////////////////////////////////////////////////////////
	// populate / rese / validate
	/////////////////////////////////////////////////////////////////////
	
	protected final void populateFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Phase jaxbPhase){
	
		int numlinks = jaxbPhase.getLinkReferences().getLinkReference().size();
		this.targetlinks = new Link[numlinks];
		for(int i=0;i<numlinks;i++){
			edu.berkeley.path.beats.jaxb.LinkReference linkref = jaxbPhase.getLinkReferences().getLinkReference().get(i);
			targetlinks[i] = myScenario.getLinkWithId(linkref.getId());
		}
		
		if(jaxbPhase.getNema()!=null)
			myNEMA = Signal.String2NEMA(jaxbPhase.getNema().toString());
		else
			myNEMA = Signal.NEMA.NULL;
		
		if(jaxbPhase.getMinGreenTime()!=null)
			this.mingreen = jaxbPhase.getMinGreenTime().floatValue();
		else
			this.mingreen = Defaults.mingreen;
		
		if(jaxbPhase.getRedClearTime()!=null)
			this.redcleartime = jaxbPhase.getRedClearTime().floatValue();
		else
			this.redcleartime = Defaults.redcleartime;

		if(jaxbPhase.getYellowTime()!=null)
			this.yellowtime = jaxbPhase.getYellowTime().floatValue();
		else
			this.yellowtime = Defaults.yellowtime;

		this.lag = jaxbPhase.isLag();
		this.permissive = jaxbPhase.isPermissive();
		this.protectd = jaxbPhase.isProtected();
		this.recall = jaxbPhase.isRecall();
		
		// actual yellow and red clear times
		this.actualyellowtime   = yellowtime;
		this.actualredcleartime = redcleartime;
		
		
		// dual ring structure: opposingPhase, isthrough, myRingGroup
		switch(myNEMA){
		case _1:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._2);
			isthrough = false;
			myRingGroup = 0;
			break;
		case _2:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._1);
			isthrough = true;
			myRingGroup = 0;
			break;
		case _3:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._4);
			isthrough = false;
			myRingGroup = 1;
			break;
		case _4:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._3);
			isthrough = true;
			myRingGroup = 1;
			break;
		case _5:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._6);
			isthrough = false;
			myRingGroup = 0;
			break;
		case _6:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._5);
			isthrough = true;
			myRingGroup = 0;
			break;
		case _7:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._8);
			isthrough = false;
			myRingGroup = 1;
			break;
		case _8:
			opposingPhase = mySignal.getPhaseForNEMA(NEMA._7);
			isthrough = true;
			myRingGroup = 1;
			break;
		case NULL:
			break;
		default:
			break;
		}		
	}
	
	protected void reset() {

		hasstoplinecall		= false;
		hasapproachcall		= false;
		hasconflictingcall	= false;
		conflictingcalltime	= 0f;
		hold_requested 		= false;
		forceoff_requested	= false;
		permithold			= true;
		permitopposinghold  = false;

		setPhaseColor(Signal.BulbColor.RED);
		bulbtimer.reset();
		
	}

	protected void validate() {

		// check that there are links attached
		if(targetlinks==null || targetlinks.length==0)
			BeatsErrorLog.addError("No valid target link for phase NEMA=" + getMyNEMA() + " in signal id=" + mySignal.getId());
		
		// target links are valid
		if(targetlinks!=null)
			for(int i=0;i<targetlinks.length;i++)
				if(targetlinks[i]==null)
					BeatsErrorLog.addError("Unknown link reference in phase NEMA=" + getMyNEMA() + " in signal id=" + mySignal.getId());
		
		// myNEMA is valid
		if(myNEMA.compareTo(Signal.NEMA.NULL)==0)
			BeatsErrorLog.addError("Invalid NEMA code in phase NEMA=" + getMyNEMA() + " in signal id=" + mySignal.getId());
		
		// numbers are positive
		if( mingreen<0 )
			BeatsErrorLog.addError("Negative mingreen=" + mingreen + " in signal id=" + mySignal.getId());

		if( yellowtime<0 )
			BeatsErrorLog.addError("Negative yellowtime=" + yellowtime + " in signal id=" + mySignal.getId());

		if( redcleartime<0 )
			BeatsErrorLog.addError("Negative redcleartime=" + redcleartime + " in signal id=" + mySignal.getId());
	}
	
	/////////////////////////////////////////////////////////////////////
	// protected
	/////////////////////////////////////////////////////////////////////
		
	protected void updatePermitOpposingHold(){
	
		switch(bulbcolor){

		case GREEN:
			// iff I am about to go off and there is no transition time
			permitopposinghold = forceoff_requested && actualyellowtime==0 && redcleartime==0;
			break;
		case YELLOW:
			// iff near end yellow time and there is no red clear time
			permitopposinghold =  BeatsMath.greaterorequalthan(bulbtimer.getT(),actualyellowtime-bulbtimer.dt) && redcleartime==0 ;
			break;
		case RED:	
			// iff near end of red clear time and not starting again.
			permitopposinghold =  BeatsMath.greaterorequalthan(bulbtimer.getT(),redcleartime-bulbtimer.dt) && !hold_requested;
			break;
		case DARK:
			break;
		default:
			break;
		}	

		
	}

	protected void update(boolean hold_approved,boolean forceoff_approved)
	{
		mySignal.completedPhases.clear();

		double bulbt = bulbtimer.getT();

		if(!protectd){
			if(permissive)
				return;
			else{
				setPhaseColor(Signal.BulbColor.RED);
				return;
			}
		}
		
		// execute this state machine until "done". May be more than once if 
		// some state has zero holding time (eg yellowtime=0)
		boolean done=false;
		
		while(!done){
			
			switch(bulbcolor){
	
			// .............................................................................................
			case GREEN:
	
				setPhaseColor(Signal.BulbColor.GREEN);
				
//				permitopposinghold = false;
					
				// Force off 
				if( forceoff_approved ){ 
					setPhaseColor(Signal.BulbColor.YELLOW);
					mySignal.completedPhases.add(mySignal.new PhaseData(myNEMA, mySignal.myScenario.clock.getT() - bulbtimer.getT(), bulbtimer.getT()));
					bulbtimer.reset();
					//FlushAllStationCallsAndConflicts();
					done = actualyellowtime>0;
				}
				else
					done = true;

				break;
	
			// .............................................................................................
			case YELLOW:
				
				setPhaseColor(Signal.BulbColor.YELLOW);
				
				// set permitopposinghold one step ahead of time so that other phases update correctly next time.
//				permitopposinghold = false;
				
				
//				if( BeatsMath.greaterorequalthan(bulbt,actualyellowtime-bulbtimer.dt) && redcleartime==0)
//					permitopposinghold = true;

				// yellow time over, go immediately to red if redcleartime==0
				if( BeatsMath.greaterorequalthan(bulbt,actualyellowtime) ){
					setPhaseColor(Signal.BulbColor.RED);
					bulbtimer.reset();
					done = redcleartime>0;
				}
				else
					done = true;
				break;
	
			// .............................................................................................
			case RED:
	
				setPhaseColor(Signal.BulbColor.RED);
	
				//if( BeatsMath.greaterorequalthan(bulbt,redcleartime-myNode.getMyNetwork().getTP()*3600f  && !goG )
//				if( BeatsMath.greaterorequalthan(bulbt,redcleartime-bulbtimer.dt) && !hold_approved )
//					permitopposinghold = true;
//				else
//					permitopposinghold = false;
	
				// if hold, set to green, go to green, etc.
				if( hold_approved ){ 
					setPhaseColor(Signal.BulbColor.GREEN);
					bulbtimer.reset();
	
					// Unregister calls (for reading conflicting calls)
					//FlushAllStationCallsAndConflicts(); // GCG ?????
					
					done = !forceoff_approved;
				}
				else
					done = true;
	
				break;
			case DARK:
				break;
			default:
				break;
			}
			
		}
	}
	
	protected void setPhaseColor(Signal.BulbColor color){
		mySignal.myPhaseController.setPhaseColor(myNEMA,color);
		bulbcolor = color;
	}

	/////////////////////////////////////////////////////////////////////
	// public
	/////////////////////////////////////////////////////////////////////		
	
	public float getYellowtime() {
		return yellowtime;
	}

	public float getRedcleartime() {
		return redcleartime;
	}

	public float getMingreen() {
		return mingreen;
	}

	public Signal.NEMA getMyNEMA() {
		return myNEMA;
	}
	
	public float getActualyellowtime() {
		return actualyellowtime;
	}

	public void setActualyellowtime(float actualyellowtime) {
		if(BeatsMath.lessthan(actualyellowtime,0d))
			return;
		this.actualyellowtime = actualyellowtime;
	}
	
	public float getActualredcleartime() {
		return actualredcleartime;
	}

	public void setActualredcleartime(float actualredcleartime) {
		if(BeatsMath.lessthan(actualredcleartime,0d))
			return;
		this.actualredcleartime = actualredcleartime;
	}

	public Signal.BulbColor getBulbcolor() {
		return bulbcolor;
	}
		
}
