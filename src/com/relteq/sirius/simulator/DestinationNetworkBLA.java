package com.relteq.sirius.simulator;

import java.util.ArrayList;
import java.util.HashMap;

public final class DestinationNetworkBLA {
	
	protected com.relteq.sirius.jaxb.DestinationNetwork dnetwork;
	protected Scenario myScenario;
	protected int myIndex;
	
	public DestinationNetworkBLA(com.relteq.sirius.jaxb.DestinationNetwork dnetwork,Scenario myScenario,int myIndex){
		this.dnetwork = dnetwork;
		this.myScenario = myScenario;
		this.myIndex = myIndex;
	}
	
	protected void populate() {

		if(dnetwork.getLinkReferences()!=null){
			for(com.relteq.sirius.jaxb.LinkReference linkref : dnetwork.getLinkReferences().getLinkReference()){
				Link link = myScenario.getLinkWithId(linkref.getId());
				if(link!=null)
					link.addDestination(myIndex);
			}
		}
	}
}
