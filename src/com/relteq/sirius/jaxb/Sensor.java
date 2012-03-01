//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.29 at 04:19:00 PM PST 
//


package com.relteq.sirius.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element ref="{}description" minOccurs="0"/>
 *         &lt;element ref="{}position" minOccurs="0"/>
 *         &lt;element ref="{}display_position" minOccurs="0"/>
 *         &lt;element ref="{}link_reference" minOccurs="0"/>
 *         &lt;element ref="{}parameters" minOccurs="0"/>
 *         &lt;element ref="{}data_sources" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="link_position" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="static_point|static_area|moving_point"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="link_type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="freeway|HOV|onramp|offramp|other"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "sensor")
public class Sensor {

    protected String description;
    protected Position position;
    @XmlElement(name = "display_position")
    protected DisplayPosition displayPosition;
    @XmlElement(name = "link_reference")
    protected LinkReference linkReference;
    protected Parameters parameters;
    @XmlElement(name = "data_sources")
    protected DataSources dataSources;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "link_position")
    protected BigDecimal linkPosition;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "link_type", required = true)
    protected String linkType;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link Position }
     *     
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link Position }
     *     
     */
    public void setPosition(Position value) {
        this.position = value;
    }

    /**
     * Gets the value of the displayPosition property.
     * 
     * @return
     *     possible object is
     *     {@link DisplayPosition }
     *     
     */
    public DisplayPosition getDisplayPosition() {
        return displayPosition;
    }

    /**
     * Sets the value of the displayPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisplayPosition }
     *     
     */
    public void setDisplayPosition(DisplayPosition value) {
        this.displayPosition = value;
    }

    /**
     * Gets the value of the linkReference property.
     * 
     * @return
     *     possible object is
     *     {@link LinkReference }
     *     
     */
    public LinkReference getLinkReference() {
        return linkReference;
    }

    /**
     * Sets the value of the linkReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkReference }
     *     
     */
    public void setLinkReference(LinkReference value) {
        this.linkReference = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link Parameters }
     *     
     */
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameters }
     *     
     */
    public void setParameters(Parameters value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the dataSources property.
     * 
     * @return
     *     possible object is
     *     {@link DataSources }
     *     
     */
    public DataSources getDataSources() {
        return dataSources;
    }

    /**
     * Sets the value of the dataSources property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSources }
     *     
     */
    public void setDataSources(DataSources value) {
        this.dataSources = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the linkPosition property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLinkPosition() {
        return linkPosition;
    }

    /**
     * Sets the value of the linkPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLinkPosition(BigDecimal value) {
        this.linkPosition = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkType(String value) {
        this.linkType = value;
    }

}
