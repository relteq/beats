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
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.util.Data1D;
import edu.berkeley.path.beats.util.Data2D;

/**
 * Imports a scenario
 */
public class ScenarioImporter {
//	private Connection conn = null;
//
//	private Long project_id;
//	/**
//	 * @return the project id
//	 */
//	private Long getProjectId() {
//		return project_id;
//	}
//
//	private VehicleTypes [] vehicle_type = null;
//	private Map<Long, Long> network_id = null;
//	private Map<Long, Nodes> nodes = null;
//	private Map<Long, Links> links = null;
//	private Map<Long, Controllers> controllers = null;
//	private Map<Long, Sensors> sensors = null;
//	private Map<Long, Events> events = null;
//	private Map<Long, Signals> signals = null;
//	private Map<Long, DestinationNetworks> destnets = null;
//
//	private Long getDBNodeId(long id) {
//		Nodes db_node = nodes.get(id);
//		if (null == db_node) {
//			logger.warn("Node " + id + " does not exist");
//			return null;
//		}
//		return db_node.getId();
//	}
//
//	private Long getDBLinkId(long id) {
//		Links db_link = links.get(id);
//		if (null == db_link) {
//			logger.warn("Link " + id + " does not exist");
//			return null;
//		}
//		return db_link.getId();
//	}
//
//	private edu.berkeley.path.beats.util.polyline.EncoderBase polyline_encoder;
//
//	private ScenarioImporter() {
//		project_id = Long.valueOf(0);
//		polyline_encoder = new edu.berkeley.path.beats.util.polyline.GoogleEncoder();
//	}
//
//	private static Logger logger = Logger.getLogger(ScenarioImporter.class);
//
//	/**
//	 * Imports a scenario
//	 * @param scenario
//	 * @return the scenario ID in the database
//	 * @throws BeatsException
//	 */
//	public static Long doImport(edu.berkeley.path.beats.jaxb.Scenario scenario) throws BeatsException {
//		return new ScenarioImporter().store(scenario).getId();
//	}
//
//	private Scenarios store(edu.berkeley.path.beats.jaxb.Scenario scenario) throws BeatsException {
//		edu.berkeley.path.beats.db.Service.ensureInit();
//		
//		try {
//			conn = Transaction.begin();
//			Scenarios db_scenario = save(scenario);
//			Transaction.commit(conn);
//			conn = null;
//			return db_scenario;
//		} catch (TorqueException exc) {
//			throw new BeatsException(exc);
//		} finally {
//			if (null != conn) {
//				Transaction.safeRollback(conn);
//				conn = null;
//			}
//		}
//	}
//
//	/**
//	 * Imports a scenario
//	 * @param scenario
//	 * @throws TorqueException
//	 * @throws BeatsException
//	 */
//	private Scenarios save(edu.berkeley.path.beats.jaxb.Scenario scenario) throws TorqueException, BeatsException {
//		if (null == scenario) return null;
//		if (null != scenario.getSettings() && !"SI".equalsIgnoreCase(scenario.getSettings().getUnits()))
//			throw new BeatsException("Scenario's system of units is not SI");
//		Scenarios db_scenario = new Scenarios();
//		db_scenario.setProjectId(getProjectId());
//		db_scenario.setName(scenario.getName());
//		db_scenario.setDescription(scenario.getDescription());
//		edu.berkeley.path.beats.jaxb.VehicleTypes vtypes = null;
//		if (null != scenario.getSettings())
//			vtypes = scenario.getSettings().getVehicleTypes();
//		db_scenario.setVehicleTypeSets(save(vtypes));
//		db_scenario.save(conn);
//		save(scenario.getNetworkList(), db_scenario);
//		save(scenario.getDestinationNetworks(), db_scenario);
//		db_scenario.setNetworkConnectionSets(save(scenario.getNetworkConnections()));
//		db_scenario.setSignalSets(save(scenario.getSignalList()));
//		db_scenario.setSensorSets(save(scenario.getSensorList()));
//		db_scenario.setSplitRatioProfileSets(save(scenario.getSplitRatioProfileSet()));
//		db_scenario.setWeavingFactorSets(save(scenario.getWeavingFactorSet()));
//		db_scenario.setInitialDensitySets(save(scenario.getInitialDensitySet()));
//		db_scenario.setFundamentalDiagramProfileSets(save(scenario.getFundamentalDiagramProfileSet()));
//		db_scenario.setDemandProfileSets(save(scenario.getDemandProfileSet()));
//		db_scenario.setDownstreamBoundaryCapacityProfileSets(save(scenario.getDownstreamBoundaryCapacityProfileSet()));
//		db_scenario.setControllerSets(save(scenario.getControllerSet()));
//		db_scenario.setEventSets(save(scenario.getEventSet()));
//		save(scenario.getRouteSet(), db_scenario);
//		db_scenario.save(conn);
//
//		// save referenced elements
//		if (null != scenario.getControllerSet())
//			for (edu.berkeley.path.beats.jaxb.Controller cntr : scenario.getControllerSet().getController()) {
//				save(cntr.getTargetElements(), controllers.get(cntr.getId()));
//				save(cntr.getFeedbackElements(), controllers.get(cntr.getId()));
//			}
//		if (null != scenario.getEventSet())
//			for (edu.berkeley.path.beats.jaxb.Event event : scenario.getEventSet().getEvent())
//				save(event.getTargetElements(), events.get(event.getId()));
//
//		// create default simulation settings
//		DefSimSettings db_defss = new DefSimSettings();
//		db_defss.setScenarios(db_scenario);
//		db_defss.setSimStartTime(BigDecimal.valueOf(0));
//		db_defss.setSimDuration(BigDecimal.valueOf(60 * 60 * 24));
//		BigDecimal simdt = null;
//		if (null != scenario.getNetworkList())
//			for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()) {
//				if (null == simdt)
//					simdt = network.getDt();
//				else if (simdt.compareTo(network.getDt()) > 0)
//					simdt = network.getDt(); // TODO revise
//			}
//		if (null == simdt) simdt = BigDecimal.valueOf(1);
//		db_defss.setSimDt(simdt);
//		BigDecimal defaultOutputDt = BigDecimal.valueOf(60);
//		db_defss.setOutputDt(simdt.compareTo(defaultOutputDt) > 0 ? simdt : defaultOutputDt); // TODO revise
//		db_defss.save(conn);
//
//		return db_scenario;
//	}
//
//	/**
//	 * Imports vehicle types
//	 * @param vtypes
//	 * @return the imported vehicle type set
//	 * @throws TorqueException
//	 */
//	private VehicleTypeSets save(edu.berkeley.path.beats.jaxb.VehicleTypes vtypes) throws TorqueException {
//		VehicleTypeSets db_vts = new VehicleTypeSets();
//		db_vts.setProjectId(getProjectId());
//		db_vts.save(conn);
//		if (null == vtypes) {
//			vtypes = new edu.berkeley.path.beats.jaxb.VehicleTypes();
//			edu.berkeley.path.beats.jaxb.VehicleType vt = new edu.berkeley.path.beats.jaxb.VehicleType();
//			vt.setName("SOV");
//			vt.setWeight(new BigDecimal(1));
//			vtypes.getVehicleType().add(vt);
//		}
//		List<edu.berkeley.path.beats.jaxb.VehicleType> vtlist = vtypes.getVehicleType();
//		vehicle_type = new VehicleTypes[vtlist.size()];
//		int ind = 0;
//		for (edu.berkeley.path.beats.jaxb.VehicleType vt : vtlist)
//			vehicle_type[ind++] = save(vt, db_vts);
//		return db_vts;
//	}
//
//	/**
//	 * Imports a vehicle type
//	 * @param vt the vehicle type to be imported
//	 * @param db_vts an imported vehicle type set
//	 * @return the imported (or already existing) vehicle type
//	 * @throws TorqueException
//	 */
//	private VehicleTypes save(edu.berkeley.path.beats.jaxb.VehicleType vt, VehicleTypeSets db_vts) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(VehicleTypesPeer.NAME, vt.getName());
//		crit.add(VehicleTypesPeer.SIZE_FACTOR, vt.getWeight());
//		@SuppressWarnings("unchecked")
//		List<VehicleTypes> db_vt_l = VehicleTypesPeer.doSelect(crit, conn);
//		VehicleTypes db_vtype = null;
//		if (db_vt_l.isEmpty()) {
//			db_vtype = new VehicleTypes();
//			db_vtype.setName(vt.getName());
//			db_vtype.setWeight(vt.getWeight());
//			db_vtype.setIsStandard(Boolean.FALSE);
//			db_vtype.save(conn);
//		} else {
//			db_vtype = db_vt_l.get(0);
//			if (1 < db_vt_l.size())
//				logger.warn("Found " + db_vt_l.size() + " vehicle types with name=" + vt.getName() + ", weight=" + vt.getWeight());
//		}
//		VehicleTypesInSets db_vtins = new VehicleTypesInSets();
//		db_vtins.setVehicleTypeSets(db_vts);
//		db_vtins.setVehicleTypes(db_vtype);
//		db_vtins.save(conn);
//		return db_vtype;
//	}
//
//	/**
//	 * Imports a network list
//	 * @param nl
//	 * @throws TorqueException
//	 * @throws BeatsException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.NetworkList nl, Scenarios db_scenario) throws TorqueException, BeatsException {
//		network_id = new HashMap<Long, Long>(nl.getNetwork().size());
//		nodes = new HashMap<Long, Nodes>();
//		links = new HashMap<Long, Links>();
//		for (edu.berkeley.path.beats.jaxb.Network network : nl.getNetwork()) {
//			NetworkSets db_ns = new NetworkSets();
//			db_ns.setScenarios(db_scenario);
//			db_ns.setNetworks(save(network));
//			db_ns.save(conn);
//		}
//	}
//
//	/**
//	 * Imports a network
//	 * @param network
//	 * @return the imported network
//	 * @throws TorqueException
//	 * @throws BeatsException
//	 */
//	private Networks save(edu.berkeley.path.beats.jaxb.Network network) throws TorqueException, BeatsException {
//		Networks db_network = new Networks();
//		db_network.setName(network.getName());
//		db_network.setDescription(network.getDescription());
//		//db_network.setLocked(network.isLocked());
//		db_network.save(conn);
//		network_id.put(network.getId(), Long.valueOf(db_network.getId()));
//		for (edu.berkeley.path.beats.jaxb.Node node : network.getNodeList().getNode())
//			save(node, db_network);
//		for (edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink())
//			save(link, db_network);
//		return db_network;
//	}
//
//	private <T extends BaseTypes> T createType(T obj, String name) throws TorqueException {
//		obj.setName(name);
//		obj.setInUse(Boolean.TRUE);
//		obj.save(conn);
//		return obj;
//	}
//
//	private NodeTypes getNodeTypes(String node_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(NodeTypesPeer.NAME, node_type);
//		@SuppressWarnings("unchecked")
//		List<NodeTypes> db_nt_l = NodeTypesPeer.doSelect(crit, conn);
//		if (!db_nt_l.isEmpty()) {
//			if (1 < db_nt_l.size())
//				logger.warn("Found " + db_nt_l.size() + " node types '" + node_type + "'");
//			return db_nt_l.get(0);
//		} else {
//			logger.warn("Node type '" + node_type + "' does not exist");
//			return createType(new NodeTypes(), node_type);
//		}
//	}
//
//	/**
//	 * Imports a node
//	 * @param node
//	 * @param db_network
//	 * @throws TorqueException
//	 * @throws BeatsException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Node node, Networks db_network) throws TorqueException, BeatsException {
//		if (nodes.containsKey(node.getId())) throw new BeatsException("Node " + node.getId() + " already exists");
//		NodeFamilies db_nf = new NodeFamilies();
//		db_nf.setId(NodeFamiliesPeer.nextId(NodeFamiliesPeer.ID, conn));
//		db_nf.save(conn);
//
//		Nodes db_node = new Nodes();
//		db_node.setNodeFamilies(db_nf);
//		db_node.setNetworks(db_network);
//		db_node.setGeom(pos2str(node.getPosition()));
//		db_node.setInSync(node.isInSync());
//
//		// node type
//		NodeTypeDet db_ntdet = new NodeTypeDet();
//		db_ntdet.setNodeTypes(getNodeTypes(node.getType()));
//		db_node.addNodeTypeDet(db_ntdet, conn);
//
//		db_node.save(conn);
//		nodes.put(node.getId(), db_node);
//
//		save(node.getRoadwayMarkers(), db_node);
//	}
//
//	/**
//	 * Imports roadway markers
//	 * @param markers
//	 * @param db_node an imported node
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.RoadwayMarkers markers, Nodes db_node) throws TorqueException {
//		if (null == markers) return;
//		for (edu.berkeley.path.beats.jaxb.Marker marker : markers.getMarker())
//			if (null == marker.getPostmile()) { // importing node name
//				NodeName db_nname = new NodeName();
//				db_nname.setNodes(db_node);
//				db_nname.setName(marker.getName());
//				db_nname.save(conn);
//			} else { // importing highway postmile
//				PostmileHighways db_pmhw = null;
//				Criteria crit = new Criteria();
//				crit.add(PostmileHighwaysPeer.HIGHWAY_NAME, marker.getName());
//				@SuppressWarnings("unchecked")
//				List<PostmileHighways> db_pmhw_l = PostmileHighwaysPeer.doSelect(crit);
//				if (!db_pmhw_l.isEmpty()) {
//					db_pmhw = db_pmhw_l.get(0);
//					if (1 < db_pmhw_l.size())
//						logger.warn("There are " + db_pmhw_l.size() + " hoghways with name=" + marker.getName());
//				} else {
//					db_pmhw = new PostmileHighways();
//					db_pmhw.setHighwayName(marker.getName());
//					db_pmhw.save(conn);
//				}
//				Postmiles db_postmile = new Postmiles();
//				db_postmile.setNodes(db_node);
//				db_postmile.setPostmileHighways(db_pmhw);
//				db_postmile.setPostmile(marker.getPostmile());
//				db_postmile.save(conn);
//			}
//	}
//
//	private LinkTypes getLinkTypes(String linktype) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(LinkTypesPeer.NAME, linktype);
//		@SuppressWarnings("unchecked")
//		List<LinkTypes> db_lt_l = LinkTypesPeer.doSelect(crit, conn);
//		if (!db_lt_l.isEmpty()) {
//			if (1 < db_lt_l.size())
//				logger.warn("Found " + db_lt_l.size() + " link types '" + linktype + "'");
//			return db_lt_l.get(0);
//		} else {
//			logger.warn("Link type '" + linktype + "' does not exist");
//			return createType(new LinkTypes(), linktype);
//		}
//	}
//
//	/**
//	 * Imports a link
//	 * @param link
//	 * @param db_network
//	 * @throws TorqueException
//	 * @throws BeatsException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Link link, Networks db_network) throws TorqueException, BeatsException {
//		if (links.containsKey(link.getId())) throw new BeatsException("Link " + link.getId() + " already exists");
//		LinkFamilies db_lf = new LinkFamilies();
//		db_lf.setId(LinkFamiliesPeer.nextId(LinkFamiliesPeer.ID, conn));
//		db_lf.save(conn);
//
//		Links db_link = new Links();
//		db_link.setLinkFamilies(db_lf);
//		db_link.setNetworks(db_network);
//		db_link.setBegNodeId(getDBNodeId(link.getBegin().getNodeId()));
//		db_link.setEndNodeId(getDBNodeId(link.getEnd().getNodeId()));
//		db_link.setGeom(null == link.getShape() ? "" : link.getShape()); // TODO revise: shape -> geometry
//		db_link.setLength(link.getLength());
//		db_link.setDetailLevel(1);
//		db_link.setInSync(link.isInSync());
//
//		// link type
//		LinkTypeDet db_ltdet = new LinkTypeDet();
//		db_ltdet.setLinkTypes(getLinkTypes(link.getType()));
//		db_link.addLinkTypeDet(db_ltdet, conn);
//
//		// link lanes
//		LinkLanes db_llanes = new LinkLanes();
//		db_llanes.setLanes(link.getLanes());
//		db_link.addLinkLanes(db_llanes, conn);
//
//		// link lane offset
//		if (null != link.getLaneOffset()) {
//			LinkLaneOffset db_lloffset = new LinkLaneOffset();
//			db_lloffset.setDisplayLaneOffset(link.getLaneOffset());
//			db_link.addLinkLaneOffset(db_lloffset, conn);
//		}
//
//		db_link.save(conn);
//		links.put(link.getId(), db_link);
//
//		save(link.getRoads(), db_link);
//	}
//
//	/**
//	 * Imports link roads
//	 * @param roads
//	 * @param db_link an imported link
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Roads roads, Links db_link) throws TorqueException {
//		if (null == roads) return;
//		for (edu.berkeley.path.beats.jaxb.Road road : roads.getRoad()) {
//			LinkName db_lname = new LinkName();
//			db_lname.setLinks(db_link);
//			db_lname.setName(road.getName());
//			db_lname.save(conn);
//		}
//	}
//
//	/**
//	 * Imports a signal list
//	 * @param sl
//	 * @return the imported signal set
//	 * @throws TorqueException
//	 */
//	private SignalSets save(edu.berkeley.path.beats.jaxb.SignalList sl) throws TorqueException {
//		if (null == sl) return null;
//		SignalSets db_ss = new SignalSets();
//		db_ss.setProjectId(getProjectId());
//		db_ss.setName(sl.getName());
//		db_ss.setDescription(sl.getDescription());
//		db_ss.save(conn);
//		signals = new HashMap<Long, Signals>(sl.getSignal().size());
//		for (edu.berkeley.path.beats.jaxb.Signal signal : sl.getSignal())
//			signals.put(signal.getId(), save(signal, db_ss));
//		return db_ss;
//	}
//
//	/**
//	 * Imports a signal
//	 * @param signal
//	 * @param db_ss an imported signal set
//	 * @return an imported signal
//	 * @throws TorqueException
//	 */
//	private Signals save(edu.berkeley.path.beats.jaxb.Signal signal, SignalSets db_ss) throws TorqueException {
//		Signals db_signal = new Signals();
//		db_signal.setNodeId(getDBNodeId(signal.getNodeId()));
//		db_signal.setSignalSets(db_ss);
//		db_signal.save(conn);
//		for (edu.berkeley.path.beats.jaxb.Phase phase : signal.getPhase())
//			save(phase, db_signal);
//		return db_signal;
//	}
//
//	/**
//	 * Imports a signal phase
//	 * @param phase
//	 * @param db_signal
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Phase phase, Signals db_signal) throws TorqueException {
//		Phases db_phase = new Phases();
//		db_phase.setSignals(db_signal);
//		db_phase.setPhaseId(phase.getNema().intValue());
//		db_phase.setIsProtected(phase.isProtected());
//		db_phase.setIsPermissive(phase.isPermissive());
//		db_phase.setIsLagged(phase.isLag());
//		db_phase.setDoRecall(phase.isRecall());
//		db_phase.setMinGreenTime(phase.getMinGreenTime());
//		db_phase.setYellowTime(phase.getYellowTime());
//		db_phase.setRedClearTime(phase.getRedClearTime());
//		db_phase.save(conn);
//		if (null != phase.getLinkReferences())
//			for (edu.berkeley.path.beats.jaxb.LinkReference lr : phase.getLinkReferences().getLinkReference())
//				save(lr, db_phase);
//	}
//
//	/**
//	 * Imports a link reference (for a signal phase)
//	 * @param lr the link reference
//	 * @param db_phase the imported phase
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.LinkReference lr, Phases db_phase) throws TorqueException {
//		PhaseLinks db_lr = new PhaseLinks();
//		db_lr.setPhases(db_phase);
//		db_lr.setLinkId(getDBLinkId(lr.getId()));
//		db_lr.save(conn);
//	}
//
//	/**
//	 * Imports a sensor list
//	 * @param sl
//	 * @param db_network
//	 * @return the imported sensor set
//	 * @throws TorqueException
//	 */
//	private SensorSets save(edu.berkeley.path.beats.jaxb.SensorList sl) throws TorqueException {
//		if (null == sl) return null;
//		SensorSets db_ss = new SensorSets();
//		db_ss.setProjectId(getProjectId());
//		// TODO db_ss.setName();
//		// TODO db_ss.setDescription();
//		db_ss.save(conn);
//		sensors = new HashMap<Long, Sensors>(sl.getSensor().size());
//		for (edu.berkeley.path.beats.jaxb.Sensor sensor : sl.getSensor())
//			sensors.put(sensor.getId(), save(sensor, db_ss));
//		return db_ss;
//	}
//
//	private SensorTypes getSensorType(String sensor_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(SensorTypesPeer.NAME, sensor_type);
//		@SuppressWarnings("unchecked")
//		List<SensorTypes> db_st_l = SensorTypesPeer.doSelect(crit, conn);
//		if (db_st_l.isEmpty()) {
//			logger.warn("Sensor type '" + sensor_type + "' does not exist");
//			return createType(new SensorTypes(), sensor_type);
//		} else {
//			if (1 < db_st_l.size())
//				logger.warn("Found " + db_st_l.size() + " sensor types '" + sensor_type + "'");
//			return db_st_l.get(0);
//		}
//	}
//
//	/**
//	 * Imports a sensor
//	 * @param sensor
//	 * @param db_ss and imported sensor set
//	 * @return an imported sensor
//	 * @throws TorqueException
//	 */
//	private Sensors save(edu.berkeley.path.beats.jaxb.Sensor sensor, SensorSets db_ss) throws TorqueException {
//		Sensors db_sensor = new Sensors();
//		db_sensor.setSensorSets(db_ss);
//		db_sensor.setSensorTypes(getSensorType(sensor.getType()));
//		// TODO db_sensor.setJavaClass();
//		db_sensor.setOriginalId(sensor.getSensorIdOriginal());
//		// TODO db_sensor.setMeasurementSourceId();
//		db_sensor.setDisplayGeometry(pos2str(sensor.getDisplayPosition()));
//		if (null != sensor.getLinkReference())
//			db_sensor.setLinkId(getDBLinkId(sensor.getLinkReference().getId()));
//		db_sensor.setLinkPosition(sensor.getLinkPosition());
//		if (null != sensor.getLaneNumber())
//			db_sensor.setLaneNumber(Integer.valueOf(sensor.getLaneNumber().intValue()));
//		db_sensor.setHealthStatus(sensor.getHealthStatus());
//		db_sensor.save(conn);
//		save(sensor.getParameters(), db_sensor);
//		save(sensor.getTable(), db_sensor);
//		return db_sensor;
//	}
//
//	/**
//	 * Builds a vehicle type list from vehicle type order
//	 * @param order vehicle type order
//	 * @return default vehicle type array if order is NULL
//	 */
//	private VehicleTypes[] reorderVehicleTypes(edu.berkeley.path.beats.jaxb.VehicleTypeOrder order) {
//		if (null == order) return vehicle_type;
//		VehicleTypes[] reordered_vt = new VehicleTypes[order.getVehicleType().size()];
//		int i = 0;
//		for (edu.berkeley.path.beats.jaxb.VehicleType vt : order.getVehicleType()) {
//			reordered_vt[i] = null;
//			for (VehicleTypes db_vt : vehicle_type)
//				if (vt.getName().equals(db_vt.getName())) {
//					reordered_vt[i] = db_vt;
//					break;
//				}
//			++i;
//		}
//		return reordered_vt;
//	}
//
//	/**
//	 * Imports initial densities
//	 * @param idset
//	 * @return the imported initial density set
//	 * @throws TorqueException
//	 */
//	private InitialDensitySets save(edu.berkeley.path.beats.jaxb.InitialDensitySet idset) throws TorqueException {
//		if (null == idset) return null;
//		InitialDensitySets db_idsets = new InitialDensitySets();
//		db_idsets.setProjectId(getProjectId());
//		db_idsets.setName(idset.getName());
//		db_idsets.setDescription(idset.getDescription());
//		db_idsets.setActionTime(idset.getTstamp());
//		db_idsets.save(conn);
//		VehicleTypes[] db_vt = reorderVehicleTypes(idset.getVehicleTypeOrder());
//		for (edu.berkeley.path.beats.jaxb.Density density : idset.getDensity())
//			save(density, db_idsets, db_vt);
//		return db_idsets;
//	}
//
//	/**
//	 * Imports an initial density
//	 * @param density
//	 * @param db_idsets an imported initial density set
//	 * @param db_vt [possibly reordered] vehicle types
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Density density, InitialDensitySets db_idsets, VehicleTypes[] db_vt) throws TorqueException {
//		Data1D data1d = new Data1D(density.getContent(), ":");
//		if (!data1d.isEmpty()) {
//			BigDecimal[] data = data1d.getData();
//			if (data.length != db_vt.length)
//				logger.warn("initial density [link id=" + density.getLinkId() + "]: data.length=" + data.length + " and vehicle_types.length=" + db_vt.length + " differ");
//			for (int i = 0; i < data.length; ++i) {
//				InitialDensities db_id = new InitialDensities();
//				db_id.setInitialDensitySets(db_idsets);
//				db_id.setLinkId(getDBLinkId(density.getLinkId()));
//				db_id.setVehicleTypes(db_vt[i]);
//				if (null != density.getDestinationNetworkId())
//					db_id.setDestinationNetworks(this.destnets.get(density.getDestinationNetworkId()));
//				db_id.setDensity(data[i]);
//				db_id.save(conn);
//			}
//		}
//	}
//
//	/**
//	 * Imports weaving factors
//	 * @param wfset
//	 * @return the imported weaving factor set
//	 * @throws TorqueException
//	 */
//	private WeavingFactorSets save(edu.berkeley.path.beats.jaxb.WeavingFactorSet wfset) throws TorqueException {
//		if (null == wfset) return null;
//		WeavingFactorSets db_wfset = new WeavingFactorSets();
//		db_wfset.setProjectId(getProjectId());
//		db_wfset.setName(wfset.getName());
//		db_wfset.setDescription(wfset.getDescription());
//		db_wfset.save(conn);
//		VehicleTypes[] db_vt = reorderVehicleTypes(wfset.getVehicleTypeOrder());
//		for (edu.berkeley.path.beats.jaxb.Weavingfactors wf : wfset.getWeavingfactors())
//			save(wf, db_wfset, db_vt);
//		return db_wfset;
//	}
//
//	/**
//	 * Imports weaving factors
//	 * @param wf weaving factors to be imported
//	 * @param db_wfset an already imported weaving factor set
//	 * @param db_vt [imported] vehicle type list
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Weavingfactors wf, WeavingFactorSets db_wfset, VehicleTypes[] db_vt) throws TorqueException {
//		Data1D data1d = new Data1D(wf.getContent(), ":");
//		if (!data1d.isEmpty()) {
//			BigDecimal[] data = data1d.getData();
//			for (int i = 0; i < data.length; ++i) {
//				WeavingFactors db_wf = new WeavingFactors();
//				db_wf.setWeavingFactorSets(db_wfset);
//				db_wf.setInLinkId(getDBLinkId(wf.getLinkIn()));
//				db_wf.setOutLinkId(getDBLinkId(wf.getLinkOut()));
//				db_wf.setVehicleTypes(db_vt[i]);
//				db_wf.setFactor(data[i]);
//				db_wf.save(conn);
//			}
//		}
//	}
//
//	/**
//	 * Imports split ratio profiles
//	 * @param srps
//	 * @return the imported split ratio profile set
//	 * @throws TorqueException
//	 */
//	private SplitRatioProfileSets save(edu.berkeley.path.beats.jaxb.SplitRatioProfileSet srps) throws TorqueException {
//		if (null == srps) return null;
//		SplitRatioProfileSets db_srps = new SplitRatioProfileSets();
//		db_srps.setProjectId(getProjectId());
//		db_srps.setName(srps.getName());
//		db_srps.setDescription(srps.getDescription());
//		db_srps.save(conn);
//		for (edu.berkeley.path.beats.jaxb.SplitratioProfile srp : srps.getSplitratioProfile())
//			save(srp, db_srps, reorderVehicleTypes(srps.getVehicleTypeOrder()));
//		return db_srps;
//	}
//
//	/**
//	 * Imports a split ratio profile
//	 * @param srp
//	 * @param db_srps an already imported split ratio profile set
//	 * @param db_vt [imported] vehicle type list
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.SplitratioProfile srp, SplitRatioProfileSets db_srps, VehicleTypes[] db_vt) throws TorqueException {
//		SplitRatioProfiles db_srp = new SplitRatioProfiles();
//		db_srp.setSplitRatioProfileSets(db_srps);
//		db_srp.setNodeId(getDBNodeId(srp.getNodeId()));
//		if (null != srp.getDestinationNetworkId())
//			db_srp.setDestinationNetworks(this.destnets.get(srp.getDestinationNetworkId()));
//		db_srp.setSampleRate(srp.getDt());
//		db_srp.setStartTime(srp.getStartTime());
//		db_srp.save(conn);
//		for (edu.berkeley.path.beats.jaxb.Splitratio sr : srp.getSplitratio()) {
//			BigDecimal[][] data = new Data2D(sr.getContent(), new String[] {",", ":"}).getData();
//			if (null != data) {
//				for (int t = 0; t < data.length; ++t) {
//					if (data[t].length != db_vt.length)
//						logger.warn("split ratio data: data[time=" + t + "].length=" + data[t].length + " and vehicle_types.length=" + db_vt.length + " differ");
//					for (int vtn = 0; vtn < data[t].length; ++vtn) {
//						SplitRatios db_sr = new SplitRatios();
//						// common
//						db_sr.setSplitRatioProfiles(db_srp);
//						db_sr.setInLinkId(getDBLinkId(sr.getLinkIn()));
//						db_sr.setOutLinkId(getDBLinkId(sr.getLinkOut()));
//						// unique
//						db_sr.setVehicleTypes(db_vt[vtn]);
//						db_sr.setOrdinal(Integer.valueOf(t));
//						db_sr.setSplitRatio(data[t][vtn]);
//
//						db_sr.save(conn);
//					}
//				}
//			}
//		}
//	}
//
//	private FundamentalDiagramTypes getFundamentalDiagramTypes(String fd_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(FundamentalDiagramTypesPeer.NAME, fd_type);
//		@SuppressWarnings("unchecked")
//		List<FundamentalDiagramTypes> db_fdt_l = FundamentalDiagramTypesPeer.doSelect(crit, conn);
//		if (db_fdt_l.isEmpty()) {
//			logger.warn("FD type '" + fd_type + "' does not exist");
//			return createType(new FundamentalDiagramTypes(), fd_type);
//		} else {
//			if (1 < db_fdt_l.size())
//				logger.warn("Found " + db_fdt_l.size() + " fundamental diagram types '" + fd_type + "'");
//			return db_fdt_l.get(0);
//		}
//	}
//
//	/**
//	 * Imports a fundamental diagram profile set
//	 * @param fdps
//	 * @return the imported FD profile set
//	 * @throws TorqueException
//	 */
//	private FundamentalDiagramProfileSets save(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfileSet fdps) throws TorqueException {
//		FundamentalDiagramProfileSets db_fdps = new FundamentalDiagramProfileSets();
//		db_fdps.setProjectId(getProjectId());
//		db_fdps.setFundamentalDiagramTypes(getFundamentalDiagramTypes("triangular"));
//		db_fdps.setName(fdps.getName());
//		db_fdps.setDescription(fdps.getDescription());
//		db_fdps.save(conn);
//		for (edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdprofile : fdps.getFundamentalDiagramProfile())
//			save(fdprofile, db_fdps);
//		return db_fdps;
//	}
//
//	/**
//	 * Imports a fundamental diagram profile
//	 * @param fdprofile
//	 * @param db_fdps an already imported FD profile set
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdprofile, FundamentalDiagramProfileSets db_fdps) throws TorqueException {
//		FundamentalDiagramProfiles db_fdprofile = new FundamentalDiagramProfiles();
//		db_fdprofile.setFundamentalDiagramProfileSets(db_fdps);
//		db_fdprofile.setLinkId(getDBLinkId(fdprofile.getLinkId()));
//		db_fdprofile.setSampleRate(fdprofile.getDt());
//		db_fdprofile.setStartTime(fdprofile.getStartTime());
//		db_fdprofile.save(conn);
//		int num = 0;
//		for (edu.berkeley.path.beats.jaxb.FundamentalDiagram fd : fdprofile.getFundamentalDiagram())
//			save(fd, db_fdprofile, num++);
//	}
//
//	/**
//	 * Imports a fundamental diagram
//	 * @param fd
//	 * @param db_fdprofile an already imported FD profile
//	 * @param number order of the FD
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.FundamentalDiagram fd, FundamentalDiagramProfiles db_fdprofile, int number) throws TorqueException {
//		FundamentalDiagrams db_fd = new FundamentalDiagrams();
//		db_fd.setFundamentalDiagramProfiles(db_fdprofile);
//		db_fd.setNumber(number);
//		db_fd.setFreeFlowSpeed(fd.getFreeFlowSpeed());
//		db_fd.setCriticalSpeed(fd.getCriticalSpeed());
//		db_fd.setCongestionWaveSpeed(fd.getCongestionSpeed());
//		db_fd.setCapacity(fd.getCapacity());
//		db_fd.setJamDensity(fd.getJamDensity());
//		db_fd.setCapacityDrop(fd.getCapacityDrop());
//		db_fd.setCapacityStd(fd.getStdDevCapacity());
//		db_fd.setFreeFlowSpeedStd(fd.getStdDevFreeFlowSpeed());
//		db_fd.setCongestionWaveSpeedStd(fd.getStdDevCongestionSpeed());
//		db_fd.save(conn);
//	}
//
//	/**
//	 * Imports a demand profile set
//	 * @param dpset
//	 * @return the imported demand profile set
//	 * @throws TorqueException
//	 */
//	private DemandProfileSets save(edu.berkeley.path.beats.jaxb.DemandProfileSet dpset) throws TorqueException {
//		if (null == dpset) return null;
//		DemandProfileSets db_dpset = new DemandProfileSets();
//		db_dpset.setProjectId(getProjectId());
//		db_dpset.setName(dpset.getName());
//		db_dpset.setDescription(dpset.getDescription());
//		db_dpset.save(conn);
//		VehicleTypes[] db_vt = reorderVehicleTypes(dpset.getVehicleTypeOrder());
//		for (edu.berkeley.path.beats.jaxb.DemandProfile dp : dpset.getDemandProfile())
//			save(dp, db_dpset, db_vt);
//		return db_dpset;
//	}
//
//	/**
//	 * Imports a demand profile
//	 * @param dp a demand profile
//	 * @param db_dpset an already imported demand profile set
//	 * @param db_vt [imported] vehicle type list
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.DemandProfile dp, DemandProfileSets db_dpset, VehicleTypes[] db_vt) throws TorqueException {
//		DemandProfiles db_dp = new DemandProfiles();
//		db_dp.setDemandProfileSets(db_dpset);
//		db_dp.setOriginLinkId(getDBLinkId(dp.getLinkIdOrigin()));
//		if (null != dp.getDestinationNetworkId())
//			db_dp.setDestinationNetworks(this.destnets.get(dp.getDestinationNetworkId()));
//		db_dp.setSampleRate(dp.getDt());
//		db_dp.setStartTime(dp.getStartTime());
//		db_dp.setKnob(dp.getKnob());
//		db_dp.setStdDeviationAdditive(dp.getStdDevAdd());
//		db_dp.setStdDeviationMultiplicative(dp.getStdDevMult());
//		db_dp.save(conn);
//		Data2D data2d = new Data2D(dp.getContent(), new String[] {",", ":"});
//		if (!data2d.isEmpty()) {
//			BigDecimal[][] data = data2d.getData();
//			for (int t = 0; t < data.length; ++t) {
//				for (int vtn = 0; vtn < data[t].length; ++vtn) {
//					Demands db_demand = new Demands();
//					db_demand.setDemandProfiles(db_dp);
//					db_demand.setVehicleTypes(db_vt[vtn]);
//					db_demand.setNumber(t);
//					db_demand.setDemand(data[t][vtn]);
//					db_demand.save(conn);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Imports a network connection list
//	 * @param nconns
//	 * @return the imported network connection set
//	 * @throws TorqueException
//	 */
//	private NetworkConnectionSets save(edu.berkeley.path.beats.jaxb.NetworkConnections nconns) throws TorqueException {
//		if (null == nconns) return null;
//		NetworkConnectionSets db_ncs = new NetworkConnectionSets();
//		db_ncs.setProjectId(getProjectId());
//		db_ncs.setName(nconns.getName());
//		db_ncs.setDescription(nconns.getDescription());
//		db_ncs.save(conn);
//		for (edu.berkeley.path.beats.jaxb.Networkpair np : nconns.getNetworkpair())
//			save(np, db_ncs);
//		return db_ncs;
//	}
//
//	/**
//	 * Imports network connections
//	 * @param np
//	 * @param db_ncs an already imported network connection set
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Networkpair np, NetworkConnectionSets db_ncs) throws TorqueException {
//		for (edu.berkeley.path.beats.jaxb.Linkpair lp : np.getLinkpair()) {
//			NetworkConnections db_nc = new NetworkConnections();
//			db_nc.setNetworkConnectionSets(db_ncs);
//			db_nc.setFromNetworkId(network_id.get(np.getNetworkA()));
//			db_nc.setFromLinkId(getDBLinkId(lp.getLinkA()));
//			db_nc.setToNetworkId(network_id.get(np.getNetworkB()));
//			db_nc.setToLinkId(getDBLinkId(lp.getLinkB()));
//			db_nc.save(conn);
//		}
//	}
//
//	/**
//	 * Imports downstream boundary capacity profiles
//	 * @param dbcps
//	 * @return the imported downstream boundary capacity profile set
//	 * @throws TorqueException
//	 */
//	private DownstreamBoundaryCapacityProfileSets save(edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfileSet dbcps) throws TorqueException {
//		if (null == dbcps) return null;
//		DownstreamBoundaryCapacityProfileSets db_dbcps = new DownstreamBoundaryCapacityProfileSets();
//		db_dbcps.setProjectId(getProjectId());
//		db_dbcps.setName(dbcps.getName());
//		db_dbcps.setDescription(dbcps.getDescription());
//		db_dbcps.save(conn);
//		for (edu.berkeley.path.beats.jaxb.CapacityProfile cp : dbcps.getCapacityProfile())
//			save(cp, db_dbcps);
//		return db_dbcps;
//	}
//
//	/**
//	 * Imports a downstream boundary capacity profile
//	 * @param cp
//	 * @param db_dbcps an already imported capacity profile set
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.CapacityProfile cp, DownstreamBoundaryCapacityProfileSets db_dbcps) throws TorqueException {
//		DownstreamBoundaryCapacityProfiles db_dbcp = new DownstreamBoundaryCapacityProfiles();
//		db_dbcp.setDownstreamBoundaryCapacityProfileSets(db_dbcps);
//		db_dbcp.setLinkId(getDBLinkId(cp.getLinkId()));
//		db_dbcp.setSampleRate(cp.getDt());
//		db_dbcp.setStartTime(cp.getStartTime());
//		db_dbcp.save(conn);
//		Data1D data1d = new Data1D(cp.getContent(), ",");
//		if (!data1d.isEmpty()) {
//			BigDecimal[] data = data1d.getData();
//			for (int number = 0; number < data.length; ++number) {
//				DownstreamBoundaryCapacities db_dbc = new DownstreamBoundaryCapacities();
//				db_dbc.setDownstreamBoundaryCapacityProfiles(db_dbcp);
//				db_dbc.setNumber(number);
//				db_dbc.setDownstreamBoundaryCapacity(data[number]);
//				db_dbc.save(conn);
//			}
//		}
//	}
//
//	/**
//	 * Imports a controller set
//	 * @param cset
//	 * @return an imported controller set
//	 * @throws TorqueException
//	 */
//	private ControllerSets save(edu.berkeley.path.beats.jaxb.ControllerSet cset) throws TorqueException {
//		if (null == cset) return null;
//		ControllerSets db_cset = new ControllerSets();
//		db_cset.setProjectId(getProjectId());
//		db_cset.setName(cset.getName());
//		db_cset.setDescription(cset.getDescription());
//		db_cset.save(conn);
//		controllers = new HashMap<Long, Controllers>(cset.getController().size());
//		for (edu.berkeley.path.beats.jaxb.Controller controller : cset.getController())
//			controllers.put(controller.getId(), save(controller, db_cset));
//		return db_cset;
//	}
//
//	private ControllerTypes getControllerType(String controller_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(ControllerTypesPeer.NAME, controller_type);
//		@SuppressWarnings("unchecked")
//		List<ControllerTypes> db_ct_l = ControllerTypesPeer.doSelect(crit, conn);
//		if (db_ct_l.isEmpty()) {
//			logger.warn("Controller type '" + controller_type + "' does not exist");
//			return createType(new ControllerTypes(), controller_type);
//		} else {
//			if (1 < db_ct_l.size())
//				logger.warn("Found " + db_ct_l.size() + " controller types '" + controller_type + "'");
//			return db_ct_l.get(0);
//		}
//	}
//
//	/**
//	 * Imports a controller
//	 * @param cntr a controller
//	 * @param db_cset an imported controller set
//	 * @return an imported controller
//	 * @throws TorqueException
//	 */
//	private Controllers save(edu.berkeley.path.beats.jaxb.Controller cntr, ControllerSets db_cset) throws TorqueException {
//		Controllers db_cntr = new Controllers();
//		db_cntr.setControllerSets(db_cset);
//		db_cntr.setControllerTypes(getControllerType(cntr.getType()));
//		db_cntr.setJavaClass(cntr.getJavaClass());
//		db_cntr.setDt(cntr.getDt());
//		db_cntr.setDisplayGeometry(pos2str(cntr.getDisplayPosition()));
//		db_cntr.save(conn);
//		save(cntr.getQueueController(), db_cntr);
//		save(cntr.getParameters(), db_cntr);
//		for (edu.berkeley.path.beats.jaxb.Table table : cntr.getTable())
//			save(table, db_cntr);
//		save(cntr.getActivationIntervals(), db_cntr);
//		return db_cntr;
//	}
//
//	private QueueControllerTypes getQueueControllerType(String qc_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(QueueControllerTypesPeer.NAME, qc_type);
//		@SuppressWarnings("unchecked")
//		List<QueueControllerTypes> db_qct_l = QueueControllerTypesPeer.doSelectVillageRecords(crit, conn);
//		if (db_qct_l.isEmpty()) {
//			logger.warn("Queue controller type '" + qc_type + "' does not exist");
//			return createType(new QueueControllerTypes(), qc_type);
//		} else {
//			if (1 < db_qct_l.size())
//				logger.warn("Found " + db_qct_l.size() + " queue controller types '" + qc_type + "'");
//			return db_qct_l.get(0);
//		}
//	}
//
//	/**
//	 * Imports a queue controller
//	 * @param qc the queue controller
//	 * @param db_cntr an imported controller
//	 * @return an imported queue controller
//	 * @throws TorqueException
//	 */
//	private QueueControllers save(edu.berkeley.path.beats.jaxb.QueueController qc, Controllers db_cntr) throws TorqueException {
//		if (null == qc) return null;
//		QueueControllers db_qc = new QueueControllers();
//		db_qc.setQueueControllerTypes(getQueueControllerType(qc.getType()));
//		db_qc.setControllers(db_cntr);
//		db_qc.setJavaClass(qc.getJavaClass());
//		db_qc.save(conn);
//		save(qc.getParameters(), db_qc);
//		return db_qc;
//	}
//
//	/**
//	 * Imports controller activation intervals
//	 * @param ais activation intervals
//	 * @param db_cntr an imported controller
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.ActivationIntervals ais, Controllers db_cntr) throws TorqueException {
//		if (null == ais) return;
//		for (edu.berkeley.path.beats.jaxb.Interval interval : ais.getInterval()) {
//			ControllerActivationIntervals db_cai = new ControllerActivationIntervals();
//			db_cai.setControllers(db_cntr);
//			db_cai.setStartTime(interval.getStartTime());
//			db_cai.setDuration(interval.getEndTime().subtract(interval.getStartTime()));
//			db_cai.save(conn);
//		}
//	}
//
//	/**
//	 * Imports an event set
//	 * @param eset
//	 * @return an imported event set
//	 * @throws TorqueException
//	 */
//	private EventSets save(edu.berkeley.path.beats.jaxb.EventSet eset) throws TorqueException {
//		if (null == eset) return null;
//		EventSets db_eset = new EventSets();
//		db_eset.setProjectId(getProjectId());
//		db_eset.setName(eset.getName());
//		db_eset.setDescription(eset.getDescription());
//		db_eset.save(conn);
//
//		events = new HashMap<Long, Events>(eset.getEvent().size());
//		for (edu.berkeley.path.beats.jaxb.Event event : eset.getEvent())
//			events.put(event.getId(), save(event, db_eset));
//
//		return db_eset;
//	}
//
//	private EventTypes getEventType(String event_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(EventTypesPeer.NAME, event_type);
//		@SuppressWarnings("unchecked")
//		List<EventTypes> db_event_type_l = EventTypesPeer.doSelect(crit, conn);
//		if (!db_event_type_l.isEmpty()) {
//			if (1 < db_event_type_l.size())
//				logger.warn("Found " + db_event_type_l.size() + " event types '" + event_type + "'");
//			return db_event_type_l.get(0);
//		} else {
//			logger.warn("Event type '" + event_type + "' does not exist");
//			return createType(new EventTypes(), event_type);
//		}
//	}
//
//	/**
//	 * Imports an event
//	 * @param event
//	 * @param db_eset an imported event set
//	 * @return an imported event
//	 * @throws TorqueException
//	 */
//	private Events save(edu.berkeley.path.beats.jaxb.Event event, EventSets db_eset) throws TorqueException {
//		Events db_event = new Events();
//		db_event.setEventSets(db_eset);
//		db_event.setActionTime(event.getTstamp());
//		db_event.setEventTypes(getEventType(event.getType()));
//		db_event.setJavaClass(event.getJavaClass());
//		db_event.setDescription(event.getDescription());
//		db_event.setDisplayGeometry(pos2str(event.getDisplayPosition()));
//		db_event.setEnabled(event.isEnabled());
//		db_event.save(conn);
//		save(event.getParameters(), db_event);
//		save(event.getSplitratioEvent(), db_event);
//		return db_event;
//	}
//
//	/**
//	 * Imports a split ratio event
//	 * @param srevent
//	 * @param db_event an imported event
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.SplitratioEvent srevent, Events db_event) throws TorqueException {
//		if (null == srevent) return;
//		VehicleTypes[] db_vt = reorderVehicleTypes(srevent.getVehicleTypeOrder());
//		for (edu.berkeley.path.beats.jaxb.Splitratio sr : srevent.getSplitratio())
//			save(sr, db_event, db_vt);
//	}
//
//	/**
//	 * Imports a split ratio of a split ratio event
//	 * @param sr a split ratio
//	 * @param db_event an imported event
//	 * @param db_vt vehicle type order
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Splitratio sr, Events db_event, VehicleTypes[] db_vt) throws TorqueException {
//		Data1D data1d = new Data1D(sr.getContent(), ":");
//		if (!data1d.isEmpty()) {
//			BigDecimal[] data = data1d.getData();
//			for (int i = 0; i < data.length; ++i) {
//				EventSplitRatios db_esr = new EventSplitRatios();
//				db_esr.setEvents(db_event);
//				db_esr.setInLinkId(getDBLinkId(sr.getLinkIn()));
//				db_esr.setOutLinkId(getDBLinkId(sr.getLinkOut()));
//				db_esr.setVehicleTypes(db_vt[i]);
//				db_esr.setSplitRatio(data[i]);
//				db_esr.save(conn);
//			}
//		}
//	}
//
//	/**
//	 * Imports destination networks
//	 * @param destnets destination networks
//	 * @param db_scenario an imported scenario
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.DestinationNetworks destnets, Scenarios db_scenario) throws TorqueException {
//		if (null == destnets) return;
//		this.destnets = new HashMap<Long, DestinationNetworks>(destnets.getDestinationNetwork().size());
//		for (edu.berkeley.path.beats.jaxb.DestinationNetwork destnet : destnets.getDestinationNetwork()) {
//			DestinationNetworkSets db_destnetset = new DestinationNetworkSets();
//			db_destnetset.setScenarios(db_scenario);
//			db_destnetset.setDestinationNetworks(save(destnet));
//			db_destnetset.save(conn);
//		}
//	}
//
//	/**
//	 * Imports a destination network
//	 * @param destnet a destination network
//	 * @return an imported destination network
//	 * @throws TorqueException
//	 */
//	private DestinationNetworks save(edu.berkeley.path.beats.jaxb.DestinationNetwork destnet) throws TorqueException {
//		DestinationNetworks db_destnet = new DestinationNetworks();
//		db_destnet.setDestinationLinkId(getDBLinkId(destnet.getLinkIdDestination()));
//		db_destnet.setProjectId(getProjectId());
//		db_destnet.save(conn);
//		for (edu.berkeley.path.beats.jaxb.LinkReference linkref : destnet.getLinkReferences().getLinkReference())
//			save(linkref, db_destnet);
//		this.destnets.put(destnet.getId(), db_destnet);
//		return db_destnet;
//	}
//
//	/**
//	 * Imports destination network's link reference
//	 * @param linkref a link reference
//	 * @param db_destnet an imported destination network
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.LinkReference linkref, DestinationNetworks db_destnet) throws TorqueException {
//		DestinationNetworkLinks db_dnl = new DestinationNetworkLinks();
//		db_dnl.setLinkId(getDBLinkId(linkref.getId()));
//		db_dnl.setDestinationNetworks(db_destnet);
//		db_dnl.save(conn);
//	}

//	/**
//	 * Imports routes
//	 * @param routes
//	 * @param db_scenario an imported scenario
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.RouteSet routes, Scenarios db_scenario) throws TorqueException {
//		if (null == routes) return;
//		for (edu.berkeley.path.beats.jaxb.Route route : routes.getRoute()) {
//			RouteSets db_rs = new RouteSets();
//			db_rs.setScenarios(db_scenario);
//			db_rs.setRoutes(save(route));
//			db_rs.save(conn);
//		}
//	}
//
//	/**
//	 * Imports a route
//	 * @param route
//	 * @return an imported route
//	 * @throws TorqueException
//	 */
//	private Routes save(edu.berkeley.path.beats.jaxb.Route route) throws TorqueException {
//		Routes db_route = new Routes();
//		db_route.setProjectId(getProjectId());
//		db_route.setName(route.getName());
//		int ordinal = 0;
//		for (edu.berkeley.path.beats.jaxb.LinkReference lr : route.getLinkReferences().getLinkReference()) {
//			RouteLinks db_rl = new RouteLinks();
//			db_rl.setLinkId(getDBLinkId(lr.getId()));
//			db_rl.setOrdinal(Integer.valueOf(ordinal++));
//			db_route.addRouteLinks(db_rl);
//		}
//		db_route.save(conn);
//		return db_route;
//	}
//
//	private ScenarioElementTypes getScenarioElementTypes(String scenario_element_type) throws TorqueException {
//		Criteria crit = new Criteria();
//		crit.add(ScenarioElementTypesPeer.NAME, scenario_element_type);
//		@SuppressWarnings("unchecked")
//		List<ScenarioElementTypes> db_scelt_l = ScenarioElementTypesPeer.doSelect(crit, conn);
//		if (db_scelt_l.isEmpty()) {
//			logger.warn("Scenario element type '" + scenario_element_type + "' does not exist");
//			return createType(new ScenarioElementTypes(), scenario_element_type);
//		} else {
//			if (1 < db_scelt_l.size())
//				logger.warn("Found " + db_scelt_l.size() + " scenario element types '" + scenario_element_type + "'");
//			return db_scelt_l.get(0);
//		}
//	}
//
//	private ScenarioElementTypes getScenarioElementTypes(edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		return getScenarioElementTypes(db_obj.getElementType());
//	}
//
//	/**
//	 * Imports parameters
//	 * @param params
//	 * @param db_obj an imported parent element
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Parameters params, edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		if (null == params) return;
//		ScenarioElementTypes db_scelt = getScenarioElementTypes(db_obj);
//		for (edu.berkeley.path.beats.jaxb.Parameter param : params.getParameter()) {
//			edu.berkeley.path.beats.om.Parameters db_param = new edu.berkeley.path.beats.om.Parameters();
//			db_param.setElementId(db_obj.getId());
//			db_param.setScenarioElementTypes(db_scelt);
//			db_param.setName(param.getName());
//			db_param.setValue(param.getValue());
//			db_param.save(conn);
//		}
//	}
//
//	/**
//	 * Imports a table
//	 * @param table
//	 * @param db_obj an imported parent element
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.Table table, edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		if (null == table) return;
//		Tables db_table = new Tables();
//		db_table.setName(table.getName());
//		db_table.setElementId(db_obj.getId());
//		db_table.setScenarioElementTypes(getScenarioElementTypes(db_obj));
//		db_table.save(conn);
//
//		int colnum = 0;
//		for (edu.berkeley.path.beats.jaxb.ColumnName colname : table.getColumnNames().getColumnName()) {
//			TabularDataKeys db_tdk = new TabularDataKeys();
//			db_tdk.setTables(db_table);
//			db_tdk.setColumnName(colname.getName());
//			db_tdk.setColumnNumber(Integer.valueOf(colnum++));
//			db_tdk.setIsKey(colname.isKey());
//			db_tdk.save(conn);
//		}
//		int rownum = 0;
//		for (edu.berkeley.path.beats.jaxb.Row row : table.getRow()) {
//			java.util.Iterator<edu.berkeley.path.beats.jaxb.ColumnName> citer = table.getColumnNames().getColumnName().iterator();
//			for (String elem : row.getColumn()) {
//				String column_name = citer.next().getName();
//				if ("Intersection".equals(column_name)) {
//					long longelem = Long.parseLong(elem);
//					logger.debug("Column `" + table.getName() + "`.`" + column_name + "`: " + elem + " -> " + getDBNodeId(longelem));
//					elem = ScenarioExporter.id2str(getDBNodeId(longelem));
//				}
//				TabularData db_td = new TabularData();
//				db_td.setTables(db_table);
//				db_td.setColumnName(column_name);
//				db_td.setRowNumber(Integer.valueOf(rownum));
//				db_td.setValue(elem);
//				db_td.save(conn);
//			}
//			++rownum;
//		}
//	}
//
//	/**
//	 * Imports target elements
//	 * @param elems
//	 * @param db_obj an imported parent element
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.TargetElements elems, edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		if (null == elems) return;
//		for (edu.berkeley.path.beats.jaxb.ScenarioElement elem : elems.getScenarioElement())
//			save(elem, "target", db_obj);
//	}
//
//	/**
//	 * Imports feedback elements
//	 * @param elems
//	 * @param db_obj an imported parent element
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.FeedbackElements elems, edu.berkeley.path.beats.db.BaseObject db_obj) throws TorqueException {
//		if (null == elems) return;
//		for (edu.berkeley.path.beats.jaxb.ScenarioElement elem : elems.getScenarioElement())
//			save(elem, "feedback", db_obj);
//	}
//
//	/**
//	 * Imports a referenced scenario element
//	 * @param elem scenario element
//	 * @param type "target" or "feedback"
//	 * @param db_parent an imported parent element
//	 * @throws TorqueException
//	 */
//	private void save(edu.berkeley.path.beats.jaxb.ScenarioElement elem, String type, edu.berkeley.path.beats.db.BaseObject db_parent) throws TorqueException {
//		ReferencedScenarioElements db_elems = new ReferencedScenarioElements();
//		db_elems.setParentElementId(db_parent.getId());
//		db_elems.setScenarioElementTypesRelatedByParentElementTypeId(getScenarioElementTypes(db_parent));
//		db_elems.setType(type);
//		db_elems.setUsage(elem.getUsage());
//		edu.berkeley.path.beats.db.BaseObject db_ref = null;
//		if (elem.getType().equals("link")) {
//			if (null != links) db_ref = links.get(elem.getId());
//		} else if (elem.getType().equals("node")) {
//			if (null != nodes) db_ref = nodes.get(elem.getId());
//		} else if (elem.getType().equals("controller")) {
//			if (null != controllers) db_ref = controllers.get(elem.getId());
//		} else if (elem.getType().equals("sensor")) {
//			if (null != sensors) db_ref = sensors.get(elem.getId());
//		} else if (elem.getType().equals("event")) {
//			if (null != events) db_ref = events.get(elem.getId());
//		} else if (elem.getType().equals("signal")) {
//			if (null != signals) db_ref = signals.get(elem.getId());
//		} else
//			logger.error("Reference to a " + elem.getType() + " is not implemented");
//		if (null != db_ref) {
//			db_elems.setElementId(db_ref.getId());
//			db_elems.setScenarioElementTypesRelatedByElementTypeId(getScenarioElementTypes(db_ref));
//			db_elems.save(conn);
//		} else
//			logger.error("Object " + elem.getType() + " [id=" + elem.getId() + "] not found");
//	}
//
//	private String pos2str(edu.berkeley.path.beats.jaxb.Position position) {
//		return null == position ? null : encodePolyline(position.getPoint());
//	}
//
//	private String pos2str(edu.berkeley.path.beats.jaxb.DisplayPosition position) {
//		return null == position ? null : encodePolyline(position.getPoint());
//	}
//
//	private String encodePolyline(List<edu.berkeley.path.beats.jaxb.Point> point_l) {
//		polyline_encoder.reset();
//		polyline_encoder.add(point_l);
//		return polyline_encoder.getResult();
//	}
}
