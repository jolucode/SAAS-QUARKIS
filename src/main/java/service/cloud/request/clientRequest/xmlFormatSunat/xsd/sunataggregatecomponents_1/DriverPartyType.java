/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.PartyType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.AdditionalAccountIDType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.CustomerAssignedAccountIDType;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for DriverPartyType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DriverPartyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}CustomerAssignedAccountID" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}AdditionalAccountID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}Party" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@XmlRootElement(name = "DriverParty")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DriverPartyType", propOrder = {
        "customerAssignedAccountID",
        "additionalAccountID",
        "party"
})
public class DriverPartyType {

    @XmlElement(name = "CustomerAssignedAccountID", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
    protected CustomerAssignedAccountIDType customerAssignedAccountID;

    @XmlElement(name = "AdditionalAccountID", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
    protected List<AdditionalAccountIDType> additionalAccountID;

    @XmlElement(name = "Party", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    protected PartyType party;

    /**
     * Gets the value of the customerAssignedAccountID property.
     *
     * @return possible object is
     * {@link CustomerAssignedAccountIDType }
     */
    public CustomerAssignedAccountIDType getCustomerAssignedAccountID() {
        return customerAssignedAccountID;
    }

    /**
     * Sets the value of the customerAssignedAccountID property.
     *
     * @param value allowed object is
     *              {@link CustomerAssignedAccountIDType }
     */
    public void setCustomerAssignedAccountID(CustomerAssignedAccountIDType value) {
        this.customerAssignedAccountID = value;
    }

    /**
     * Gets the value of the additionalAccountID property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalAccountID property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalAccountID().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionalAccountIDType }
     */
    public List<AdditionalAccountIDType> getAdditionalAccountID() {
        if (additionalAccountID == null) {
            additionalAccountID = new ArrayList<AdditionalAccountIDType>();
        }
        return this.additionalAccountID;
    }

    /**
     * Gets the value of the party property.
     *
     * @return possible object is
     * {@link PartyType }
     */
    public PartyType getParty() {
        return party;
    }

    /**
     * Sets the value of the party property.
     *
     * @param value allowed object is
     *              {@link PartyType }
     */
    public void setParty(PartyType value) {
        this.party = value;
    }


}
