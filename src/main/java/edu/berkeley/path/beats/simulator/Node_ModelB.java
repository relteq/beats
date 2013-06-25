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

public class Node_ModelB extends Node {

	

    /*
    private Float3DMatrix resolveUnassignedSplits_B(SR){

        // GCG: take care of case single class
        
        for(i=0;i<nIn;i++){
            for(k=0;k<nTypes;k++){
                
                sr_j = SR(i,:,k);                            // 1 x nOut
                
                if(~any(sr_j<0))
                    continue;
                
                sr_pos = sr_j;
                sr_pos(sr_pos<0) = 0;
                phi = find(sr_j<0);             // possible destinations
                
                phi_dsratio = dsratio(phi);
                
                // classes are sorted in order of increasing congestion
                dsratio_class = sort(unique(phi_dsratio),2,'ascend');
                
                // class z has members phi(isinclass(z,:))
                numclasses = length(dsratio_class);
                isinclass = false(numclasses,length(phi));
                for(z=0;z<numclasses;z++)
                    isinclass(z,phi_dsratio==dsratio_class(z)) = true;
                
                // for each class compute the demand needed to get to the next class
                Delta = zeros(numclasses-1,1);
                for(z=0;z<numclasses-1;z++){
                    myphi = phi(isinclass(z,:));
                    Delta(z) = sum( outSupply(myphi)*dsratio_class(z+1) - outDemandKnown(myphi) );
                }
                
                // flow needed to raise classes
                if(numclasses==1)
                    flowtolevel = inf;
                else
                    flowtolevel = [cumsum(Delta.*(1:numclasses-1)) inf];    // 1xnumclasses
                
                // numclassups = n then remainingDemand is sufficient to unite classes 1..n, but not {1..n} and n+1
                remainingSplit = 1-sum(sr_pos);
                remainingDemand = inDemand(i,k)*remainingSplit;
                numclassmerge = find(remainingDemand<flowtolevel,1,'first');
                
                // flowtolevel(numclassmerge-1) is flow used to
                // level off classes. Distribute the remainder
                // equally among unassigned outputs
                if(numclassmerge>1)
                    levelflow = flowtolevel(numclassmerge-1);
                else
                    levelflow = 0;
                
                leftoverperclass = (remainingDemand-levelflow)/numclassmerge;
                
                for(z=0;z<numclasses;z++){
                    
                    flowtoclass = 0;
                    if(numclassmerge>z)
                        flowtoclass = sum(Delta(z:end));
                    
                    if(numclassmerge>=z)
                        flowtoclass = flowtoclass + leftoverperclass;
                    
                    // distribute among class members
                    myphi = phi(isinclass(z,:));
                    phishare = outSupply(myphi)/sum(outSupply(myphi));
                    flowtophi = flowtoclass*phishare;
                    
                    // save in SR matrix
                    if(inDemand(i,k)>0)
                        SR(i,myphi,k) = flowtophi/inDemand(i,k);
                    else{
                        SR(i,myphi,k) = 0;
                        s = sum(SR(i,:,k));
                        if(s>0)
                            SR(i,:,k) = SR(i,:,k)/s;
                        else
                            SR(i,1,k) = 1;
                    }
                }
            }
        }
    }
*/
	
}
