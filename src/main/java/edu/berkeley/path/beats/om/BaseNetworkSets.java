package com.relteq.sirius.om;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.torque.TorqueException;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.DateKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.om.StringKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;





/**
 * This class was autogenerated by Torque on:
 *
 * [Wed Nov 14 14:25:09 PST 2012]
 *
 * You should not use this class directly.  It should not even be
 * extended all references should be to NetworkSets
 */
public abstract class BaseNetworkSets extends com.relteq.sirius.db.BaseObject
{
    /** Serial version */
    private static final long serialVersionUID = 1352931909099L;

    /** The Peer class */
    private static final NetworkSetsPeer peer =
        new NetworkSetsPeer();


    /** The value for the scenarioId field */
    private Long scenarioId;

    /** The value for the networkId field */
    private Long networkId;

    /** The value for the created field */
    private Date created;

    /** The value for the modified field */
    private Date modified;

    /** The value for the createdBy field */
    private String createdBy;

    /** The value for the modifiedBy field */
    private String modifiedBy;

    /** The value for the modstamp field */
    private Date modstamp;


    /**
     * Get the ScenarioId
     *
     * @return Long
     */
    public Long getScenarioId()
    {
        return scenarioId;
    }


    /**
     * Set the value of ScenarioId
     *
     * @param v new value
     */
    public void setScenarioId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.scenarioId, v))
        {
            this.scenarioId = v;
            setModified(true);
        }


        if (aScenarios != null && !ObjectUtils.equals(aScenarios.getId(), v))
        {
            aScenarios = null;
        }

    }

    /**
     * Get the NetworkId
     *
     * @return Long
     */
    public Long getNetworkId()
    {
        return networkId;
    }


    /**
     * Set the value of NetworkId
     *
     * @param v new value
     */
    public void setNetworkId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.networkId, v))
        {
            this.networkId = v;
            setModified(true);
        }


        if (aNetworks != null && !ObjectUtils.equals(aNetworks.getId(), v))
        {
            aNetworks = null;
        }

    }

    /**
     * Get the Created
     *
     * @return Date
     */
    public Date getCreated()
    {
        return created;
    }


    /**
     * Set the value of Created
     *
     * @param v new value
     */
    public void setCreated(Date v) 
    {

        if (!ObjectUtils.equals(this.created, v))
        {
            this.created = v;
            setModified(true);
        }


    }

    /**
     * Get the Modified
     *
     * @return Date
     */
    public Date getModified()
    {
        return modified;
    }


    /**
     * Set the value of Modified
     *
     * @param v new value
     */
    public void setModified(Date v) 
    {

        if (!ObjectUtils.equals(this.modified, v))
        {
            this.modified = v;
            setModified(true);
        }


    }

    /**
     * Get the CreatedBy
     *
     * @return String
     */
    public String getCreatedBy()
    {
        return createdBy;
    }


    /**
     * Set the value of CreatedBy
     *
     * @param v new value
     */
    public void setCreatedBy(String v) 
    {

        if (!ObjectUtils.equals(this.createdBy, v))
        {
            this.createdBy = v;
            setModified(true);
        }


    }

    /**
     * Get the ModifiedBy
     *
     * @return String
     */
    public String getModifiedBy()
    {
        return modifiedBy;
    }


    /**
     * Set the value of ModifiedBy
     *
     * @param v new value
     */
    public void setModifiedBy(String v) 
    {

        if (!ObjectUtils.equals(this.modifiedBy, v))
        {
            this.modifiedBy = v;
            setModified(true);
        }


    }

    /**
     * Get the Modstamp
     *
     * @return Date
     */
    public Date getModstamp()
    {
        return modstamp;
    }


    /**
     * Set the value of Modstamp
     *
     * @param v new value
     */
    public void setModstamp(Date v) 
    {

        if (!ObjectUtils.equals(this.modstamp, v))
        {
            this.modstamp = v;
            setModified(true);
        }


    }

    



    private Scenarios aScenarios;

    /**
     * Declares an association between this object and a Scenarios object
     *
     * @param v Scenarios
     * @throws TorqueException
     */
    public void setScenarios(Scenarios v) throws TorqueException
    {
        if (v == null)
        {
            setScenarioId((Long) null);
        }
        else
        {
            setScenarioId(v.getId());
        }
        aScenarios = v;
    }


    /**
     * Returns the associated Scenarios object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated Scenarios object
     * @throws TorqueException
     */
    public Scenarios getScenarios()
        throws TorqueException
    {
        if (aScenarios == null && (!ObjectUtils.equals(this.scenarioId, null)))
        {
            aScenarios = ScenariosPeer.retrieveByPK(SimpleKey.keyFor(this.scenarioId));
        }
        return aScenarios;
    }

    /**
     * Return the associated Scenarios object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated Scenarios object
     * @throws TorqueException
     */
    public Scenarios getScenarios(Connection connection)
        throws TorqueException
    {
        if (aScenarios == null && (!ObjectUtils.equals(this.scenarioId, null)))
        {
            aScenarios = ScenariosPeer.retrieveByPK(SimpleKey.keyFor(this.scenarioId), connection);
        }
        return aScenarios;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setScenariosKey(ObjectKey key) throws TorqueException
    {

        setScenarioId(new Long(((NumberKey) key).longValue()));
    }




    private Networks aNetworks;

    /**
     * Declares an association between this object and a Networks object
     *
     * @param v Networks
     * @throws TorqueException
     */
    public void setNetworks(Networks v) throws TorqueException
    {
        if (v == null)
        {
            setNetworkId((Long) null);
        }
        else
        {
            setNetworkId(v.getId());
        }
        aNetworks = v;
    }


    /**
     * Returns the associated Networks object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated Networks object
     * @throws TorqueException
     */
    public Networks getNetworks()
        throws TorqueException
    {
        if (aNetworks == null && (!ObjectUtils.equals(this.networkId, null)))
        {
            aNetworks = NetworksPeer.retrieveByPK(SimpleKey.keyFor(this.networkId));
        }
        return aNetworks;
    }

    /**
     * Return the associated Networks object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated Networks object
     * @throws TorqueException
     */
    public Networks getNetworks(Connection connection)
        throws TorqueException
    {
        if (aNetworks == null && (!ObjectUtils.equals(this.networkId, null)))
        {
            aNetworks = NetworksPeer.retrieveByPK(SimpleKey.keyFor(this.networkId), connection);
        }
        return aNetworks;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setNetworksKey(ObjectKey key) throws TorqueException
    {

        setNetworkId(new Long(((NumberKey) key).longValue()));
    }
   
        
    private static List fieldNames = null;

    /**
     * Generate a list of field names.
     *
     * @return a list of field names
     */
    public static synchronized List getFieldNames()
    {
        if (fieldNames == null)
        {
            fieldNames = new ArrayList();
            fieldNames.add("ScenarioId");
            fieldNames.add("NetworkId");
            fieldNames.add("Created");
            fieldNames.add("Modified");
            fieldNames.add("CreatedBy");
            fieldNames.add("ModifiedBy");
            fieldNames.add("Modstamp");
            fieldNames = Collections.unmodifiableList(fieldNames);
        }
        return fieldNames;
    }

    /**
     * Retrieves a field from the object by field (Java) name passed in as a String.
     *
     * @param name field name
     * @return value
     */
    public Object getByName(String name)
    {
        if (name.equals("ScenarioId"))
        {
            return getScenarioId();
        }
        if (name.equals("NetworkId"))
        {
            return getNetworkId();
        }
        if (name.equals("Created"))
        {
            return getCreated();
        }
        if (name.equals("Modified"))
        {
            return getModified();
        }
        if (name.equals("CreatedBy"))
        {
            return getCreatedBy();
        }
        if (name.equals("ModifiedBy"))
        {
            return getModifiedBy();
        }
        if (name.equals("Modstamp"))
        {
            return getModstamp();
        }
        return null;
    }

    /**
     * Set a field in the object by field (Java) name.
     *
     * @param name field name
     * @param value field value
     * @return True if value was set, false if not (invalid name / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByName(String name, Object value )
        throws TorqueException, IllegalArgumentException
    {
        if (name.equals("ScenarioId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setScenarioId((Long) value);
            return true;
        }
        if (name.equals("NetworkId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setNetworkId((Long) value);
            return true;
        }
        if (name.equals("Created"))
        {
            // Object fields can be null
            if (value != null && ! Date.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setCreated((Date) value);
            return true;
        }
        if (name.equals("Modified"))
        {
            // Object fields can be null
            if (value != null && ! Date.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setModified((Date) value);
            return true;
        }
        if (name.equals("CreatedBy"))
        {
            // Object fields can be null
            if (value != null && ! String.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setCreatedBy((String) value);
            return true;
        }
        if (name.equals("ModifiedBy"))
        {
            // Object fields can be null
            if (value != null && ! String.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setModifiedBy((String) value);
            return true;
        }
        if (name.equals("Modstamp"))
        {
            // Object fields can be null
            if (value != null && ! Date.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setModstamp((Date) value);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.  The String must be one of the static
     * Strings defined in this Class' Peer.
     *
     * @param name peer name
     * @return value
     */
    public Object getByPeerName(String name)
    {
        if (name.equals(NetworkSetsPeer.SCENARIO_ID))
        {
            return getScenarioId();
        }
        if (name.equals(NetworkSetsPeer.NETWORK_ID))
        {
            return getNetworkId();
        }
        if (name.equals(NetworkSetsPeer.CREATED))
        {
            return getCreated();
        }
        if (name.equals(NetworkSetsPeer.MODIFIED))
        {
            return getModified();
        }
        if (name.equals(NetworkSetsPeer.CREATED_BY))
        {
            return getCreatedBy();
        }
        if (name.equals(NetworkSetsPeer.MODIFIED_BY))
        {
            return getModifiedBy();
        }
        if (name.equals(NetworkSetsPeer.MODSTAMP))
        {
            return getModstamp();
        }
        return null;
    }

    /**
     * Set field values by Peer Field Name
     *
     * @param name field name
     * @param value field value
     * @return True if value was set, false if not (invalid name / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByPeerName(String name, Object value)
        throws TorqueException, IllegalArgumentException
    {
      if (NetworkSetsPeer.SCENARIO_ID.equals(name))
        {
            return setByName("ScenarioId", value);
        }
      if (NetworkSetsPeer.NETWORK_ID.equals(name))
        {
            return setByName("NetworkId", value);
        }
      if (NetworkSetsPeer.CREATED.equals(name))
        {
            return setByName("Created", value);
        }
      if (NetworkSetsPeer.MODIFIED.equals(name))
        {
            return setByName("Modified", value);
        }
      if (NetworkSetsPeer.CREATED_BY.equals(name))
        {
            return setByName("CreatedBy", value);
        }
      if (NetworkSetsPeer.MODIFIED_BY.equals(name))
        {
            return setByName("ModifiedBy", value);
        }
      if (NetworkSetsPeer.MODSTAMP.equals(name))
        {
            return setByName("Modstamp", value);
        }
        return false;
    }

    /**
     * Retrieves a field from the object by Position as specified
     * in the xml schema.  Zero-based.
     *
     * @param pos position in xml schema
     * @return value
     */
    public Object getByPosition(int pos)
    {
        if (pos == 0)
        {
            return getScenarioId();
        }
        if (pos == 1)
        {
            return getNetworkId();
        }
        if (pos == 2)
        {
            return getCreated();
        }
        if (pos == 3)
        {
            return getModified();
        }
        if (pos == 4)
        {
            return getCreatedBy();
        }
        if (pos == 5)
        {
            return getModifiedBy();
        }
        if (pos == 6)
        {
            return getModstamp();
        }
        return null;
    }

    /**
     * Set field values by its position (zero based) in the XML schema.
     *
     * @param position The field position
     * @param value field value
     * @return True if value was set, false if not (invalid position / protected field).
     * @throws IllegalArgumentException if object type of value does not match field object type.
     * @throws TorqueException If a problem occurs with the set[Field] method.
     */
    public boolean setByPosition(int position, Object value)
        throws TorqueException, IllegalArgumentException
    {
    if (position == 0)
        {
            return setByName("ScenarioId", value);
        }
    if (position == 1)
        {
            return setByName("NetworkId", value);
        }
    if (position == 2)
        {
            return setByName("Created", value);
        }
    if (position == 3)
        {
            return setByName("Modified", value);
        }
    if (position == 4)
        {
            return setByName("CreatedBy", value);
        }
    if (position == 5)
        {
            return setByName("ModifiedBy", value);
        }
    if (position == 6)
        {
            return setByName("Modstamp", value);
        }
        return false;
    }
     
    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     *
     * @throws Exception
     */
    public void save() throws Exception
    {
        save(NetworkSetsPeer.DATABASE_NAME);
    }

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     * Note: this code is here because the method body is
     * auto-generated conditionally and therefore needs to be
     * in this file instead of in the super class, BaseObject.
     *
     * @param dbName
     * @throws TorqueException
     */
    public void save(String dbName) throws TorqueException
    {
        Connection con = null;
        try
        {
            con = Transaction.begin(dbName);
            save(con);
            Transaction.commit(con);
        }
        catch(TorqueException e)
        {
            Transaction.safeRollback(con);
            throw e;
        }
    }

    /** flag to prevent endless save loop, if this object is referenced
        by another object which falls in this transaction. */
    private boolean alreadyInSave = false;
    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     *
     * @param con
     * @throws TorqueException
     */
    public void save(Connection con) throws TorqueException
    {
        if (!alreadyInSave)
        {
            alreadyInSave = true;



            // If this object has been modified, then save it to the database.
            if (isModified())
            {
                if (isNew())
                {
                    NetworkSetsPeer.doInsert((NetworkSets) this, con);
                    setNew(false);
                }
                else
                {
                    NetworkSetsPeer.doUpdate((NetworkSets) this, con);
                }
            }

            alreadyInSave = false;
        }
    }



    /**
     * returns an id that differentiates this object from others
     * of its class.
     */
    public ObjectKey getPrimaryKey()
    {
        return null;
    }
 

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     */
    public NetworkSets copy() throws TorqueException
    {
        return copy(true);
    }

    /**
     * Makes a copy of this object using connection.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     *
     * @param con the database connection to read associated objects.
     */
    public NetworkSets copy(Connection con) throws TorqueException
    {
        return copy(true, con);
    }

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * If the parameter deepcopy is true, it then fills all the
     * association collections and sets the related objects to
     * isNew=true.
     *
     * @param deepcopy whether to copy the associated objects.
     */
    public NetworkSets copy(boolean deepcopy) throws TorqueException
    {
        return copyInto(new NetworkSets(), deepcopy);
    }

    /**
     * Makes a copy of this object using connection.
     * It creates a new object filling in the simple attributes.
     * If the parameter deepcopy is true, it then fills all the
     * association collections and sets the related objects to
     * isNew=true.
     *
     * @param deepcopy whether to copy the associated objects.
     * @param con the database connection to read associated objects.
     */
    public NetworkSets copy(boolean deepcopy, Connection con) throws TorqueException
    {
        return copyInto(new NetworkSets(), deepcopy, con);
    }
  
    /**
     * Fills the copyObj with the contents of this object.
     * The associated objects are also copied and treated as new objects.
     *
     * @param copyObj the object to fill.
     */
    protected NetworkSets copyInto(NetworkSets copyObj) throws TorqueException
    {
        return copyInto(copyObj, true);
    }

  
    /**
     * Fills the copyObj with the contents of this object using connection.
     * The associated objects are also copied and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param con the database connection to read associated objects.
     */
    protected NetworkSets copyInto(NetworkSets copyObj, Connection con) throws TorqueException
    {
        return copyInto(copyObj, true, con);
    }
  
    /**
     * Fills the copyObj with the contents of this object.
     * If deepcopy is true, The associated objects are also copied
     * and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param deepcopy whether the associated objects should be copied.
     */
    protected NetworkSets copyInto(NetworkSets copyObj, boolean deepcopy) throws TorqueException
    {
        copyObj.setScenarioId(scenarioId);
        copyObj.setNetworkId(networkId);
        copyObj.setCreated(created);
        copyObj.setModified(modified);
        copyObj.setCreatedBy(createdBy);
        copyObj.setModifiedBy(modifiedBy);
        copyObj.setModstamp(modstamp);


        if (deepcopy)
        {
        }
        return copyObj;
    }
        
    
    /**
     * Fills the copyObj with the contents of this object using connection.
     * If deepcopy is true, The associated objects are also copied
     * and treated as new objects.
     *
     * @param copyObj the object to fill.
     * @param deepcopy whether the associated objects should be copied.
     * @param con the database connection to read associated objects.
     */
    protected NetworkSets copyInto(NetworkSets copyObj, boolean deepcopy, Connection con) throws TorqueException
    {
        copyObj.setScenarioId(scenarioId);
        copyObj.setNetworkId(networkId);
        copyObj.setCreated(created);
        copyObj.setModified(modified);
        copyObj.setCreatedBy(createdBy);
        copyObj.setModifiedBy(modifiedBy);
        copyObj.setModstamp(modstamp);


        if (deepcopy)
        {
        }
        return copyObj;
    }
    
    

    /**
     * returns a peer instance associated with this om.  Since Peer classes
     * are not to have any instance attributes, this method returns the
     * same instance for all member of this class. The method could therefore
     * be static, but this would prevent one from overriding the behavior.
     */
    public NetworkSetsPeer getPeer()
    {
        return peer;
    }

    /**
     * Retrieves the TableMap object related to this Table data without
     * compiler warnings of using getPeer().getTableMap().
     *
     * @return The associated TableMap object.
     */
    public TableMap getTableMap() throws TorqueException
    {
        return NetworkSetsPeer.getTableMap();
    }


    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("NetworkSets:\n");
        str.append("ScenarioId = ")
           .append(getScenarioId())
           .append("\n");
        str.append("NetworkId = ")
           .append(getNetworkId())
           .append("\n");
        str.append("Created = ")
           .append(getCreated())
           .append("\n");
        str.append("Modified = ")
           .append(getModified())
           .append("\n");
        str.append("CreatedBy = ")
           .append(getCreatedBy())
           .append("\n");
        str.append("ModifiedBy = ")
           .append(getModifiedBy())
           .append("\n");
        str.append("Modstamp = ")
           .append(getModstamp())
           .append("\n");
        return(str.toString());
    }
}
