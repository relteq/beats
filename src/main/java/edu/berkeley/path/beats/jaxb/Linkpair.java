//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-146 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.14 at 02:24:14 PM PST 
//


package com.relteq.sirius.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="link_a" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="link_b" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "linkpair")
public class Linkpair {

    @XmlAttribute(name = "link_a", required = true)
    protected String linkA;
    @XmlAttribute(name = "link_b", required = true)
    protected String linkB;

    /**
     * Gets the value of the linkA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkA() {
        return linkA;
    }

    /**
     * Sets the value of the linkA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkA(String value) {
        this.linkA = value;
    }

    /**
     * Gets the value of the linkB property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkB() {
        return linkB;
    }

    /**
     * Sets the value of the linkB property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkB(String value) {
        this.linkB = value;
    }

}
