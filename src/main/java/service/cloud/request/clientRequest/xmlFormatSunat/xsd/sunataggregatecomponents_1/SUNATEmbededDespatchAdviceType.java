/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.AddressType;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SUNATEmbededDespatchAdviceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SUNATEmbededDespatchAdviceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}DeliveryAddress" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}OriginAddress" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATCarrierParty" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}DriverParty" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATRoadTransport" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlRootElement(name = "SUNATEmbededDespatchAdvice")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SUNATEmbededDespatchAdviceType", propOrder = {
        "deliveryAddress",
        "originAddress",
        "sunatCarrierParty",
        "driverParty",
        "sunatRoadTransport"
})
public class SUNATEmbededDespatchAdviceType {

    @XmlElement(name = "DeliveryAddress", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    protected AddressType deliveryAddress;

    @XmlElement(name = "OriginAddress", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    protected AddressType originAddress;

    @XmlElement(name = "SUNATCarrierParty")
    protected SUNATCarrierPartyType sunatCarrierParty;

    @XmlElement(name = "DriverParty")
    protected DriverPartyType driverParty;

    @XmlElement(name = "SUNATRoadTransport")
    protected List<SUNATRoadTransportType> sunatRoadTransport;

    /**
     * Gets the value of the deliveryAddress property.
     *
     * @return possible object is
     * {@link AddressType }
     */
    public AddressType getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Sets the value of the deliveryAddress property.
     *
     * @param value allowed object is
     *              {@link AddressType }
     */
    public void setDeliveryAddress(AddressType value) {
        this.deliveryAddress = value;
    }

    /**
     * Gets the value of the originAddress property.
     *
     * @return possible object is
     * {@link AddressType }
     */
    public AddressType getOriginAddress() {
        return originAddress;
    }

    /**
     * Sets the value of the originAddress property.
     *
     * @param value allowed object is
     *              {@link AddressType }
     */
    public void setOriginAddress(AddressType value) {
        this.originAddress = value;
    }

    /**
     * Gets the value of the sunatCarrierParty property.
     *
     * @return possible object is
     * {@link SUNATCarrierPartyType }
     */
    public SUNATCarrierPartyType getSUNATCarrierParty() {
        return sunatCarrierParty;
    }

    /**
     * Sets the value of the sunatCarrierParty property.
     *
     * @param value allowed object is
     *              {@link SUNATCarrierPartyType }
     */
    public void setSUNATCarrierParty(SUNATCarrierPartyType value) {
        this.sunatCarrierParty = value;
    }

    /**
     * Gets the value of the driverParty property.
     *
     * @return possible object is
     * {@link DriverPartyType }
     */
    public DriverPartyType getDriverParty() {
        return driverParty;
    }

    /**
     * Sets the value of the driverParty property.
     *
     * @param value allowed object is
     *              {@link DriverPartyType }
     */
    public void setDriverParty(DriverPartyType value) {
        this.driverParty = value;
    }

    /**
     * Gets the value of the sunatRoadTransport property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sunatRoadTransport property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSUNATRoadTransport().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SUNATRoadTransportType }
     *
     * @return
     */
    public List<SUNATRoadTransportType> getSUNATRoadTransport() {
        if (sunatRoadTransport == null) {
            sunatRoadTransport = new ArrayList<SUNATRoadTransportType>();
        }
        return this.sunatRoadTransport;
    }


}
