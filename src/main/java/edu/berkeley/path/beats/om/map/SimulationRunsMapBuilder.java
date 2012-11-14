package com.relteq.sirius.om.map;

import java.util.Date;
import java.math.BigDecimal;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.map.MapBuilder;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.map.TableMap;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.map.InheritanceMap;

/**
  *  This class was autogenerated by Torque on:
  *
  * [Wed Nov 14 14:25:09 PST 2012]
  *
  */
public class SimulationRunsMapBuilder implements MapBuilder
{
    /**
     * The name of this class
     */
    public static final String CLASS_NAME =
        "com.relteq.sirius.om.map.SimulationRunsMapBuilder";

    /**
     * The database map.
     */
    private DatabaseMap dbMap = null;

    /**
     * Tells us if this DatabaseMapBuilder is built so that we
     * don't have to re-build it every time.
     *
     * @return true if this DatabaseMapBuilder is built
     */
    public boolean isBuilt()
    {
        return (dbMap != null);
    }

    /**
     * Gets the databasemap this map builder built.
     *
     * @return the databasemap
     */
    public DatabaseMap getDatabaseMap()
    {
        return this.dbMap;
    }

    /**
     * The doBuild() method builds the DatabaseMap
     *
     * @throws TorqueException
     */
    public synchronized void doBuild() throws TorqueException
    {
        if ( isBuilt() ) {
            return;
        }
        dbMap = Torque.getDatabaseMap("sirius");

        dbMap.addTable("simulation_runs");
        TableMap tMap = dbMap.getTable("simulation_runs");
        tMap.setJavaName("SimulationRuns");
        tMap.setOMClass( com.relteq.sirius.om.SimulationRuns.class );
        tMap.setPeerClass( com.relteq.sirius.om.SimulationRunsPeer.class );
        tMap.setPrimaryKeyMethod("none");

        ColumnMap cMap = null;


  // ------------- Column: data_source_id --------------------
        cMap = new ColumnMap( "data_source_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(true);
        cMap.setNotNull(false);
        cMap.setJavaName( "DataSourceId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("data_sources", "id");
        cMap.setPosition(1);
        tMap.addColumn(cMap);
  // ------------- Column: scenario_id --------------------
        cMap = new ColumnMap( "scenario_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "ScenarioId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("scenarios", "id");
        cMap.setPosition(2);
        tMap.addColumn(cMap);
  // ------------- Column: run_number --------------------
        cMap = new ColumnMap( "run_number", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "RunNumber" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(3);
        tMap.addColumn(cMap);
  // ------------- Column: version --------------------
        cMap = new ColumnMap( "version", tMap);
        cMap.setType( "" );
        cMap.setTorqueType( "VARCHAR" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Version" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setSize( 64 );
        cMap.setPosition(4);
        tMap.addColumn(cMap);
  // ------------- Column: build --------------------
        cMap = new ColumnMap( "build", tMap);
        cMap.setType( "" );
        cMap.setTorqueType( "VARCHAR" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Build" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setSize( 64 );
        cMap.setPosition(5);
        tMap.addColumn(cMap);
  // ------------- Column: simulation_start_time --------------------
        cMap = new ColumnMap( "simulation_start_time", tMap);
        cMap.setType( new BigDecimal((double) 0) );
        cMap.setTorqueType( "DECIMAL" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "SimulationStartTime" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Start time in seconds. Default is 0.");
        cMap.setInheritance("false");
        cMap.setSize( 31 );
        cMap.setScale( 16 );
        cMap.setPosition(6);
        tMap.addColumn(cMap);
  // ------------- Column: simulation_duration --------------------
        cMap = new ColumnMap( "simulation_duration", tMap);
        cMap.setType( new BigDecimal((double) 0) );
        cMap.setTorqueType( "DECIMAL" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "SimulationDuration" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Active time in seconds. Default is 86400 (24 hours).");
        cMap.setInheritance("false");
        cMap.setSize( 31 );
        cMap.setScale( 16 );
        cMap.setPosition(7);
        tMap.addColumn(cMap);
  // ------------- Column: simulation_dt --------------------
        cMap = new ColumnMap( "simulation_dt", tMap);
        cMap.setType( new BigDecimal((double) 0) );
        cMap.setTorqueType( "DECIMAL" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "SimulationDt" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Simulation step in seconds. Default is 1.");
        cMap.setInheritance("false");
        cMap.setSize( 31 );
        cMap.setScale( 16 );
        cMap.setPosition(8);
        tMap.addColumn(cMap);
  // ------------- Column: output_dt --------------------
        cMap = new ColumnMap( "output_dt", tMap);
        cMap.setType( new BigDecimal((double) 0) );
        cMap.setTorqueType( "DECIMAL" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "OutputDt" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Frequency of writing the output in seconds. Default is 60.");
        cMap.setInheritance("false");
        cMap.setSize( 31 );
        cMap.setScale( 16 );
        cMap.setPosition(9);
        tMap.addColumn(cMap);
  // ------------- Column: execution_start_time --------------------
        cMap = new ColumnMap( "execution_start_time", tMap);
        cMap.setType( new Date() );
        cMap.setTorqueType( "TIMESTAMP" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "ExecutionStartTime" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(10);
        tMap.addColumn(cMap);
  // ------------- Column: execution_end_time --------------------
        cMap = new ColumnMap( "execution_end_time", tMap);
        cMap.setType( new Date() );
        cMap.setTorqueType( "TIMESTAMP" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(false);
        cMap.setJavaName( "ExecutionEndTime" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(11);
        tMap.addColumn(cMap);
  // ------------- Column: status --------------------
        cMap = new ColumnMap( "status", tMap);
        cMap.setType( new Integer(0) );
        cMap.setTorqueType( "INTEGER" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Status" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Status of the run execution.");
        cMap.setInheritance("false");
        cMap.setPosition(12);
        tMap.addColumn(cMap);
  // ------------- Column: created --------------------
        cMap = new ColumnMap( "created", tMap);
        cMap.setType( new Date() );
        cMap.setTorqueType( "DATE" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Created" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(13);
        tMap.addColumn(cMap);
  // ------------- Column: modified --------------------
        cMap = new ColumnMap( "modified", tMap);
        cMap.setType( new Date() );
        cMap.setTorqueType( "DATE" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Modified" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(14);
        tMap.addColumn(cMap);
  // ------------- Column: created_by --------------------
        cMap = new ColumnMap( "created_by", tMap);
        cMap.setType( "" );
        cMap.setTorqueType( "VARCHAR" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "CreatedBy" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setSize( 64 );
        cMap.setPosition(15);
        tMap.addColumn(cMap);
  // ------------- Column: modified_by --------------------
        cMap = new ColumnMap( "modified_by", tMap);
        cMap.setType( "" );
        cMap.setTorqueType( "VARCHAR" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "ModifiedBy" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setSize( 64 );
        cMap.setPosition(16);
        tMap.addColumn(cMap);
  // ------------- Column: modstamp --------------------
        cMap = new ColumnMap( "modstamp", tMap);
        cMap.setType( new Date() );
        cMap.setTorqueType( "TIMESTAMP" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(true);
        cMap.setJavaName( "Modstamp" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setPosition(17);
        tMap.addColumn(cMap);
        tMap.setUseInheritance(false);
    }
}
