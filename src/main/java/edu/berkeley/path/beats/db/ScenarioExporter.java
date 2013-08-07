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

package edu.berkeley.path.beats.db;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Loads a scenario from the database
 */
public class ScenarioExporter {
//
//	/**
//	 * Loads a scenario from the database.
//	 * The loaded scenario is not ready for simulation:
//	 * it needs unit conversion, validation, etc
//	 * @param id
//	 * @return the raw restored scenario
//	 * @throws BeatsException
//	 */
//	public static edu.berkeley.path.beats.jaxb.Scenario doExport(Long id) throws BeatsException {
//		return new ScenarioExporter().restore(id);
//	}
//
//	private edu.berkeley.path.beats.simulator.JaxbObjectFactory factory = null;
//	private edu.berkeley.path.beats.util.polyline.DecoderIF polyline_decoder;
//
//	private ScenarioExporter() {
//		factory = new edu.berkeley.path.beats.simulator.JaxbObjectFactory();
//		polyline_decoder = new edu.berkeley.path.beats.util.polyline.GoogleDecoder();
//		polyline_decoder.setObjectFactory(factory);
//	}
//
//	private static Logger logger = Logger.getLogger(ScenarioExporter.class);
//
//	private edu.berkeley.path.beats.jaxb.Scenario restore(Long id) throws BeatsException {
//		edu.berkeley.path.beats.db.Service.ensureInit();
//		Scenarios db_scenario = null;
//		try {
//			db_scenario = ScenariosPeer.retrieveByPK(id);
//		} catch (NoRowsException exc) {
//			throw new BeatsException("Scenario " + id + " does not exist", exc);
//		} catch (TorqueException exc) {
//			throw new BeatsException(exc);
//		}
//		return restoreScenario(db_scenario);
//	}
//
//	/**
//	 * Converts a numeric ID to a string
//	 * @param id
//	 * @return String
//	 */
//	static String id2str(Long id) {
//		if (null == id) return null;
//		return id.toString();
//	}
//
//	private edu.berkeley.path.beats.jaxb.Scenario restoreScenario(Scenarios db_scenario) throws BeatsException {
//		if (null == db_scenario) return null;
//		edu.berkeley.path.beats.jaxb.Scenario scenario = factory.createScenario();
//		scenario.setId(db_scenario.getId());
//		scenario.setName(db_scenario.getName());
//		scenario.setDescription(db_scenario.getDescription());
//		try{
//			scenario.setSettings(restoreSettings(db_scenario));
//			scenario.setNetworkList(restoreNetworkList(db_scenario));
//			scenario.setSignalList(restoreSignalList(db_scenario.getSignalSets()));
//			scenario.setSensorList(restoreSensorList(db_scenario.getSensorSets()));
//			scenario.setInitialDensitySet(restoreInitialDensitySet(db_scenario.getInitialDensitySets()));
//			scenario.setWeavingFactorSet(restoreWeavingFactorSet(db_scenario.getWeavingFactorSets()));
//			scenario.setSplitRatioProfileSet(restoreSplitRatioProfileSet(db_scenario.getSplitRatioProfileSets()));
//			scenario.setDownstreamBoundaryCapacityProfileSet(restoreDownstreamBoundaryCapacity(db_scenario.getDownstreamBoundaryCapacityProfileSets()));
//			scenario.setEventSet(restoreEventSet(db_scenario.getEventSets()));
//			scenario.setDemandProfileSet(restoreDemandProfileSet(db_scenario.getDemandProfileSets()));
//			scenario.setControllerSet(restoreControllerSet(db_scenario.getControllerSets()));
//			scenario.setFundamentalDiagramProfileSet(restoreFundamentalDiagramProfileSet(db_scenario.getFundamentalDiagramProfileSets()));
//			scenario.setNetworkConnections(restoreNetworkConnections(db_scenario.getNetworkConnectionSets()));
//			scenario.setDestinationNetworks(restoreDestinationNetworks(db_scenario));
//			scenario.setRoutes(restoreRoutes(db_scenario));
//
//			@SuppressWarnings("unchecked")
//			List<DefSimSettings> db_defss = db_scenario.getDefSimSettingss();
//			if (db_defss.isEmpty())
//				logger.warn("Found no default simulation settings for scenario " + db_scenario.getId());
//			else if (1 < db_defss.size())
//				logger.error("Found " + db_defss.size() + " default simulation settings for scenario " + db_scenario.getId());
//			else if (null != scenario.getNetworkList()) {
//				logger.info("Default sample rate: " + db_defss.get(0).getSimDt() + " sec");
//				for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork())
//					network.setDt(db_defss.get(0).getSimDt());
//			}
//		} catch (TorqueException exc) {
//			throw new BeatsException(exc);
//		}
//		return scenario;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Settings restoreSettings(Scenarios db_scenario) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Settings settings = factory.createSettings();
//		settings.setUnits("SI");
//		settings.setVehicleTypes(restoreVehicleTypes(db_scenario.getVehicleTypeSets()));
//		return settings;
//	}
//
//	private edu.berkeley.path.beats.jaxb.VehicleTypes restoreVehicleTypes(VehicleTypeSets db_vtsets) throws TorqueException {
//		if (null == db_vtsets) return null;
//		Criteria crit = new Criteria();
//		crit.addJoin(VehicleTypesInSetsPeer.VEH_TYPE_ID, VehicleTypesPeer.ID);
//		crit.add(VehicleTypesInSetsPeer.VEH_TYPE_SET_ID, db_vtsets.getId());
//		crit.addAscendingOrderByColumn(VehicleTypesPeer.ID);
//		@SuppressWarnings("unchecked")
//		List<VehicleTypes> db_vt_l = VehicleTypesPeer.doSelect(crit);
//		if (db_vt_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.VehicleTypes vtypes = factory.createVehicleTypes();
//		for (VehicleTypes db_vt : db_vt_l)
//			vtypes.getVehicleType().add(restoreVehicleType(db_vt));
//		return vtypes;
//	}
//
//	private edu.berkeley.path.beats.jaxb.VehicleType restoreVehicleType(VehicleTypes db_vt) {
//		edu.berkeley.path.beats.jaxb.VehicleType vt = factory.createVehicleType();
//		vt.setName(db_vt.getName());
//		vt.setWeight(db_vt.getWeight());
//		return vt;
//	}
//
//	private edu.berkeley.path.beats.jaxb.NetworkList restoreNetworkList(Scenarios db_scenario) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<NetworkSets> db_nets_l = db_scenario.getNetworkSetss();
//		if (db_nets_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.NetworkList nets = factory.createNetworkList();
//		for (NetworkSets db_nets : db_nets_l)
//			nets.getNetwork().add(restoreNetwork(db_nets.getNetworks()));
//		return nets;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Network restoreNetwork(Networks db_net) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Network net = factory.createNetwork();
//		net.setId(db_net.getId());
//		net.setName(db_net.getName());
//		net.setDescription(db_net.getDescription());
//		// TODO net.setPosition();
//		net.setDt(new BigDecimal(1)); // TODO change this when the DB schema is updated
//		//net.setLocked(db_net.getLocked());
//		net.setNodeList(restoreNodeList(db_net));
//		net.setLinkList(restoreLinkList(db_net));
//		return net;
//	}
//
//	private edu.berkeley.path.beats.jaxb.NodeList restoreNodeList(Networks db_net) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<Nodes> db_nl = db_net.getNodess();
//		if (db_nl.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.NodeList nl = factory.createNodeList();
//		for (Nodes db_node : db_nl)
//			nl.getNode().add(restoreNode(db_node));
//		return nl;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Node restoreNode(Nodes db_node) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Node node = factory.createNode();
//		node.setId(db_node.getId());
//		node.setInSync(db_node.getInSync());
//
//		@SuppressWarnings("unchecked")
//		List<NodeTypeDet> db_ntd_l = db_node.getNodeTypeDets();
//		if (!db_ntd_l.isEmpty()) {
//			if (1 < db_ntd_l.size())
//				logger.warn("Found " + db_ntd_l.size() + " node types for node " + db_node.getId());
//			node.setType(db_ntd_l.get(0).getNodeTypes().getName());
//		} else
//			logger.warn("No node types for node " + db_node.getId());
//
//		node.setRoadwayMarkers(restoreRoadwayMarkers(db_node));
//		node.setInputs(restoreInputs(db_node));
//		node.setOutputs(restoreOutputs(db_node));
//		node.setPosition(restorePosition(db_node.getGeom()));
//		return node;
//	}
//
//	private edu.berkeley.path.beats.jaxb.RoadwayMarkers restoreRoadwayMarkers(Nodes db_node) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<NodeName> db_nname_l = db_node.getNodeNames();
//		@SuppressWarnings("unchecked")
//		List<Postmiles> db_postmile_l = db_node.getPostmiless();
//		if (db_nname_l.isEmpty() && db_postmile_l.isEmpty()) return null;
//
//		edu.berkeley.path.beats.jaxb.RoadwayMarkers markers = factory.createRoadwayMarkers();
//		for (NodeName db_nname : db_nname_l) {
//			edu.berkeley.path.beats.jaxb.Marker marker = factory.createMarker();
//			marker.setName(db_nname.getName());
//			markers.getMarker().add(marker);
//		}
//		for (Postmiles db_postmile : db_postmile_l) {
//			edu.berkeley.path.beats.jaxb.Marker marker = factory.createMarker();
//			marker.setName(db_postmile.getPostmileHighways().getHighwayName());
//			marker.setPostmile(db_postmile.getPostmile());
//			markers.getMarker().add(marker);
//		}
//		return markers;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Inputs restoreInputs(Nodes db_node) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(LinksPeer.NETWORK_ID, db_node.getNetworkId());
//		crit.add(LinksPeer.END_NODE_ID, db_node.getId());
//		@SuppressWarnings("unchecked")
//		List<Links> db_link_l = LinksPeer.doSelect(crit);
//		edu.berkeley.path.beats.jaxb.Inputs inputs = factory.createInputs();
//		for (Links db_link : db_link_l)
//			inputs.getInput().add(restoreInput(db_link));
//		return inputs;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Input restoreInput(Links db_link) {
//		edu.berkeley.path.beats.jaxb.Input input = factory.createInput();
//		input.setLinkId(db_link.getId());
//		return input;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Outputs restoreOutputs(Nodes db_node) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(LinksPeer.NETWORK_ID, db_node.getNetworkId());
//		crit.add(LinksPeer.BEG_NODE_ID, db_node.getId());
//		@SuppressWarnings("unchecked")
//		List<Links> db_link_l = LinksPeer.doSelect(crit);
//		edu.berkeley.path.beats.jaxb.Outputs outputs = factory.createOutputs();
//		for (Links db_link : db_link_l)
//			outputs.getOutput().add(restoreOutput(db_link));
//		return outputs;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Output restoreOutput(Links db_link) {
//		edu.berkeley.path.beats.jaxb.Output output = factory.createOutput();
//		output.setLinkId(db_link.getId());
//		return output;
//	}
//
//	private edu.berkeley.path.beats.jaxb.LinkList restoreLinkList(Networks db_net) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<Links> db_ll = db_net.getLinkss();
//		if (db_ll.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.LinkList ll = factory.createLinkList();
//		for (Links db_link : db_ll)
//			ll.getLink().add(restoreLink(db_link));
//		return ll;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Link restoreLink(Links db_link) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Link link = factory.createLink();
//		link.setId(db_link.getId());
//
//		// begin node
//		edu.berkeley.path.beats.jaxb.Begin begin = factory.createBegin();
//		begin.setNodeId(db_link.getBegNodeId());
//		link.setBegin(begin);
//
//		// end node
//		edu.berkeley.path.beats.jaxb.End end = factory.createEnd();
//		end.setNodeId(db_link.getEndNodeId());
//		link.setEnd(end);
//
//		link.setRoads(restoreRoads(db_link));
//		// TODO link.setDynamics();
//		link.setShape(db_link.getGeom()); // TODO revise: geometry -> shape
//
//		LinkLanes db_llanes = LinkLanesPeer.retrieveByPK(db_link.getId(), db_link.getNetworkId());
//		link.setLanes(db_llanes.getLanes());
//
//		@SuppressWarnings("unchecked")
//		List<LinkLaneOffset> db_lloffset_l = db_link.getLinkLaneOffsets();
//		if (!db_lloffset_l.isEmpty()) {
//			link.setLaneOffset(db_lloffset_l.get(0).getDisplayLaneOffset());
//			if (1 < db_lloffset_l.size())
//				logger.warn("Found " + db_lloffset_l.size() + " lane offsets for link[id=" + db_link.getId() + "]");
//		}
//
//		link.setLength(db_link.getLength());
//
//		LinkTypeDet db_ltdet = LinkTypeDetPeer.retrieveByPK(db_link.getId(), db_link.getNetworkId());
//		link.setType(db_ltdet.getLinkTypes().getName());
//
//		link.setInSync(db_link.getInSync());
//		return link;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Roads restoreRoads(Links db_link) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<LinkName> db_lname_l = db_link.getLinkNames();
//		if (db_lname_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.Roads roads = factory.createRoads();
//		for (LinkName db_lname : db_lname_l) {
//			edu.berkeley.path.beats.jaxb.Road road = factory.createRoad();
//			road.setName(db_lname.getName());
//			roads.getRoad().add(road);
//		}
//		return roads;
//	}
//
//	private edu.berkeley.path.beats.jaxb.InitialDensitySet restoreInitialDensitySet(InitialDensitySets db_idset) throws TorqueException {
//		if (null == db_idset) return null;
//		edu.berkeley.path.beats.jaxb.InitialDensitySet idset = factory.createInitialDensitySet();
//		idset.setId(db_idset.getId());
//		idset.setName(db_idset.getName());
//		idset.setDescription(db_idset.getDescription());
//		idset.setTstamp(db_idset.getActionTime());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(InitialDensitiesPeer.LINK_ID);
//		crit.addAscendingOrderByColumn(InitialDensitiesPeer.VEH_TYPE_ID);
//		@SuppressWarnings("unchecked")
//		List<InitialDensities> db_idl = db_idset.getInitialDensitiess(crit);
//		edu.berkeley.path.beats.jaxb.Density density = null;
//		StringBuilder sb = new StringBuilder();
//		for (InitialDensities db_id : db_idl) {
//			if (null != density && density.getLinkId()!=db_id.getLinkId()) {
//				density.setContent(sb.toString());
//				idset.getDensity().add(density);
//				density = null;
//			}
//			if (null == density) { // new link
//				density = factory.createDensity();
//				density.setLinkId(db_id.getLinkId());
//				density.setDestinationNetworkId(db_id.getDestinationNetworkId());
//				sb.setLength(0);
//			} else { // same link, different vehicle type
//				sb.append(":");
//			}
//			sb.append(db_id.getDensity().toPlainString());
//		}
//		// last link
//		if (null != density) {
//			density.setContent(sb.toString());
//			idset.getDensity().add(density);
//		}
//		return idset;
//	}
//
//	private edu.berkeley.path.beats.jaxb.WeavingFactorSet restoreWeavingFactorSet(WeavingFactorSets db_wfset) throws TorqueException {
//		if (null == db_wfset) return null;
//		edu.berkeley.path.beats.jaxb.WeavingFactorSet wfset = factory.createWeavingFactorSet();
//		wfset.setId(db_wfset.getId());
//		wfset.setName(db_wfset.getName());
//		wfset.setDescription(db_wfset.getDescription());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(WeavingFactorsPeer.IN_LINK_ID);
//		crit.addAscendingOrderByColumn(WeavingFactorsPeer.OUT_LINK_ID);
//		crit.addAscendingOrderByColumn(WeavingFactorsPeer.VEH_TYPE_ID);
//		@SuppressWarnings("unchecked")
//		List<WeavingFactors> db_wf_l = db_wfset.getWeavingFactorss(crit);
//		edu.berkeley.path.beats.jaxb.Weavingfactors wf = null;
//		StringBuilder sb = new StringBuilder();
//		for (WeavingFactors db_wf : db_wf_l) {
//			if (null != wf && !(wf.getLinkIn()==db_wf.getInLinkId() && wf.getLinkOut()==db_wf.getOutLinkId())) {
//				wf.setContent(sb.toString());
//				wfset.getWeavingfactors().add(wf);
//				wf = null;
//			}
//			if (null == wf) { // new weaving factor
//				wf = factory.createWeavingfactors();
//				wf.setLinkIn(db_wf.getInLinkId());
//				wf.setLinkOut(db_wf.getOutLinkId());
//				sb.setLength(0);
//			} else { // same weaving factor, different vehicle type
//				sb.append(':');
//			}
//			sb.append(db_wf.getFactor().toPlainString());
//		}
//		if (null != wf) {
//			wf.setContent(sb.toString());
//			wfset.getWeavingfactors().add(wf);
//		}
//		return wfset;
//	}
//
//	private edu.berkeley.path.beats.jaxb.SplitRatioProfileSet restoreSplitRatioProfileSet(SplitRatioProfileSets db_srps) throws TorqueException {
//		if (null == db_srps) return null;
//		edu.berkeley.path.beats.jaxb.SplitRatioProfileSet srps = factory.createSplitRatioProfileSet();
//		srps.setId(db_srps.getId());
//		srps.setName(db_srps.getName());
//		srps.setDescription(db_srps.getDescription());
//		@SuppressWarnings("unchecked")
//		List<SplitRatioProfiles> db_srp_l = db_srps.getSplitRatioProfiless();
//		for (SplitRatioProfiles db_srp : db_srp_l)
//			srps.getSplitratioProfile().add(restoreSplitRatioProfile(db_srp));
//		return srps;
//	}
//
//	private edu.berkeley.path.beats.jaxb.SplitratioProfile restoreSplitRatioProfile(SplitRatioProfiles db_srp) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.SplitratioProfile srp = factory.createSplitratioProfile();
//		srp.setNodeId(db_srp.getNodeId());
//		srp.setDt(db_srp.getSampleRate());
//		srp.setStartTime(db_srp.getStartTime());
//		srp.setDestinationNetworkId(db_srp.getDestinationNetworkId());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(SplitRatiosPeer.IN_LINK_ID);
//		crit.addAscendingOrderByColumn(SplitRatiosPeer.OUT_LINK_ID);
//		crit.addAscendingOrderByColumn(SplitRatiosPeer.RATIO_ORDER);
//		crit.addAscendingOrderByColumn(SplitRatiosPeer.VEH_TYPE_ID);
//		@SuppressWarnings("unchecked")
//		List<SplitRatios> db_sr_l = db_srp.getSplitRatioss(crit);
//		edu.berkeley.path.beats.jaxb.Splitratio sr = null;
//		Integer ordinal = null;
//		StringBuilder sb = new StringBuilder();
//		for (SplitRatios db_sr : db_sr_l) {
//			if (null != sr && !( sr.getLinkIn()==db_sr.getInLinkId() && sr.getLinkOut()==db_sr.getOutLinkId())) {
//				sr.setContent(sb.toString());
//				srp.getSplitratio().add(sr);
//				sr = null;
//			}
//			if (null == sr) { // new split ratio
//				sr = factory.createSplitratio();
//				sr.setLinkIn(db_sr.getInLinkId());
//				sr.setLinkOut(db_sr.getOutLinkId());
//				sb.setLength(0);
//			} else { // same split ratio, different time stamp (',') or vehicle type (':')
//				sb.append(db_sr.getOrdinal().equals(ordinal) ? ':' : ',');
//			}
//			ordinal = db_sr.getOrdinal();
//			sb.append(db_sr.getSplitRatio().toPlainString());
//		}
//		if (null != sr) {
//			sr.setContent(sb.toString());
//			srp.getSplitratio().add(sr);
//		}
//		return srp;
//	}
//
//	edu.berkeley.path.beats.jaxb.FundamentalDiagramProfileSet restoreFundamentalDiagramProfileSet(FundamentalDiagramProfileSets db_fdps) throws TorqueException {
//		if (null == db_fdps) return null;
//		edu.berkeley.path.beats.jaxb.FundamentalDiagramProfileSet fdps = factory.createFundamentalDiagramProfileSet();
//		fdps.setId(db_fdps.getId());
//		fdps.setName(db_fdps.getName());
//		fdps.setDescription(db_fdps.getDescription());
//		@SuppressWarnings("unchecked")
//		List<FundamentalDiagramProfiles> db_fdprofile_l = db_fdps.getFundamentalDiagramProfiless();
//		for (FundamentalDiagramProfiles db_fdprofile : db_fdprofile_l)
//			fdps.getFundamentalDiagramProfile().add(restoreFundamentalDiagramProfile(db_fdprofile));
//		return fdps;
//	}
//
//	edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile restoreFundamentalDiagramProfile(FundamentalDiagramProfiles db_fdprofile) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdprofile = factory.createFundamentalDiagramProfile();
//		fdprofile.setLinkId(db_fdprofile.getLinkId());
//		fdprofile.setDt(db_fdprofile.getSampleRate());
//		fdprofile.setStartTime(db_fdprofile.getStartTime());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(FundamentalDiagramsPeer.DIAG_ORDER);
//		@SuppressWarnings("unchecked")
//		List<FundamentalDiagrams> db_fd_l = db_fdprofile.getFundamentalDiagramss(crit);
//		for (FundamentalDiagrams db_fd : db_fd_l)
//			fdprofile.getFundamentalDiagram().add(restoreFundamentalDiagram(db_fd));
//		return fdprofile;
//	}
//
//	edu.berkeley.path.beats.jaxb.FundamentalDiagram restoreFundamentalDiagram(FundamentalDiagrams db_fd) {
//		edu.berkeley.path.beats.jaxb.FundamentalDiagram fd = factory.createFundamentalDiagram();
//		fd.setFreeFlowSpeed(db_fd.getFreeFlowSpeed());
//		fd.setCriticalSpeed(db_fd.getCriticalSpeed());
//		fd.setCongestionSpeed(db_fd.getCongestionWaveSpeed());
//		fd.setCapacity(db_fd.getCapacity());
//		fd.setJamDensity(db_fd.getJamDensity());
//		fd.setCapacityDrop(db_fd.getCapacityDrop());
//		fd.setStdDevCapacity(db_fd.getCapacityStd());
//		fd.setStdDevFreeFlowSpeed(db_fd.getFreeFlowSpeedStd());
//		fd.setStdDevCongestionSpeed(db_fd.getCongestionWaveSpeedStd());
//		return fd;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DemandProfileSet restoreDemandProfileSet(DemandProfileSets db_dpset) throws TorqueException {
//		if (null == db_dpset) return null;
//		edu.berkeley.path.beats.jaxb.DemandProfileSet dpset = factory.createDemandProfileSet();
//		dpset.setId(db_dpset.getId());
//		dpset.setName(db_dpset.getName());
//		dpset.setDescription(db_dpset.getDescription());
//		@SuppressWarnings("unchecked")
//		List<DemandProfiles> db_dp_l = db_dpset.getDemandProfiless();
//		for (DemandProfiles db_dp : db_dp_l)
//			dpset.getDemandProfile().add(restoreDemandProfile(db_dp));
//		return dpset;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DemandProfile restoreDemandProfile(DemandProfiles db_dp) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.DemandProfile dp = factory.createDemandProfile();
//		dp.setKnob(db_dp.getKnob());
//		dp.setStartTime(db_dp.getStartTime());
//		dp.setDt(db_dp.getSampleRate());
//		dp.setLinkIdOrigin(db_dp.getOriginLinkId());
//		dp.setDestinationNetworkId(db_dp.getDestinationNetworkId());
//		dp.setStdDevAdd(db_dp.getStdDeviationAdditive());
//		dp.setStdDevMult(db_dp.getStdDeviationMultiplicative());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(DemandsPeer.NUMBER);
//		crit.addAscendingOrderByColumn(DemandsPeer.VEH_TYPE_ID);
//		@SuppressWarnings("unchecked")
//		List<Demands> db_demand_l = db_dp.getDemandss(crit);
//		StringBuilder sb = null;
//		Integer number = null;
//		for (Demands db_demand : db_demand_l) {
//			if (null == sb) sb = new StringBuilder();
//			else sb.append(db_demand.getNumber().equals(number) ? ':' : ',');
//			number = db_demand.getNumber();
//			sb.append(db_demand.getDemand().toPlainString());
//		}
//		if (null != sb) dp.setContent(sb.toString());
//		return dp;
//	}
//
//	private edu.berkeley.path.beats.jaxb.NetworkConnections restoreNetworkConnections(NetworkConnectionSets db_ncs) throws TorqueException {
//		if (null == db_ncs) return null;
//		edu.berkeley.path.beats.jaxb.NetworkConnections nc = factory.createNetworkConnections();
//		nc.setId(db_ncs.getId());
//		nc.setName(db_ncs.getName());
//		nc.setDescription(db_ncs.getDescription());
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(NetworkConnectionsPeer.FROM_NET_ID);
//		crit.addAscendingOrderByColumn(NetworkConnectionsPeer.TO_NET_ID);
//		@SuppressWarnings("unchecked")
//		List<NetworkConnections> db_nc_l = db_ncs.getNetworkConnectionss(crit);
//		edu.berkeley.path.beats.jaxb.Networkpair np = null;
//		for (NetworkConnections db_nc : db_nc_l) {
//			if (null != np && (np.getNetworkA()!=db_nc.getFromNetworkId() || np.getNetworkB()!=db_nc.getToNetworkId())) {
//				nc.getNetworkpair().add(np);
//				np = null;
//			}
//			if (null == np) {
//				np = factory.createNetworkpair();
//				np.setNetworkA(db_nc.getFromNetworkId());
//				np.setNetworkB(db_nc.getToNetworkId());
//			}
//			edu.berkeley.path.beats.jaxb.Linkpair lp = factory.createLinkpair();
//			lp.setLinkA(db_nc.getFromLinkId());
//			lp.setLinkB(db_nc.getToLinkId());
//			np.getLinkpair().add(lp);
//		}
//		if (null != np) nc.getNetworkpair().add(np);
//		return nc;
//	}
//
//	private edu.berkeley.path.beats.jaxb.SignalList restoreSignalList(SignalSets db_ss) throws TorqueException {
//		if (null == db_ss) return null;
//		edu.berkeley.path.beats.jaxb.SignalList sl = factory.createSignalList();
//		sl.setName(db_ss.getName());
//		sl.setDescription(db_ss.getDescription());
//		@SuppressWarnings("unchecked")
//		List<Signals> db_signal_l = db_ss.getSignalss();
//		for (Signals db_signal : db_signal_l)
//			sl.getSignal().add(restoreSignal(db_signal));
//		return sl;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Signal restoreSignal(Signals db_signal) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Signal signal = factory.createSignal();
//		signal.setId(db_signal.getId());
//		signal.setNodeId(db_signal.getNodeId());
//		@SuppressWarnings("unchecked")
//		List<Phases> db_ph_l = db_signal.getPhasess();
//		for (Phases db_ph : db_ph_l)
//			signal.getPhase().add(restorePhase(db_ph));
//		return signal;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Phase restorePhase(Phases db_ph) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Phase phase = factory.createPhase();
//		phase.setNema(BigInteger.valueOf(db_ph.getPhaseId()));
//		phase.setProtected(db_ph.getIsProtected());
//		phase.setPermissive(db_ph.getIsPermissive());
//		phase.setLag(db_ph.getIsLagged());
//		phase.setRecall(db_ph.getDoRecall());
//		phase.setMinGreenTime(db_ph.getMinGreenTime());
//		phase.setYellowTime(db_ph.getYellowTime());
//		phase.setRedClearTime(db_ph.getRedClearTime());
//		@SuppressWarnings("unchecked")
//		List<PhaseLinks> db_phl_l = db_ph.getPhaseLinkss();
//		edu.berkeley.path.beats.jaxb.LinkReferences linkrefs = factory.createLinkReferences();
//		for (PhaseLinks db_phl : db_phl_l)
//			linkrefs.getLinkReference().add(restorePhaseLink(db_phl));
//		phase.setLinkReferences(linkrefs);
//		return phase;
//	}
//
//	private edu.berkeley.path.beats.jaxb.LinkReference restorePhaseLink(PhaseLinks db_phl) {
//		edu.berkeley.path.beats.jaxb.LinkReference lr = factory.createLinkReference();
//		lr.setId(db_phl.getLinkId());
//		return lr;
//	}
//
//	private edu.berkeley.path.beats.jaxb.SensorList restoreSensorList(SensorSets db_ss) throws TorqueException {
//		if (null == db_ss) return null;
//		edu.berkeley.path.beats.jaxb.SensorList sl = factory.createSensorList();
//		@SuppressWarnings("unchecked")
//		List<Sensors> db_sensor_l = db_ss.getSensorss();
//		for (Sensors db_sensor: db_sensor_l)
//			sl.getSensor().add(restoreSensor(db_sensor));
//		return sl;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Sensor restoreSensor(Sensors db_sensor) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Sensor sensor = factory.createSensor();
//		sensor.setId(db_sensor.getId());
//		sensor.setLinkPosition(db_sensor.getLinkPosition());
//		sensor.setType(db_sensor.getSensorTypes().getName());
//		sensor.setSensorIdOriginal(db_sensor.getOriginalId());
//		if (null != db_sensor.getLaneNumber())
//			sensor.setLaneNumber(BigInteger.valueOf(db_sensor.getLaneNumber().longValue()));
//		sensor.setHealthStatus(db_sensor.getHealthStatus());
//		sensor.setDisplayPosition(restoreDisplayPosition(db_sensor.getDisplayGeometry()));
//		if (null != db_sensor.getLinkId()) {
//			edu.berkeley.path.beats.jaxb.LinkReference lr = factory.createLinkReference();
//			lr.setId(db_sensor.getLinkId());
//			sensor.setLinkReference(lr);
//		}
//		sensor.setParameters(restoreParameters(db_sensor));
//		List<edu.berkeley.path.beats.jaxb.Table> table_l = restoreTables(db_sensor);
//		if (null != table_l && !table_l.isEmpty()) {
//			sensor.setTable(table_l.get(0));
//			if (1 < table_l.size())
//				logger.warn("Sensor " + db_sensor.getId() + " has " + table_l.size() + " tables");
//		}
//		return sensor;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfileSet restoreDownstreamBoundaryCapacity(DownstreamBoundaryCapacityProfileSets db_dbcps) throws TorqueException {
//		if (null == db_dbcps) return null;
//		edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfileSet dbcps = factory.createDownstreamBoundaryCapacityProfileSet();
//		dbcps.setId(db_dbcps.getId());
//		dbcps.setName(db_dbcps.getName());
//		dbcps.setDescription(db_dbcps.getDescription());
//		@SuppressWarnings("unchecked")
//		List<DownstreamBoundaryCapacityProfiles> db_dbcp_l = db_dbcps.getDownstreamBoundaryCapacityProfiless();
//		for (DownstreamBoundaryCapacityProfiles db_dbcp : db_dbcp_l)
//			dbcps.getCapacityProfile().add(restoreCapacityProfile(db_dbcp));
//		return dbcps;
//	}
//
//	private edu.berkeley.path.beats.jaxb.CapacityProfile restoreCapacityProfile(DownstreamBoundaryCapacityProfiles db_dbcp) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.CapacityProfile cprofile = factory.createCapacityProfile();
//		cprofile.setLinkId(db_dbcp.getLinkId());
//		cprofile.setDt(db_dbcp.getSampleRate());
//		cprofile.setStartTime(db_dbcp.getStartTime());
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(DownstreamBoundaryCapacitiesPeer.DS_BNDRY_CAP_ORDER);
//		@SuppressWarnings("unchecked")
//		List<DownstreamBoundaryCapacities> db_dbc_l = db_dbcp.getDownstreamBoundaryCapacitiess(crit);
//		StringBuilder sb = null;
//		for (DownstreamBoundaryCapacities db_dbc : db_dbc_l) {
//			if (null == sb) sb = new StringBuilder();
//			else sb.append(',');
//			sb.append(db_dbc.getDownstreamBoundaryCapacity().toPlainString());
//		}
//		if (null != sb) cprofile.setContent(sb.toString());
//		return cprofile;
//	}
//
//	private edu.berkeley.path.beats.jaxb.ControllerSet restoreControllerSet(ControllerSets db_cs) throws TorqueException {
//		if (null == db_cs) return null;
//		edu.berkeley.path.beats.jaxb.ControllerSet cset = factory.createControllerSet();
//		cset.setId(db_cs.getId());
//		cset.setName(db_cs.getName());
//		cset.setDescription(db_cs.getDescription());
//
//		@SuppressWarnings("unchecked")
//		List<Controllers> db_cntr_l = db_cs.getControllerss();
//		for (Controllers db_cntr : db_cntr_l)
//			cset.getController().add(restoreController(db_cntr));
//
//		return cset;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Controller restoreController(Controllers db_cntr) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Controller cntr = factory.createController();
//		cntr.setId(db_cntr.getId());
//		// TODO cntr.setName();
//		cntr.setType(db_cntr.getControllerTypes().getName());
//		cntr.setDt(db_cntr.getDt());
//		cntr.setEnabled(Boolean.TRUE);
//		cntr.setJavaClass(db_cntr.getJavaClass());
//		cntr.setDisplayPosition(restoreDisplayPosition(db_cntr.getDisplayGeometry()));
//		cntr.setTargetElements(restoreTargetElements(db_cntr));
//		cntr.setFeedbackElements(restoreFeedbackElements(db_cntr));
//		@SuppressWarnings("unchecked")
//		List<QueueControllers> db_qc_l = db_cntr.getQueueControllerss();
//		if (!db_qc_l.isEmpty()) {
//			if (1 < db_qc_l.size())
//				logger.warn("Found " + db_qc_l.size() + " queue controllers for controller " + db_cntr.getId());
//			cntr.setQueueController(restoreQueueController(db_qc_l.get(0)));
//		}
//		cntr.setParameters(restoreParameters(db_cntr));
//		cntr.getTable().addAll(restoreTables(db_cntr));
//		cntr.setActivationIntervals(restoreActivationIntervals(db_cntr));
//		// TODO cntr.setPlanSequence();
//		// TODO cntr.setPlanList();
//		return cntr;
//	}
//
//	private edu.berkeley.path.beats.jaxb.QueueController restoreQueueController(QueueControllers db_qc) throws TorqueException {
//		if (null == db_qc) return null;
//		edu.berkeley.path.beats.jaxb.QueueController qc = factory.createQueueController();
//		qc.setType(db_qc.getQueueControllerTypes().getName());
//		qc.setJavaClass(db_qc.getJavaClass());
//		qc.setParameters(restoreParameters(db_qc));
//		return qc;
//	}
//
//	private edu.berkeley.path.beats.jaxb.ActivationIntervals restoreActivationIntervals(Controllers db_cntr) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<ControllerActivationIntervals> db_cai_l = db_cntr.getControllerActivationIntervalss();
//		if (db_cai_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.ActivationIntervals ais = factory.createActivationIntervals();
//		for (ControllerActivationIntervals db_cai : db_cai_l)
//			ais.getInterval().add(restoreInterval(db_cai));
//		return ais;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Interval restoreInterval(ControllerActivationIntervals db_cai) {
//		edu.berkeley.path.beats.jaxb.Interval interval = factory.createInterval();
//		interval.setStartTime(db_cai.getStartTime());
//		interval.setEndTime(db_cai.getStartTime().add(db_cai.getDuration()));
//		return interval;
//	}
//
//	private edu.berkeley.path.beats.jaxb.EventSet restoreEventSet(EventSets db_eset) throws TorqueException {
//		if (null == db_eset) return null;
//		edu.berkeley.path.beats.jaxb.EventSet eset = factory.createEventSet();
//		eset.setId(db_eset.getId());
//		eset.setName(db_eset.getName());
//		eset.setDescription(db_eset.getDescription());
//
//		@SuppressWarnings("unchecked")
//		List<Events> db_event_l = db_eset.getEventss();
//		for (Events db_event : db_event_l)
//			eset.getEvent().add(restoreEvent(db_event));
//
//		return eset;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Event restoreEvent(Events db_event) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Event event = factory.createEvent();
//		event.setId(db_event.getId());
//		event.setTstamp(db_event.getActionTime());
//		event.setEnabled(db_event.getEnabled());
//		event.setType(db_event.getEventTypes().getName());
//		event.setJavaClass(db_event.getJavaClass());
//		event.setDescription(db_event.getDescription());
//		event.setDisplayPosition(restoreDisplayPosition(db_event.getDisplayGeometry()));
//		event.setTargetElements(restoreTargetElements(db_event));
//		event.setParameters(restoreParameters(db_event));
//
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(EventSplitRatiosPeer.IN_LINK_ID);
//		crit.addAscendingOrderByColumn(EventSplitRatiosPeer.OUT_LINK_ID);
//		crit.addAscendingOrderByColumn(EventSplitRatiosPeer.VEH_TYPE_ID);
//		@SuppressWarnings("unchecked")
//		List<EventSplitRatios> db_esr_l = db_event.getEventSplitRatioss(crit);
//		if (!db_esr_l.isEmpty()) {
//			edu.berkeley.path.beats.jaxb.SplitratioEvent srevent = factory.createSplitratioEvent();
//			edu.berkeley.path.beats.jaxb.Splitratio sr = null;
//			StringBuilder sb = new StringBuilder();
//			for (EventSplitRatios db_esr : db_esr_l) {
//				if (null != sr && !(sr.getLinkIn()==db_esr.getInLinkId() && sr.getLinkOut()==db_esr.getOutLinkId())) {
//					sr.setContent(sb.toString());
//					sb.setLength(0);
//					srevent.getSplitratio().add(sr);
//					sr = null;
//				}
//				if (null == sr) {
//					sr = factory.createSplitratio();
//					sr.setLinkIn(db_esr.getInLinkId());
//					sr.setLinkOut(db_esr.getOutLinkId());
//				} else
//					sb.append(':');
//				// TODO revise: check if there are missing vehicle types
//				sb.append(db_esr.getSplitRatio().toPlainString());
//			}
//			if (null != sr) {
//				sr.setContent(sb.toString());
//				srevent.getSplitratio().add(sr);
//			}
//			event.setSplitratioEvent(srevent);
//		}
//
//		return event;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DestinationNetworks restoreDestinationNetworks(Scenarios db_scenario) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<DestinationNetworkSets> db_dns_l = db_scenario.getDestinationNetworkSetss();
//		if (db_dns_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.DestinationNetworks destnets = factory.createDestinationNetworks();
//		for (DestinationNetworkSets db_dns : db_dns_l)
//			destnets.getDestinationNetwork().add(restoreDestinationNetwork(db_dns.getDestinationNetworks()));
//		return destnets;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DestinationNetwork restoreDestinationNetwork(DestinationNetworks db_destnet) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.DestinationNetwork destnet = factory.createDestinationNetwork();
//		destnet.setId(db_destnet.getId());
//		destnet.setLinkIdDestination(db_destnet.getDestinationLinkId());
//		edu.berkeley.path.beats.jaxb.LinkReferences linkrefs = factory.createLinkReferences();
//		@SuppressWarnings("unchecked")
//		List<DestinationNetworkLinks> db_dnl_l = db_destnet.getDestinationNetworkLinkss();
//		for (DestinationNetworkLinks db_dnl : db_dnl_l)
//			linkrefs.getLinkReference().add(restoreDestinationNetworkLinks(db_dnl));
//		destnet.setLinkReferences(linkrefs);
//		return destnet;
//	}
//
//	private edu.berkeley.path.beats.jaxb.LinkReference restoreDestinationNetworkLinks(DestinationNetworkLinks db_dnl) {
//		edu.berkeley.path.beats.jaxb.LinkReference linkref = factory.createLinkReference();
//		linkref.setId(db_dnl.getLinkId());
//		return linkref;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Routes restoreRoutes(Scenarios db_scenario) throws TorqueException {
//		@SuppressWarnings("unchecked")
//		List<RouteSets> db_rset_l = db_scenario.getRouteSetss();
//		if (db_rset_l.isEmpty()) return null;
//		edu.berkeley.path.beats.jaxb.Routes routes = factory.createRoutes();
//		for (RouteSets db_rset : db_rset_l)
//			routes.getRoute().add(restoreRoute(db_rset.getRoutes()));
//		return routes;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Route restoreRoute(Routes db_route) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Route route = factory.createRoute();
//		route.setId(db_route.getId());
//		route.setName(db_route.getName());
//		edu.berkeley.path.beats.jaxb.LinkReferences lrs = factory.createLinkReferences();
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(RouteLinksPeer.LINK_ORDER);
//		@SuppressWarnings("unchecked")
//		List<RouteLinks> db_rl_l = db_route.getRouteLinkss(crit);
//		for (RouteLinks db_rl : db_rl_l) {
//			edu.berkeley.path.beats.jaxb.LinkReference lr = factory.createLinkReference();
//			lr.setId(db_rl.getLinkId());
//			lrs.getLinkReference().add(lr);
//		}
//		route.setLinkReferences(lrs);
//		return route;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Parameters restoreParameters(edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Parameters params = factory.createParameters();
//
//		Criteria crit = new Criteria();
//		crit.add(ParametersPeer.ELEMENT_ID, db_obj.getId());
//		crit.addJoin(ParametersPeer.ELEMENT_TYPE_ID, ScenarioElementTypesPeer.ID);
//		crit.add(ScenarioElementTypesPeer.NAME, db_obj.getElementType());
//		@SuppressWarnings("unchecked")
//		List<edu.berkeley.path.beats.om.Parameters> db_param_l = ParametersPeer.doSelect(crit);
//		for (edu.berkeley.path.beats.om.Parameters db_param : db_param_l)
//			params.getParameter().add(restoreParameter(db_param));
//
//		return params;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Parameter restoreParameter(edu.berkeley.path.beats.om.Parameters db_param) {
//		edu.berkeley.path.beats.jaxb.Parameter param = factory.createParameter();
//		param.setName(db_param.getName());
//		param.setValue(db_param.getValue());
//		return param;
//	}
//
//	private List<edu.berkeley.path.beats.jaxb.Table> restoreTables(edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(TablesPeer.ELEMENT_ID, db_obj.getId());
//		crit.addJoin(TablesPeer.ELEMENT_TYPE_ID, ScenarioElementTypesPeer.ID);
//		crit.add(ScenarioElementTypesPeer.NAME, db_obj.getElementType());
//		@SuppressWarnings("unchecked")
//		List<Tables> db_table_l = TablesPeer.doSelect(crit);
//		List<edu.berkeley.path.beats.jaxb.Table> table_l = new java.util.ArrayList<edu.berkeley.path.beats.jaxb.Table>(db_table_l.size());
//		for (Tables db_table : db_table_l)
//			table_l.add(restoreTable(db_table));
//		return table_l;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Table restoreTable(Tables db_table) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.Table table = factory.createTable();
//		table.setName(db_table.getName());
//		table.setColumnNames(restoreColumnNames(db_table));
//
//		java.util.Map<String, Integer> colname2index = new java.util.HashMap<String, Integer>();
//		int index = 0;
//		for (edu.berkeley.path.beats.jaxb.ColumnName colname : table.getColumnNames().getColumnName())
//			colname2index.put(colname.getName(), Integer.valueOf(index++));
//
//		Criteria crit = new Criteria();
//		crit.addJoin(TabularDataPeer.TABLE_ID, TabularDataKeysPeer.TABLE_ID);
//		crit.addJoin(TabularDataPeer.COLUMN_NAME, TabularDataKeysPeer.COLUMN_NAME);
//		crit.addAscendingOrderByColumn(TabularDataPeer.ROW_NUMBER);
//		crit.addAscendingOrderByColumn(TabularDataKeysPeer.COLUMN_NUMBER);
//		@SuppressWarnings("unchecked")
//		List<TabularData> db_td_l = db_table.getTabularDatas(crit);
//		for (TabularData db_td : db_td_l) {
//			while (table.getRow().size() <= db_td.getRowNumber()) {
//				edu.berkeley.path.beats.jaxb.Row row = factory.createRow();
//				for (int i = 0; i < table.getColumnNames().getColumnName().size(); ++i)
//					row.getColumn().add(null);
//				table.getRow().add(row);
//			}
//			edu.berkeley.path.beats.jaxb.Row row = table.getRow().get(db_td.getRowNumber());
//			int colnum = colname2index.get(db_td.getColumnName()).intValue();
//			if (null != row.getColumn().get(colnum))
//				logger.warn("Table " + table.getName() + ", row " + db_td.getRowNumber() + ": duplicate column '" + db_td.getColumnName() + "'");
//			row.getColumn().set(colnum, db_td.getValue());
//		}
//
//		return table;
//	}
//
//	private edu.berkeley.path.beats.jaxb.ColumnNames restoreColumnNames(Tables db_table) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.addAscendingOrderByColumn(TabularDataKeysPeer.COLUMN_NUMBER);
//		@SuppressWarnings("unchecked")
//		List<TabularDataKeys> db_tdk_l = db_table.getTabularDataKeyss(crit);
//		edu.berkeley.path.beats.jaxb.ColumnNames colnames = factory.createColumnNames();
//		for (TabularDataKeys db_tdk : db_tdk_l)
//			colnames.getColumnName().add(restoreColumnName(db_tdk));
//		return colnames;
//	}
//
//	private edu.berkeley.path.beats.jaxb.ColumnName restoreColumnName(TabularDataKeys db_tdk) {
//		edu.berkeley.path.beats.jaxb.ColumnName colname = factory.createColumnName();
//		colname.setName(db_tdk.getColumnName());
//		colname.setKey(db_tdk.getIsKey());
//		return colname;
//	}
//
//	private edu.berkeley.path.beats.jaxb.TargetElements restoreTargetElements(edu.berkeley.path.beats.db.BaseObject db_parent) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.TargetElements elems = factory.createTargetElements();
//		Criteria crit = new Criteria();
//		crit.add(ReferencedScenarioElementsPeer.PARENT_ELEMENT_ID, db_parent.getId());
//		crit.addJoin(ReferencedScenarioElementsPeer.PARENT_ELEMENT_TYPE_ID, ScenarioElementTypesPeer.ID);
//		crit.add(ScenarioElementTypesPeer.NAME, db_parent.getElementType());
//		crit.add(ReferencedScenarioElementsPeer.TYPE, "target");
//		@SuppressWarnings("unchecked")
//		List<ReferencedScenarioElements> db_elem_l = ReferencedScenarioElementsPeer.doSelect(crit);
//		for (ReferencedScenarioElements db_elem : db_elem_l)
//			elems.getScenarioElement().add(restoreScenarioElement(db_elem));
//		return elems;
//	}
//
//	private edu.berkeley.path.beats.jaxb.FeedbackElements restoreFeedbackElements(edu.berkeley.path.beats.db.BaseObject db_parent) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.FeedbackElements elems = factory.createFeedbackElements();
//		Criteria crit = new Criteria();
//		crit.add(ReferencedScenarioElementsPeer.PARENT_ELEMENT_ID, db_parent.getId());
//		crit.addJoin(ReferencedScenarioElementsPeer.PARENT_ELEMENT_TYPE_ID, ScenarioElementTypesPeer.ID);
//		crit.add(ScenarioElementTypesPeer.NAME, db_parent.getElementType());
//		crit.add(ReferencedScenarioElementsPeer.TYPE, "feedback");
//		@SuppressWarnings("unchecked")
//		List<ReferencedScenarioElements> db_elem_l = ReferencedScenarioElementsPeer.doSelect(crit);
//		for (ReferencedScenarioElements db_elem : db_elem_l)
//			elems.getScenarioElement().add(restoreScenarioElement(db_elem));
//		return elems;
//	}
//
//	private edu.berkeley.path.beats.jaxb.ScenarioElement restoreScenarioElement(ReferencedScenarioElements db_elem) throws TorqueException {
//		edu.berkeley.path.beats.jaxb.ScenarioElement elem = factory.createScenarioElement();
//		elem.setId(db_elem.getElementId());
//		elem.setType(db_elem.getScenarioElementTypesRelatedByElementTypeId().getName());
//		elem.setUsage(db_elem.getUsage());
//		return elem;
//	}
//
//	private edu.berkeley.path.beats.jaxb.Position restorePosition(String geometry) {
//		if (null == geometry) return null;
//		List<edu.berkeley.path.beats.jaxb.Point> point_l = decodePolyline(geometry);
//		if (null != point_l) return null;
//		edu.berkeley.path.beats.jaxb.Position pos = factory.createPosition();
//		pos.getPoint().addAll(point_l);
//		return pos;
//	}
//
//	private edu.berkeley.path.beats.jaxb.DisplayPosition restoreDisplayPosition(String geometry) {
//		if (null == geometry) return null;
//		List<edu.berkeley.path.beats.jaxb.Point> point_l = decodePolyline(geometry);
//		if (null == point_l) return null;
//		edu.berkeley.path.beats.jaxb.DisplayPosition pos = factory.createDisplayPosition();
//		pos.getPoint().addAll(point_l);
//		return pos;
//	}
//
//	private List<edu.berkeley.path.beats.jaxb.Point> decodePolyline(String path) {
//		if (null == path) return null;
//		polyline_decoder.reset();
//		try {
//			return polyline_decoder.decode(path);
//		} catch (BeatsException exc) {
//			logger.error("Failed to restore a list of points", exc);
//		}
//		return null;
//	}

}
