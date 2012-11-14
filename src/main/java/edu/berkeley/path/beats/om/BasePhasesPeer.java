package com.relteq.sirius.om;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.TorqueRuntimeException;
import org.apache.torque.map.MapBuilder;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.DateKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.StringKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.QueryDataSet;
import com.workingdogs.village.Record;

// Local classes
import com.relteq.sirius.om.map.*;




/**
 * This class was autogenerated by Torque on:
 *
 * [Wed Nov 14 14:25:09 PST 2012]
 *
 */
public abstract class BasePhasesPeer
    extends com.relteq.sirius.db.BasePeer
{
    /** Serial version */
    private static final long serialVersionUID = 1352931909099L;


    /** the default database name for this class */
    public static final String DATABASE_NAME;

     /** the table name for this class */
    public static final String TABLE_NAME;

    /**
     * @return the map builder for this peer
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     * @deprecated Torque.getMapBuilder(PhasesMapBuilder.CLASS_NAME) instead
     */
    public static MapBuilder getMapBuilder()
        throws TorqueException
    {
        return Torque.getMapBuilder(PhasesMapBuilder.CLASS_NAME);
    }

    /** the column name for the signal_id field */
    public static final String SIGNAL_ID;
    /** the column name for the nema field */
    public static final String NEMA;
    /** the column name for the protected field */
    public static final String PROTECTED;
    /** the column name for the permissive field */
    public static final String PERMISSIVE;
    /** the column name for the lag field */
    public static final String LAG;
    /** the column name for the recall field */
    public static final String RECALL;
    /** the column name for the min_green_time field */
    public static final String MIN_GREEN_TIME;
    /** the column name for the yellow_time field */
    public static final String YELLOW_TIME;
    /** the column name for the red_clear_time field */
    public static final String RED_CLEAR_TIME;
    /** the column name for the created field */
    public static final String CREATED;
    /** the column name for the modified field */
    public static final String MODIFIED;
    /** the column name for the created_by field */
    public static final String CREATED_BY;
    /** the column name for the modified_by field */
    public static final String MODIFIED_BY;
    /** the column name for the modstamp field */
    public static final String MODSTAMP;

    static
    {
        DATABASE_NAME = "sirius";
        TABLE_NAME = "phases";

        SIGNAL_ID = "phases.signal_id";
        NEMA = "phases.nema";
        PROTECTED = "phases.protected";
        PERMISSIVE = "phases.permissive";
        LAG = "phases.lag";
        RECALL = "phases.recall";
        MIN_GREEN_TIME = "phases.min_green_time";
        YELLOW_TIME = "phases.yellow_time";
        RED_CLEAR_TIME = "phases.red_clear_time";
        CREATED = "phases.created";
        MODIFIED = "phases.modified";
        CREATED_BY = "phases.created_by";
        MODIFIED_BY = "phases.modified_by";
        MODSTAMP = "phases.modstamp";
        if (Torque.isInit())
        {
            try
            {
                Torque.getMapBuilder(PhasesMapBuilder.CLASS_NAME);
            }
            catch (TorqueException e)
            {
                log.error("Could not initialize Peer", e);
                throw new TorqueRuntimeException(e);
            }
        }
        else
        {
            Torque.registerMapBuilder(PhasesMapBuilder.CLASS_NAME);
        }
    }
 
    /** number of columns for this peer */
    public static final int numColumns =  14;

    /** A class that can be returned by this peer. */
    protected static final String CLASSNAME_DEFAULT =
        "com.relteq.sirius.om.Phases";

    /** A class that can be returned by this peer. */
    protected static final Class CLASS_DEFAULT = initClass(CLASSNAME_DEFAULT);

    /**
     * Class object initialization method.
     *
     * @param className name of the class to initialize
     * @return the initialized class
     */
    private static Class initClass(String className)
    {
        Class c = null;
        try
        {
            c = Class.forName(className);
        }
        catch (Throwable t)
        {
            log.error("A FATAL ERROR has occurred which should not "
                + "have happened under any circumstance.  Please notify "
                + "the Torque developers <torque-dev@db.apache.org> "
                + "and give as many details as possible (including the error "
                + "stack trace).", t);

            // Error objects should always be propagated.
            if (t instanceof Error)
            {
                throw (Error) t.fillInStackTrace();
            }
        }
        return c;
    }

    /**
     * Get the list of objects for a ResultSet.  Please not that your
     * resultset MUST return columns in the right order.  You can use
     * getFieldNames() in BaseObject to get the correct sequence.
     *
     * @param results the ResultSet
     * @return the list of objects
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List resultSet2Objects(java.sql.ResultSet results)
            throws TorqueException
    {
        try
        {
            QueryDataSet qds = null;
            List rows = null;
            try
            {
                qds = new QueryDataSet(results);
                rows = getSelectResults(qds);
            }
            finally
            {
                if (qds != null)
                {
                    qds.close();
                }
            }

            return populateObjects(rows);
        }
        catch (SQLException e)
        {
            throw new TorqueException(e);
        }
        catch (DataSetException e)
        {
            throw new TorqueException(e);
        }
    }



    /**
     * Method to do inserts.
     *
     * @param criteria object used to create the INSERT statement.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static ObjectKey doInsert(Criteria criteria)
        throws TorqueException
    {
        return BasePhasesPeer
            .doInsert(criteria, (Connection) null);
    }

    /**
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(Criteria) method.  It will take care of
     * the connection details internally.
     *
     * @param criteria object used to create the INSERT statement.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static ObjectKey doInsert(Criteria criteria, Connection con)
        throws TorqueException
    {
        correctBooleans(criteria);

        setDbName(criteria);

        if (con == null)
        {
            return BasePeer.doInsert(criteria);
        }
        else
        {
            return BasePeer.doInsert(criteria, con);
        }
    }

    /**
     * Add all the columns needed to create a new object.
     *
     * @param criteria object containing the columns to add.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void addSelectColumns(Criteria criteria)
            throws TorqueException
    {
        criteria.addSelectColumn(SIGNAL_ID);
        criteria.addSelectColumn(NEMA);
        criteria.addSelectColumn(PROTECTED);
        criteria.addSelectColumn(PERMISSIVE);
        criteria.addSelectColumn(LAG);
        criteria.addSelectColumn(RECALL);
        criteria.addSelectColumn(MIN_GREEN_TIME);
        criteria.addSelectColumn(YELLOW_TIME);
        criteria.addSelectColumn(RED_CLEAR_TIME);
        criteria.addSelectColumn(CREATED);
        criteria.addSelectColumn(MODIFIED);
        criteria.addSelectColumn(CREATED_BY);
        criteria.addSelectColumn(MODIFIED_BY);
        criteria.addSelectColumn(MODSTAMP);
    }

    /**
     * changes the boolean values in the criteria to the appropriate type,
     * whenever a booleanchar or booleanint column is involved.
     * This enables the user to create criteria using Boolean values
     * for booleanchar or booleanint columns
     * @param criteria the criteria in which the boolean values should be corrected
     * @throws TorqueException if the database map for the criteria cannot be 
               obtained.
     */
    public static void correctBooleans(Criteria criteria) throws TorqueException
    {
        correctBooleans(criteria, getTableMap());
    }

    /**
     * Create a new object of type cls from a resultset row starting
     * from a specified offset.  This is done so that you can select
     * other rows than just those needed for this object.  You may
     * for example want to create two objects from the same row.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static Phases row2Object(Record row,
                                             int offset,
                                             Class cls)
        throws TorqueException
    {
        try
        {
            Phases obj = (Phases) cls.newInstance();
            PhasesPeer.populateObject(row, offset, obj);
                obj.setModified(false);
            obj.setNew(false);

            return obj;
        }
        catch (InstantiationException e)
        {
            throw new TorqueException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new TorqueException(e);
        }
    }

    /**
     * Populates an object from a resultset row starting
     * from a specified offset.  This is done so that you can select
     * other rows than just those needed for this object.  You may
     * for example want to create two objects from the same row.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void populateObject(Record row,
                                      int offset,
                                      Phases obj)
        throws TorqueException
    {
        try
        {
            obj.setSignalId(row.getValue(offset + 0).asLongObj());
            obj.setNema(row.getValue(offset + 1).asIntegerObj());
            obj.setIsProtected(row.getValue(offset + 2).asBooleanObj());
            obj.setPermissive(row.getValue(offset + 3).asBooleanObj());
            obj.setLag(row.getValue(offset + 4).asBooleanObj());
            obj.setRecall(row.getValue(offset + 5).asBooleanObj());
            obj.setMinGreenTime(row.getValue(offset + 6).asBigDecimal());
            obj.setYellowTime(row.getValue(offset + 7).asBigDecimal());
            obj.setRedClearTime(row.getValue(offset + 8).asBigDecimal());
            obj.setCreated(row.getValue(offset + 9).asUtilDate());
            obj.setModified(row.getValue(offset + 10).asUtilDate());
            obj.setCreatedBy(row.getValue(offset + 11).asString());
            obj.setModifiedBy(row.getValue(offset + 12).asString());
            obj.setModstamp(row.getValue(offset + 13).asUtilDate());
        }
        catch (DataSetException e)
        {
            throw new TorqueException(e);
        }
    }

    /**
     * Method to do selects.
     *
     * @param criteria object used to create the SELECT statement.
     * @return List of selected Objects
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List doSelect(Criteria criteria) throws TorqueException
    {
        return populateObjects(doSelectVillageRecords(criteria));
    }

    /**
     * Method to do selects within a transaction.
     *
     * @param criteria object used to create the SELECT statement.
     * @param con the connection to use
     * @return List of selected Objects
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List doSelect(Criteria criteria, Connection con)
        throws TorqueException
    {
        return populateObjects(doSelectVillageRecords(criteria, con));
    }

    /**
     * Grabs the raw Village records to be formed into objects.
     * This method handles connections internally.  The Record objects
     * returned by this method should be considered readonly.  Do not
     * alter the data and call save(), your results may vary, but are
     * certainly likely to result in hard to track MT bugs.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List doSelectVillageRecords(Criteria criteria)
        throws TorqueException
    {
        return BasePhasesPeer
            .doSelectVillageRecords(criteria, (Connection) null);
    }

    /**
     * Grabs the raw Village records to be formed into objects.
     * This method should be used for transactions
     *
     * @param criteria object used to create the SELECT statement.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List doSelectVillageRecords(Criteria criteria, Connection con)
        throws TorqueException
    {
        if (criteria.getSelectColumns().size() == 0)
        {
            addSelectColumns(criteria);
        }
        correctBooleans(criteria);

        setDbName(criteria);

        // BasePeer returns a List of Value (Village) arrays.  The array
        // order follows the order columns were placed in the Select clause.
        if (con == null)
        {
            return BasePeer.doSelect(criteria);
        }
        else
        {
            return BasePeer.doSelect(criteria, con);
        }
    }

    /**
     * The returned List will contain objects of the default type or
     * objects that inherit from the default.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List populateObjects(List records)
        throws TorqueException
    {
        List results = new ArrayList(records.size());

        // populate the object(s)
        for (int i = 0; i < records.size(); i++)
        {
            Record row = (Record) records.get(i);
            results.add(PhasesPeer.row2Object(row, 1,
                PhasesPeer.getOMClass()));
        }
        return results;
    }
 

    /**
     * The class that the Peer will make instances of.
     * If the BO is abstract then you must implement this method
     * in the BO.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static Class getOMClass()
        throws TorqueException
    {
        return CLASS_DEFAULT;
    }

    /**
     * Method to do updates.
     *
     * @param criteria object containing data that is used to create the UPDATE
     *        statement.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doUpdate(Criteria criteria) throws TorqueException
    {
         BasePhasesPeer
            .doUpdate(criteria, (Connection) null);
    }

    /**
     * Method to do updates.  This method is to be used during a transaction,
     * otherwise use the doUpdate(Criteria) method.  It will take care of
     * the connection details internally.
     *
     * @param criteria object containing data that is used to create the UPDATE
     *        statement.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doUpdate(Criteria criteria, Connection con)
        throws TorqueException
    {
        Criteria selectCriteria = new Criteria(DATABASE_NAME, 2);
        correctBooleans(criteria);


         selectCriteria.put(SIGNAL_ID, criteria.remove(SIGNAL_ID));

         selectCriteria.put(NEMA, criteria.remove(NEMA));













        setDbName(criteria);

        if (con == null)
        {
            BasePeer.doUpdate(selectCriteria, criteria);
        }
        else
        {
            BasePeer.doUpdate(selectCriteria, criteria, con);
        }
    }

    /**
     * Method to do deletes.
     *
     * @param criteria object containing data that is used DELETE from database.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
     public static void doDelete(Criteria criteria) throws TorqueException
     {
         PhasesPeer
            .doDelete(criteria, (Connection) null);
     }

    /**
     * Method to do deletes.  This method is to be used during a transaction,
     * otherwise use the doDelete(Criteria) method.  It will take care of
     * the connection details internally.
     *
     * @param criteria object containing data that is used DELETE from database.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
     public static void doDelete(Criteria criteria, Connection con)
        throws TorqueException
     {
        correctBooleans(criteria);

        setDbName(criteria);

        if (con == null)
        {
            BasePeer.doDelete(criteria, TABLE_NAME);
        }
        else
        {
            BasePeer.doDelete(criteria, TABLE_NAME, con);
        }
     }

    /**
     * Method to do selects
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List doSelect(Phases obj) throws TorqueException
    {
        return doSelect(buildSelectCriteria(obj));
    }

    /**
     * Method to do inserts
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doInsert(Phases obj) throws TorqueException
    {
        doInsert(buildCriteria(obj));
        obj.setNew(false);
        obj.setModified(false);
    }

    /**
     * @param obj the data object to update in the database.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doUpdate(Phases obj) throws TorqueException
    {
        doUpdate(buildCriteria(obj));
        obj.setModified(false);
    }

    /**
     * @param obj the data object to delete in the database.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doDelete(Phases obj) throws TorqueException
    {
        doDelete(buildSelectCriteria(obj));
    }

    /**
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(Phases) method.  It will take
     * care of the connection details internally.
     *
     * @param obj the data object to insert into the database.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doInsert(Phases obj, Connection con)
        throws TorqueException
    {
        doInsert(buildCriteria(obj), con);
        obj.setNew(false);
        obj.setModified(false);
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(Phases) method.  It will take
     * care of the connection details internally.
     *
     * @param obj the data object to update in the database.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doUpdate(Phases obj, Connection con)
        throws TorqueException
    {
        doUpdate(buildCriteria(obj), con);
        obj.setModified(false);
    }

    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(Phases) method.  It will take
     * care of the connection details internally.
     *
     * @param obj the data object to delete in the database.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doDelete(Phases obj, Connection con)
        throws TorqueException
    {
        doDelete(buildSelectCriteria(obj), con);
    }

    /**
     * Method to do deletes.
     *
     * @param pk ObjectKey that is used DELETE from database.
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doDelete(ObjectKey pk) throws TorqueException
    {
        BasePhasesPeer
           .doDelete(pk, (Connection) null);
    }

    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ObjectKey) method.  It will take
     * care of the connection details internally.
     *
     * @param pk the primary key for the object to delete in the database.
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static void doDelete(ObjectKey pk, Connection con)
        throws TorqueException
    {
        doDelete(buildCriteria(pk), con);
    }

    /** Build a Criteria object from an ObjectKey */
    public static Criteria buildCriteria( ObjectKey pk )
    {
        Criteria criteria = new Criteria();
        SimpleKey[] keys = (SimpleKey[])pk.getValue();
            criteria.add(SIGNAL_ID, keys[0]);
            criteria.add(NEMA, keys[1]);
        return criteria;
     }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( Phases obj )
    {
        Criteria criteria = new Criteria(DATABASE_NAME);
        criteria.add(SIGNAL_ID, obj.getSignalId());
        criteria.add(NEMA, obj.getNema());
        criteria.add(PROTECTED, obj.getIsProtected());
        criteria.add(PERMISSIVE, obj.getPermissive());
        criteria.add(LAG, obj.getLag());
        criteria.add(RECALL, obj.getRecall());
        criteria.add(MIN_GREEN_TIME, obj.getMinGreenTime());
        criteria.add(YELLOW_TIME, obj.getYellowTime());
        criteria.add(RED_CLEAR_TIME, obj.getRedClearTime());
        criteria.add(CREATED, obj.getCreated());
        criteria.add(MODIFIED, obj.getModified());
        criteria.add(CREATED_BY, obj.getCreatedBy());
        criteria.add(MODIFIED_BY, obj.getModifiedBy());
        criteria.add(MODSTAMP, obj.getModstamp());
        return criteria;
    }

    /** Build a Criteria object from the data object for this peer, skipping all binary columns */
    public static Criteria buildSelectCriteria( Phases obj )
    {
        Criteria criteria = new Criteria(DATABASE_NAME);
            criteria.add(SIGNAL_ID, obj.getSignalId());
            criteria.add(NEMA, obj.getNema());
            criteria.add(PROTECTED, obj.getIsProtected());
            criteria.add(PERMISSIVE, obj.getPermissive());
            criteria.add(LAG, obj.getLag());
            criteria.add(RECALL, obj.getRecall());
            criteria.add(MIN_GREEN_TIME, obj.getMinGreenTime());
            criteria.add(YELLOW_TIME, obj.getYellowTime());
            criteria.add(RED_CLEAR_TIME, obj.getRedClearTime());
            criteria.add(CREATED, obj.getCreated());
            criteria.add(MODIFIED, obj.getModified());
            criteria.add(CREATED_BY, obj.getCreatedBy());
            criteria.add(MODIFIED_BY, obj.getModifiedBy());
            criteria.add(MODSTAMP, obj.getModstamp());
        return criteria;
    }
 


    /**
     * Retrieve a single object by pk
     *
     * @param pk the primary key
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     * @throws NoRowsException Primary key was not found in database.
     * @throws TooManyRowsException Primary key was not found in database.
     */
    public static Phases retrieveByPK(ObjectKey pk)
        throws TorqueException, NoRowsException, TooManyRowsException
    {
        Connection db = null;
        Phases retVal = null;
        try
        {
            db = Torque.getConnection(DATABASE_NAME);
            retVal = retrieveByPK(pk, db);
        }
        finally
        {
            Torque.closeConnection(db);
        }
        return retVal;
    }

    /**
     * Retrieve a single object by pk
     *
     * @param pk the primary key
     * @param con the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     * @throws NoRowsException Primary key was not found in database.
     * @throws TooManyRowsException Primary key was not found in database.
     */
    public static Phases retrieveByPK(ObjectKey pk, Connection con)
        throws TorqueException, NoRowsException, TooManyRowsException
    {
        Criteria criteria = buildCriteria(pk);
        List v = doSelect(criteria, con);
        if (v.size() == 0)
        {
            throw new NoRowsException("Failed to select a row.");
        }
        else if (v.size() > 1)
        {
            throw new TooManyRowsException("Failed to select only one row.");
        }
        else
        {
            return (Phases)v.get(0);
        }
    }

    /**
     * Retrieve a multiple objects by pk
     *
     * @param pks List of primary keys
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List retrieveByPKs(List pks)
        throws TorqueException
    {
        Connection db = null;
        List retVal = null;
        try
        {
           db = Torque.getConnection(DATABASE_NAME);
           retVal = retrieveByPKs(pks, db);
        }
        finally
        {
            Torque.closeConnection(db);
        }
        return retVal;
    }

    /**
     * Retrieve a multiple objects by pk
     *
     * @param pks List of primary keys
     * @param dbcon the connection to use
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static List retrieveByPKs( List pks, Connection dbcon )
        throws TorqueException
    {
        List objs = null;
        if (pks == null || pks.size() == 0)
        {
            objs = new LinkedList();
        }
        else
        {
            Criteria criteria = new Criteria();
            Iterator iter = pks.iterator();
            while (iter.hasNext())
            {
                ObjectKey pk = (ObjectKey) iter.next();
                SimpleKey[] keys = (SimpleKey[])pk.getValue();
                    Criteria.Criterion c0 = criteria.getNewCriterion(
                        SIGNAL_ID, keys[0], Criteria.EQUAL);
                    Criteria.Criterion c1 = criteria.getNewCriterion(
                        NEMA, keys[1], Criteria.EQUAL);
                        c0.and(c1);
                criteria.or(c0);
            }
        objs = doSelect(criteria, dbcon);
        }
        return objs;
    }

 
    /**
     * retrieve object using using pk values.
     *
     * @param signalId Long
     * @param nema Integer
     */
    public static Phases retrieveByPK(
 Long signalId
, Integer nema
        ) throws TorqueException
    {
        Connection db = null;
        Phases retVal = null;
        try
        {
           db = Torque.getConnection(DATABASE_NAME);
           retVal = retrieveByPK(
 signalId
, nema
               , db);
        }
        finally
        {
            Torque.closeConnection(db);
        }
        return retVal;
    }

    /**
     * retrieve object using using pk values.
     *
     * @param signalId Long
     * @param nema Integer
     * @param con Connection
     */
    public static Phases retrieveByPK(
 Long signalId
, Integer nema
       ,Connection con) throws TorqueException
    {

        Criteria criteria = new Criteria(5);
        criteria.add(SIGNAL_ID, signalId);
        criteria.add(NEMA, nema);
        List v = doSelect(criteria, con);
        if (v.size() == 1)
        {
            return (Phases) v.get(0);
        }
        else
        {
            throw new TorqueException("Failed to select one and only one row.");
        }
    }








    /**
     * selects a collection of Phases objects pre-filled with their
     * Signals objects.
     *
     * This method is protected by default in order to keep the public
     * api reasonable.  You can provide public methods for those you
     * actually need in PhasesPeer.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    protected static List doSelectJoinSignals(Criteria criteria)
        throws TorqueException
    {
        return doSelectJoinSignals(criteria, null);
    }

    /**
     * selects a collection of Phases objects pre-filled with their
     * Signals objects.
     *
     * This method is protected by default in order to keep the public
     * api reasonable.  You can provide public methods for those you
     * actually need in PhasesPeer.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    protected static List doSelectJoinSignals(Criteria criteria, Connection conn)
        throws TorqueException
    {
        setDbName(criteria);

        PhasesPeer.addSelectColumns(criteria);
        int offset = numColumns + 1;
        SignalsPeer.addSelectColumns(criteria);

        criteria.addJoin(PhasesPeer.SIGNAL_ID,
            SignalsPeer.ID);

        correctBooleans(criteria);

        List rows;
        if (conn == null)
        {
            rows = BasePeer.doSelect(criteria);
        }
        else
        {
            rows = BasePeer.doSelect(criteria,conn);
        }

        List results = new ArrayList();

        for (int i = 0; i < rows.size(); i++)
        {
            Record row = (Record) rows.get(i);

            Class omClass = PhasesPeer.getOMClass();
            Phases obj1 = (Phases) PhasesPeer
                .row2Object(row, 1, omClass);
             omClass = SignalsPeer.getOMClass();
            Signals obj2 = (Signals) SignalsPeer
                .row2Object(row, offset, omClass);

            boolean newObject = true;
            for (int j = 0; j < results.size(); j++)
            {
                Phases temp_obj1 = (Phases) results.get(j);
                Signals temp_obj2 = (Signals) temp_obj1.getSignals();
                if (temp_obj2.getPrimaryKey().equals(obj2.getPrimaryKey()))
                {
                    newObject = false;
                    temp_obj2.addPhases(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initPhasess();
                obj2.addPhases(obj1);
            }
            results.add(obj1);
        }
        return results;
    }




    /**
     * Returns the TableMap related to this peer.
     *
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
    public static TableMap getTableMap()
        throws TorqueException
    {
        return Torque.getDatabaseMap(DATABASE_NAME).getTable(TABLE_NAME);
    }
 
    private static void setDbName(Criteria crit)
    {
        // Set the correct dbName if it has not been overridden
        // crit.getDbName will return the same object if not set to
        // another value so == check is okay and faster
        if (crit.getDbName() == Torque.getDefaultDB())
        {
            crit.setDbName(DATABASE_NAME);
        }
    }
    

}
