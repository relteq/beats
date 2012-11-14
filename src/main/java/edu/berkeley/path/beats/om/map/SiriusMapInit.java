package com.relteq.sirius.om.map;

import org.apache.torque.TorqueException;

/**
 * This is a Torque Generated class that is used to load all database map 
 * information at once.  This is useful because Torque's default behaviour
 * is to do a "lazy" load of mapping information, e.g. loading it only
 * when it is needed.<p>
 *
 * @see org.apache.torque.map.DatabaseMap#initialize() DatabaseMap.initialize() 
 */
public class SiriusMapInit
{
    public static final void init()
        throws TorqueException
    {
        com.relteq.sirius.om.ProjectsPeer.getMapBuilder();
        com.relteq.sirius.om.NetworksPeer.getMapBuilder();
        com.relteq.sirius.om.NodeFamiliesPeer.getMapBuilder();
        com.relteq.sirius.om.NodesPeer.getMapBuilder();
        com.relteq.sirius.om.NodeNamePeer.getMapBuilder();
        com.relteq.sirius.om.NodeTypePeer.getMapBuilder();
        com.relteq.sirius.om.PostmileHighwaysPeer.getMapBuilder();
        com.relteq.sirius.om.PostmilesPeer.getMapBuilder();
        com.relteq.sirius.om.LinkFamiliesPeer.getMapBuilder();
        com.relteq.sirius.om.LinksPeer.getMapBuilder();
        com.relteq.sirius.om.LinkNamePeer.getMapBuilder();
        com.relteq.sirius.om.LinkTypePeer.getMapBuilder();
        com.relteq.sirius.om.LinkLanesPeer.getMapBuilder();
        com.relteq.sirius.om.LinkLaneOffsetPeer.getMapBuilder();
        com.relteq.sirius.om.LinkSpeedLimitPeer.getMapBuilder();
        com.relteq.sirius.om.LinkTurnRestrictionsPeer.getMapBuilder();
        com.relteq.sirius.om.NetworkConnectionSetsPeer.getMapBuilder();
        com.relteq.sirius.om.NetworkConnectionsPeer.getMapBuilder();
        com.relteq.sirius.om.DestinationNetworksPeer.getMapBuilder();
        com.relteq.sirius.om.DestinationNetworkLinksPeer.getMapBuilder();
        com.relteq.sirius.om.RoutesPeer.getMapBuilder();
        com.relteq.sirius.om.RouteLinksPeer.getMapBuilder();
        com.relteq.sirius.om.VehicleTypeSetsPeer.getMapBuilder();
        com.relteq.sirius.om.VehicleTypesPeer.getMapBuilder();
        com.relteq.sirius.om.VehicleTypesInSetsPeer.getMapBuilder();
        com.relteq.sirius.om.InitialDensitySetsPeer.getMapBuilder();
        com.relteq.sirius.om.InitialDensitiesPeer.getMapBuilder();
        com.relteq.sirius.om.WeavingFactorSetsPeer.getMapBuilder();
        com.relteq.sirius.om.WeavingFactorsPeer.getMapBuilder();
        com.relteq.sirius.om.SplitRatioProfileSetsPeer.getMapBuilder();
        com.relteq.sirius.om.SplitRatioProfilesPeer.getMapBuilder();
        com.relteq.sirius.om.SplitRatiosPeer.getMapBuilder();
        com.relteq.sirius.om.FundamentalDiagramProfileSetsPeer.getMapBuilder();
        com.relteq.sirius.om.FundamentalDiagramProfilesPeer.getMapBuilder();
        com.relteq.sirius.om.FundamentalDiagramsPeer.getMapBuilder();
        com.relteq.sirius.om.DemandProfileSetsPeer.getMapBuilder();
        com.relteq.sirius.om.DemandProfilesPeer.getMapBuilder();
        com.relteq.sirius.om.DemandsPeer.getMapBuilder();
        com.relteq.sirius.om.DownstreamBoundaryCapacityProfileSetsPeer.getMapBuilder();
        com.relteq.sirius.om.DownstreamBoundaryCapacityProfilesPeer.getMapBuilder();
        com.relteq.sirius.om.DownstreamBoundaryCapacitiesPeer.getMapBuilder();
        com.relteq.sirius.om.SensorSetsPeer.getMapBuilder();
        com.relteq.sirius.om.SensorsPeer.getMapBuilder();
        com.relteq.sirius.om.SignalSetsPeer.getMapBuilder();
        com.relteq.sirius.om.SignalsPeer.getMapBuilder();
        com.relteq.sirius.om.PhasesPeer.getMapBuilder();
        com.relteq.sirius.om.PhaseLinksPeer.getMapBuilder();
        com.relteq.sirius.om.ControllerSetsPeer.getMapBuilder();
        com.relteq.sirius.om.QueueControllersPeer.getMapBuilder();
        com.relteq.sirius.om.ControllersPeer.getMapBuilder();
        com.relteq.sirius.om.ControllerActivationIntervalsPeer.getMapBuilder();
        com.relteq.sirius.om.EnkfNoiseParameterSetsPeer.getMapBuilder();
        com.relteq.sirius.om.EnkfNoiseParametersPeer.getMapBuilder();
        com.relteq.sirius.om.EventSetsPeer.getMapBuilder();
        com.relteq.sirius.om.EventsPeer.getMapBuilder();
        com.relteq.sirius.om.EventSplitRatiosPeer.getMapBuilder();
        com.relteq.sirius.om.ParametersPeer.getMapBuilder();
        com.relteq.sirius.om.TablesPeer.getMapBuilder();
        com.relteq.sirius.om.TabularDataPeer.getMapBuilder();
        com.relteq.sirius.om.TabularDataKeysPeer.getMapBuilder();
        com.relteq.sirius.om.ReferencedScenarioElementsPeer.getMapBuilder();
        com.relteq.sirius.om.ScenariosPeer.getMapBuilder();
        com.relteq.sirius.om.NetworkSetsPeer.getMapBuilder();
        com.relteq.sirius.om.DestinationNetworkSetsPeer.getMapBuilder();
        com.relteq.sirius.om.RouteSetsPeer.getMapBuilder();
        com.relteq.sirius.om.DataSourcesPeer.getMapBuilder();
        com.relteq.sirius.om.SimulationRunsPeer.getMapBuilder();
        com.relteq.sirius.om.DefaultSimulationSettingsPeer.getMapBuilder();
        com.relteq.sirius.om.LinkDataTotalPeer.getMapBuilder();
        com.relteq.sirius.om.LinkDataDetailedPeer.getMapBuilder();
        com.relteq.sirius.om.LinkPerformanceTotalPeer.getMapBuilder();
        com.relteq.sirius.om.LinkPerformanceDetailedPeer.getMapBuilder();
        com.relteq.sirius.om.RoutePerformanceTotalPeer.getMapBuilder();
        com.relteq.sirius.om.SignalDataPeer.getMapBuilder();
        com.relteq.sirius.om.SignalPhasePerformancePeer.getMapBuilder();
    }
}
