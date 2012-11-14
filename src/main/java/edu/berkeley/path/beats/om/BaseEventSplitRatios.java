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
 * extended all references should be to EventSplitRatios
 */
public abstract class BaseEventSplitRatios extends com.relteq.sirius.db.BaseObject
{
    /** Serial version */
    private static final long serialVersionUID = 1352931909099L;

    /** The Peer class */
    private static final EventSplitRatiosPeer peer =
        new EventSplitRatiosPeer();


    /** The value for the eventId field */
    private Long eventId;

    /** The value for the inLinkId field */
    private Long inLinkId;

    /** The value for the outLinkId field */
    private Long outLinkId;

    /** The value for the vehicleTypeId field */
    private Long vehicleTypeId;

    /** The value for the splitRatio field */
    private BigDecimal splitRatio;

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
     * Get the EventId
     *
     * @return Long
     */
    public Long getEventId()
    {
        return eventId;
    }


    /**
     * Set the value of EventId
     *
     * @param v new value
     */
    public void setEventId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.eventId, v))
        {
            this.eventId = v;
            setModified(true);
        }


        if (aEvents != null && !ObjectUtils.equals(aEvents.getId(), v))
        {
            aEvents = null;
        }

    }

    /**
     * Get the InLinkId
     *
     * @return Long
     */
    public Long getInLinkId()
    {
        return inLinkId;
    }


    /**
     * Set the value of InLinkId
     *
     * @param v new value
     */
    public void setInLinkId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.inLinkId, v))
        {
            this.inLinkId = v;
            setModified(true);
        }


        if (aLinkFamiliesRelatedByInLinkId != null && !ObjectUtils.equals(aLinkFamiliesRelatedByInLinkId.getId(), v))
        {
            aLinkFamiliesRelatedByInLinkId = null;
        }

    }

    /**
     * Get the OutLinkId
     *
     * @return Long
     */
    public Long getOutLinkId()
    {
        return outLinkId;
    }


    /**
     * Set the value of OutLinkId
     *
     * @param v new value
     */
    public void setOutLinkId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.outLinkId, v))
        {
            this.outLinkId = v;
            setModified(true);
        }


        if (aLinkFamiliesRelatedByOutLinkId != null && !ObjectUtils.equals(aLinkFamiliesRelatedByOutLinkId.getId(), v))
        {
            aLinkFamiliesRelatedByOutLinkId = null;
        }

    }

    /**
     * Get the VehicleTypeId
     *
     * @return Long
     */
    public Long getVehicleTypeId()
    {
        return vehicleTypeId;
    }


    /**
     * Set the value of VehicleTypeId
     *
     * @param v new value
     */
    public void setVehicleTypeId(Long v) throws TorqueException
    {

        if (!ObjectUtils.equals(this.vehicleTypeId, v))
        {
            this.vehicleTypeId = v;
            setModified(true);
        }


        if (aVehicleTypes != null && !ObjectUtils.equals(aVehicleTypes.getVehicleTypeId(), v))
        {
            aVehicleTypes = null;
        }

    }

    /**
     * Get the SplitRatio
     *
     * @return BigDecimal
     */
    public BigDecimal getSplitRatio()
    {
        return splitRatio;
    }


    /**
     * Set the value of SplitRatio
     *
     * @param v new value
     */
    public void setSplitRatio(BigDecimal v) 
    {

        if (!ObjectUtils.equals(this.splitRatio, v))
        {
            this.splitRatio = v;
            setModified(true);
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

    



    private Events aEvents;

    /**
     * Declares an association between this object and a Events object
     *
     * @param v Events
     * @throws TorqueException
     */
    public void setEvents(Events v) throws TorqueException
    {
        if (v == null)
        {
            setEventId((Long) null);
        }
        else
        {
            setEventId(v.getId());
        }
        aEvents = v;
    }


    /**
     * Returns the associated Events object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated Events object
     * @throws TorqueException
     */
    public Events getEvents()
        throws TorqueException
    {
        if (aEvents == null && (!ObjectUtils.equals(this.eventId, null)))
        {
            aEvents = EventsPeer.retrieveByPK(SimpleKey.keyFor(this.eventId));
        }
        return aEvents;
    }

    /**
     * Return the associated Events object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated Events object
     * @throws TorqueException
     */
    public Events getEvents(Connection connection)
        throws TorqueException
    {
        if (aEvents == null && (!ObjectUtils.equals(this.eventId, null)))
        {
            aEvents = EventsPeer.retrieveByPK(SimpleKey.keyFor(this.eventId), connection);
        }
        return aEvents;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setEventsKey(ObjectKey key) throws TorqueException
    {

        setEventId(new Long(((NumberKey) key).longValue()));
    }




    private LinkFamilies aLinkFamiliesRelatedByInLinkId;

    /**
     * Declares an association between this object and a LinkFamilies object
     *
     * @param v LinkFamilies
     * @throws TorqueException
     */
    public void setLinkFamiliesRelatedByInLinkId(LinkFamilies v) throws TorqueException
    {
        if (v == null)
        {
            setInLinkId((Long) null);
        }
        else
        {
            setInLinkId(v.getId());
        }
        aLinkFamiliesRelatedByInLinkId = v;
    }


    /**
     * Returns the associated LinkFamilies object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated LinkFamilies object
     * @throws TorqueException
     */
    public LinkFamilies getLinkFamiliesRelatedByInLinkId()
        throws TorqueException
    {
        if (aLinkFamiliesRelatedByInLinkId == null && (!ObjectUtils.equals(this.inLinkId, null)))
        {
            aLinkFamiliesRelatedByInLinkId = LinkFamiliesPeer.retrieveByPK(SimpleKey.keyFor(this.inLinkId));
        }
        return aLinkFamiliesRelatedByInLinkId;
    }

    /**
     * Return the associated LinkFamilies object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated LinkFamilies object
     * @throws TorqueException
     */
    public LinkFamilies getLinkFamiliesRelatedByInLinkId(Connection connection)
        throws TorqueException
    {
        if (aLinkFamiliesRelatedByInLinkId == null && (!ObjectUtils.equals(this.inLinkId, null)))
        {
            aLinkFamiliesRelatedByInLinkId = LinkFamiliesPeer.retrieveByPK(SimpleKey.keyFor(this.inLinkId), connection);
        }
        return aLinkFamiliesRelatedByInLinkId;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setLinkFamiliesRelatedByInLinkIdKey(ObjectKey key) throws TorqueException
    {

        setInLinkId(new Long(((NumberKey) key).longValue()));
    }




    private LinkFamilies aLinkFamiliesRelatedByOutLinkId;

    /**
     * Declares an association between this object and a LinkFamilies object
     *
     * @param v LinkFamilies
     * @throws TorqueException
     */
    public void setLinkFamiliesRelatedByOutLinkId(LinkFamilies v) throws TorqueException
    {
        if (v == null)
        {
            setOutLinkId((Long) null);
        }
        else
        {
            setOutLinkId(v.getId());
        }
        aLinkFamiliesRelatedByOutLinkId = v;
    }


    /**
     * Returns the associated LinkFamilies object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated LinkFamilies object
     * @throws TorqueException
     */
    public LinkFamilies getLinkFamiliesRelatedByOutLinkId()
        throws TorqueException
    {
        if (aLinkFamiliesRelatedByOutLinkId == null && (!ObjectUtils.equals(this.outLinkId, null)))
        {
            aLinkFamiliesRelatedByOutLinkId = LinkFamiliesPeer.retrieveByPK(SimpleKey.keyFor(this.outLinkId));
        }
        return aLinkFamiliesRelatedByOutLinkId;
    }

    /**
     * Return the associated LinkFamilies object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated LinkFamilies object
     * @throws TorqueException
     */
    public LinkFamilies getLinkFamiliesRelatedByOutLinkId(Connection connection)
        throws TorqueException
    {
        if (aLinkFamiliesRelatedByOutLinkId == null && (!ObjectUtils.equals(this.outLinkId, null)))
        {
            aLinkFamiliesRelatedByOutLinkId = LinkFamiliesPeer.retrieveByPK(SimpleKey.keyFor(this.outLinkId), connection);
        }
        return aLinkFamiliesRelatedByOutLinkId;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setLinkFamiliesRelatedByOutLinkIdKey(ObjectKey key) throws TorqueException
    {

        setOutLinkId(new Long(((NumberKey) key).longValue()));
    }




    private VehicleTypes aVehicleTypes;

    /**
     * Declares an association between this object and a VehicleTypes object
     *
     * @param v VehicleTypes
     * @throws TorqueException
     */
    public void setVehicleTypes(VehicleTypes v) throws TorqueException
    {
        if (v == null)
        {
            setVehicleTypeId((Long) null);
        }
        else
        {
            setVehicleTypeId(v.getVehicleTypeId());
        }
        aVehicleTypes = v;
    }


    /**
     * Returns the associated VehicleTypes object.
     * If it was not retrieved before, the object is retrieved from
     * the database
     *
     * @return the associated VehicleTypes object
     * @throws TorqueException
     */
    public VehicleTypes getVehicleTypes()
        throws TorqueException
    {
        if (aVehicleTypes == null && (!ObjectUtils.equals(this.vehicleTypeId, null)))
        {
            aVehicleTypes = VehicleTypesPeer.retrieveByPK(SimpleKey.keyFor(this.vehicleTypeId));
        }
        return aVehicleTypes;
    }

    /**
     * Return the associated VehicleTypes object
     * If it was not retrieved before, the object is retrieved from
     * the database using the passed connection
     *
     * @param connection the connection used to retrieve the associated object
     *        from the database, if it was not retrieved before
     * @return the associated VehicleTypes object
     * @throws TorqueException
     */
    public VehicleTypes getVehicleTypes(Connection connection)
        throws TorqueException
    {
        if (aVehicleTypes == null && (!ObjectUtils.equals(this.vehicleTypeId, null)))
        {
            aVehicleTypes = VehicleTypesPeer.retrieveByPK(SimpleKey.keyFor(this.vehicleTypeId), connection);
        }
        return aVehicleTypes;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey, for example
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
     */
    public void setVehicleTypesKey(ObjectKey key) throws TorqueException
    {

        setVehicleTypeId(new Long(((NumberKey) key).longValue()));
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
            fieldNames.add("EventId");
            fieldNames.add("InLinkId");
            fieldNames.add("OutLinkId");
            fieldNames.add("VehicleTypeId");
            fieldNames.add("SplitRatio");
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
        if (name.equals("EventId"))
        {
            return getEventId();
        }
        if (name.equals("InLinkId"))
        {
            return getInLinkId();
        }
        if (name.equals("OutLinkId"))
        {
            return getOutLinkId();
        }
        if (name.equals("VehicleTypeId"))
        {
            return getVehicleTypeId();
        }
        if (name.equals("SplitRatio"))
        {
            return getSplitRatio();
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
        if (name.equals("EventId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setEventId((Long) value);
            return true;
        }
        if (name.equals("InLinkId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setInLinkId((Long) value);
            return true;
        }
        if (name.equals("OutLinkId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setOutLinkId((Long) value);
            return true;
        }
        if (name.equals("VehicleTypeId"))
        {
            // Object fields can be null
            if (value != null && ! Long.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setVehicleTypeId((Long) value);
            return true;
        }
        if (name.equals("SplitRatio"))
        {
            // Object fields can be null
            if (value != null && ! BigDecimal.class.isInstance(value))
            {
                throw new IllegalArgumentException("Invalid type of object specified for value in setByName");
            }
            setSplitRatio((BigDecimal) value);
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
        if (name.equals(EventSplitRatiosPeer.EVENT_ID))
        {
            return getEventId();
        }
        if (name.equals(EventSplitRatiosPeer.IN_LINK_ID))
        {
            return getInLinkId();
        }
        if (name.equals(EventSplitRatiosPeer.OUT_LINK_ID))
        {
            return getOutLinkId();
        }
        if (name.equals(EventSplitRatiosPeer.VEHICLE_TYPE_ID))
        {
            return getVehicleTypeId();
        }
        if (name.equals(EventSplitRatiosPeer.SPLIT_RATIO))
        {
            return getSplitRatio();
        }
        if (name.equals(EventSplitRatiosPeer.CREATED))
        {
            return getCreated();
        }
        if (name.equals(EventSplitRatiosPeer.MODIFIED))
        {
            return getModified();
        }
        if (name.equals(EventSplitRatiosPeer.CREATED_BY))
        {
            return getCreatedBy();
        }
        if (name.equals(EventSplitRatiosPeer.MODIFIED_BY))
        {
            return getModifiedBy();
        }
        if (name.equals(EventSplitRatiosPeer.MODSTAMP))
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
      if (EventSplitRatiosPeer.EVENT_ID.equals(name))
        {
            return setByName("EventId", value);
        }
      if (EventSplitRatiosPeer.IN_LINK_ID.equals(name))
        {
            return setByName("InLinkId", value);
        }
      if (EventSplitRatiosPeer.OUT_LINK_ID.equals(name))
        {
            return setByName("OutLinkId", value);
        }
      if (EventSplitRatiosPeer.VEHICLE_TYPE_ID.equals(name))
        {
            return setByName("VehicleTypeId", value);
        }
      if (EventSplitRatiosPeer.SPLIT_RATIO.equals(name))
        {
            return setByName("SplitRatio", value);
        }
      if (EventSplitRatiosPeer.CREATED.equals(name))
        {
            return setByName("Created", value);
        }
      if (EventSplitRatiosPeer.MODIFIED.equals(name))
        {
            return setByName("Modified", value);
        }
      if (EventSplitRatiosPeer.CREATED_BY.equals(name))
        {
            return setByName("CreatedBy", value);
        }
      if (EventSplitRatiosPeer.MODIFIED_BY.equals(name))
        {
            return setByName("ModifiedBy", value);
        }
      if (EventSplitRatiosPeer.MODSTAMP.equals(name))
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
            return getEventId();
        }
        if (pos == 1)
        {
            return getInLinkId();
        }
        if (pos == 2)
        {
            return getOutLinkId();
        }
        if (pos == 3)
        {
            return getVehicleTypeId();
        }
        if (pos == 4)
        {
            return getSplitRatio();
        }
        if (pos == 5)
        {
            return getCreated();
        }
        if (pos == 6)
        {
            return getModified();
        }
        if (pos == 7)
        {
            return getCreatedBy();
        }
        if (pos == 8)
        {
            return getModifiedBy();
        }
        if (pos == 9)
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
            return setByName("EventId", value);
        }
    if (position == 1)
        {
            return setByName("InLinkId", value);
        }
    if (position == 2)
        {
            return setByName("OutLinkId", value);
        }
    if (position == 3)
        {
            return setByName("VehicleTypeId", value);
        }
    if (position == 4)
        {
            return setByName("SplitRatio", value);
        }
    if (position == 5)
        {
            return setByName("Created", value);
        }
    if (position == 6)
        {
            return setByName("Modified", value);
        }
    if (position == 7)
        {
            return setByName("CreatedBy", value);
        }
    if (position == 8)
        {
            return setByName("ModifiedBy", value);
        }
    if (position == 9)
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
        save(EventSplitRatiosPeer.DATABASE_NAME);
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
                    EventSplitRatiosPeer.doInsert((EventSplitRatios) this, con);
                    setNew(false);
                }
                else
                {
                    EventSplitRatiosPeer.doUpdate((EventSplitRatios) this, con);
                }
            }

            alreadyInSave = false;
        }
    }



    private final SimpleKey[] pks = new SimpleKey[4];
    private final ComboKey comboPK = new ComboKey(pks);

    /**
     * Set the PrimaryKey with an ObjectKey
     *
     * @param key
     */
    public void setPrimaryKey(ObjectKey key) throws TorqueException
    {
        SimpleKey[] keys = (SimpleKey[]) key.getValue();
        setEventId(new Long(((NumberKey)keys[0]).longValue()));
        setInLinkId(new Long(((NumberKey)keys[1]).longValue()));
        setOutLinkId(new Long(((NumberKey)keys[2]).longValue()));
        setVehicleTypeId(new Long(((NumberKey)keys[3]).longValue()));
    }

    /**
     * Set the PrimaryKey using SimpleKeys.
     *
     * @param eventId Long
     * @param inLinkId Long
     * @param outLinkId Long
     * @param vehicleTypeId Long
     */
    public void setPrimaryKey( Long eventId, Long inLinkId, Long outLinkId, Long vehicleTypeId)
        throws TorqueException
    {
        setEventId(eventId);
        setInLinkId(inLinkId);
        setOutLinkId(outLinkId);
        setVehicleTypeId(vehicleTypeId);
    }

    /**
     * Set the PrimaryKey using a String.
     */
    public void setPrimaryKey(String key) throws TorqueException
    {
        setPrimaryKey(new ComboKey(key));
    }

    /**
     * returns an id that differentiates this object from others
     * of its class.
     */
    public ObjectKey getPrimaryKey()
    {
        pks[0] = SimpleKey.keyFor(getEventId());
        pks[1] = SimpleKey.keyFor(getInLinkId());
        pks[2] = SimpleKey.keyFor(getOutLinkId());
        pks[3] = SimpleKey.keyFor(getVehicleTypeId());
        return comboPK;
    }
 

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     */
    public EventSplitRatios copy() throws TorqueException
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
    public EventSplitRatios copy(Connection con) throws TorqueException
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
    public EventSplitRatios copy(boolean deepcopy) throws TorqueException
    {
        return copyInto(new EventSplitRatios(), deepcopy);
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
    public EventSplitRatios copy(boolean deepcopy, Connection con) throws TorqueException
    {
        return copyInto(new EventSplitRatios(), deepcopy, con);
    }
  
    /**
     * Fills the copyObj with the contents of this object.
     * The associated objects are also copied and treated as new objects.
     *
     * @param copyObj the object to fill.
     */
    protected EventSplitRatios copyInto(EventSplitRatios copyObj) throws TorqueException
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
    protected EventSplitRatios copyInto(EventSplitRatios copyObj, Connection con) throws TorqueException
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
    protected EventSplitRatios copyInto(EventSplitRatios copyObj, boolean deepcopy) throws TorqueException
    {
        copyObj.setEventId(eventId);
        copyObj.setInLinkId(inLinkId);
        copyObj.setOutLinkId(outLinkId);
        copyObj.setVehicleTypeId(vehicleTypeId);
        copyObj.setSplitRatio(splitRatio);
        copyObj.setCreated(created);
        copyObj.setModified(modified);
        copyObj.setCreatedBy(createdBy);
        copyObj.setModifiedBy(modifiedBy);
        copyObj.setModstamp(modstamp);

        copyObj.setEventId((Long)null);
        copyObj.setInLinkId((Long)null);
        copyObj.setOutLinkId((Long)null);
        copyObj.setVehicleTypeId((Long)null);

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
    protected EventSplitRatios copyInto(EventSplitRatios copyObj, boolean deepcopy, Connection con) throws TorqueException
    {
        copyObj.setEventId(eventId);
        copyObj.setInLinkId(inLinkId);
        copyObj.setOutLinkId(outLinkId);
        copyObj.setVehicleTypeId(vehicleTypeId);
        copyObj.setSplitRatio(splitRatio);
        copyObj.setCreated(created);
        copyObj.setModified(modified);
        copyObj.setCreatedBy(createdBy);
        copyObj.setModifiedBy(modifiedBy);
        copyObj.setModstamp(modstamp);

        copyObj.setEventId((Long)null);
        copyObj.setInLinkId((Long)null);
        copyObj.setOutLinkId((Long)null);
        copyObj.setVehicleTypeId((Long)null);

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
    public EventSplitRatiosPeer getPeer()
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
        return EventSplitRatiosPeer.getTableMap();
    }


    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("EventSplitRatios:\n");
        str.append("EventId = ")
           .append(getEventId())
           .append("\n");
        str.append("InLinkId = ")
           .append(getInLinkId())
           .append("\n");
        str.append("OutLinkId = ")
           .append(getOutLinkId())
           .append("\n");
        str.append("VehicleTypeId = ")
           .append(getVehicleTypeId())
           .append("\n");
        str.append("SplitRatio = ")
           .append(getSplitRatio())
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
