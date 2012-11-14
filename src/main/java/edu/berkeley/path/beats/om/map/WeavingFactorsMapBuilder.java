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
public class WeavingFactorsMapBuilder implements MapBuilder
{
    /**
     * The name of this class
     */
    public static final String CLASS_NAME =
        "com.relteq.sirius.om.map.WeavingFactorsMapBuilder";

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

        dbMap.addTable("weaving_factors");
        TableMap tMap = dbMap.getTable("weaving_factors");
        tMap.setJavaName("WeavingFactors");
        tMap.setOMClass( com.relteq.sirius.om.WeavingFactors.class );
        tMap.setPeerClass( com.relteq.sirius.om.WeavingFactorsPeer.class );
        tMap.setPrimaryKeyMethod("none");

        ColumnMap cMap = null;


  // ------------- Column: weaving_factor_set_id --------------------
        cMap = new ColumnMap( "weaving_factor_set_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(true);
        cMap.setNotNull(false);
        cMap.setJavaName( "WeavingFactorSetId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("weaving_factor_sets", "id");
        cMap.setPosition(1);
        tMap.addColumn(cMap);
  // ------------- Column: in_link_id --------------------
        cMap = new ColumnMap( "in_link_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(true);
        cMap.setNotNull(false);
        cMap.setJavaName( "InLinkId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("link_families", "id");
        cMap.setPosition(2);
        tMap.addColumn(cMap);
  // ------------- Column: out_link_id --------------------
        cMap = new ColumnMap( "out_link_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(true);
        cMap.setNotNull(false);
        cMap.setJavaName( "OutLinkId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("link_families", "id");
        cMap.setPosition(3);
        tMap.addColumn(cMap);
  // ------------- Column: vehicle_type_id --------------------
        cMap = new ColumnMap( "vehicle_type_id", tMap);
        cMap.setType( new Long(0) );
        cMap.setTorqueType( "BIGINT" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(true);
        cMap.setNotNull(false);
        cMap.setJavaName( "VehicleTypeId" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setInheritance("false");
        cMap.setForeignKey("vehicle_types", "vehicle_type_id");
        cMap.setPosition(4);
        tMap.addColumn(cMap);
  // ------------- Column: factor --------------------
        cMap = new ColumnMap( "factor", tMap);
        cMap.setType( new BigDecimal((double) 0) );
        cMap.setTorqueType( "DECIMAL" );
        cMap.setUsePrimitive(false);
        cMap.setPrimaryKey(false);
        cMap.setNotNull(false);
        cMap.setJavaName( "Factor" );
        cMap.setAutoIncrement(false);
        cMap.setProtected(false);
        cMap.setDescription("Should be greater than 1 for downstream merge weaving, or less than -1 for upstream diverge weaving. 1 and -1 represent no weaving.");
        cMap.setInheritance("false");
        cMap.setSize( 31 );
        cMap.setScale( 16 );
        cMap.setPosition(5);
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
        cMap.setPosition(6);
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
        cMap.setPosition(7);
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
        cMap.setPosition(8);
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
        cMap.setPosition(9);
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
        cMap.setPosition(10);
        tMap.addColumn(cMap);
        tMap.setUseInheritance(false);
    }
}
