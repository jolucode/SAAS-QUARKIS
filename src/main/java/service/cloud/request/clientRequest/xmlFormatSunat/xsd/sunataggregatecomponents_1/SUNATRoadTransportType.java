/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.RoadTransportType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.BrandNameType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TransportAuthorizationCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TransportMeansTypeCodeType;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for SUNATRoadTransportType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="SUNATRoadTransportType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}RoadTransportType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}TransportMeansTypeCode" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}TransportAuthorizationCode" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SemiTrailer" maxOccurs="5" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}BrandName" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlRootElement(name = "SUNATRoadTransport")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SUNATRoadTransportType", propOrder = {
        "transportMeansTypeCode",
        "transportAuthorizationCode",
        "semiTrailer",
        "brandName"
})
public class SUNATRoadTransportType
        extends RoadTransportType {

    @XmlElement(name = "TransportMeansTypeCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
    protected TransportMeansTypeCodeType transportMeansTypeCode;

    @XmlElement(name = "TransportAuthorizationCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
    protected TransportAuthorizationCodeType transportAuthorizationCode;

    @XmlElement(name = "SemiTrailer")
    protected List<RoadTransportType> semiTrailer;

    @XmlElement(name = "BrandName", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
    protected BrandNameType brandName;

    /**
     * Gets the value of the transportMeansTypeCode property.
     *
     * @return possible object is {@link TransportMeansTypeCodeType }
     */
    public TransportMeansTypeCodeType getTransportMeansTypeCode() {
        return transportMeansTypeCode;
    }

    /**
     * Sets the value of the transportMeansTypeCode property.
     *
     * @param value allowed object is {@link TransportMeansTypeCodeType }
     */
    public void setTransportMeansTypeCode(TransportMeansTypeCodeType value) {
        this.transportMeansTypeCode = value;
    }

    /**
     * Gets the value of the transportAuthorizationCode property.
     *
     * @return possible object is {@link TransportAuthorizationCodeType }
     */
    public TransportAuthorizationCodeType getTransportAuthorizationCode() {
        return transportAuthorizationCode;
    }

    /**
     * Sets the value of the transportAuthorizationCode property.
     *
     * @param value allowed object is {@link TransportAuthorizationCodeType }
     */
    public void setTransportAuthorizationCode(TransportAuthorizationCodeType value) {
        this.transportAuthorizationCode = value;
    }

    /**
     * Gets the value of the semiTrailer property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the semiTrailer property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSemiTrailer().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RoadTransportType }
     */
    public List<RoadTransportType> getSemiTrailer() {
        if (semiTrailer == null) {
            semiTrailer = new ArrayList<RoadTransportType>();
        }
        return this.semiTrailer;
    }

    /**
     * Gets the value of the brandName property.
     *
     * @return possible object is {@link BrandNameType }
     */
    public BrandNameType getBrandName() {
        return brandName;
    }

    /**
     * Sets the value of the brandName property.
     *
     * @param value allowed object is {@link BrandNameType }
     */
    public void setBrandName(BrandNameType value) {
        this.brandName = value;
    }

}
