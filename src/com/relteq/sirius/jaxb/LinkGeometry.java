//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.29 at 04:19:00 PM PST 
//


package com.relteq.sirius.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *       &lt;choice>
 *         &lt;element ref="{}EncodedPolyline"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "encodedPolyline"
})
@XmlRootElement(name = "LinkGeometry")
public class LinkGeometry {

    @XmlElement(name = "EncodedPolyline")
    protected EncodedPolyline encodedPolyline;

    /**
     * Gets the value of the encodedPolyline property.
     * 
     * @return
     *     possible object is
     *     {@link EncodedPolyline }
     *     
     */
    public EncodedPolyline getEncodedPolyline() {
        return encodedPolyline;
    }

    /**
     * Sets the value of the encodedPolyline property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncodedPolyline }
     *     
     */
    public void setEncodedPolyline(EncodedPolyline value) {
        this.encodedPolyline = value;
    }

}
