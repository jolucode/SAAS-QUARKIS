package service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ExchangeRateType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for SUNATRetentionInformationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SUNATRetentionInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATRetentionAmount" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATRetentionDate" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATNetTotalPaid" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}ExchangeRate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SUNATRetentionInformationType", propOrder = {
        "sunatRetentionAmount",
        "sunatRetentionDate",
        "sunatNetTotalPaid",
        "exchangeRate"
})
public class SUNATRetentionInformationType {

    @XmlElement(name = "SUNATRetentionAmount", required = true)
    protected SUNATRetentionAmountType sunatRetentionAmount;

    @XmlElement(name = "SUNATRetentionDate", required = true)
    protected SUNATRetentionDateType sunatRetentionDate;

    @XmlElement(name = "SUNATNetTotalPaid", required = true)
    protected SUNATNetTotalPaidType sunatNetTotalPaid;

    @XmlElement(name = "ExchangeRate", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    protected ExchangeRateType exchangeRate;

    public SUNATRetentionAmountType getSunatRetentionAmount() {
        return sunatRetentionAmount;
    }

    public void setSunatRetentionAmount(SUNATRetentionAmountType value) {
        this.sunatRetentionAmount = value;
    }

    public SUNATRetentionDateType getSunatRetentionDate() {
        return sunatRetentionDate;
    }

    public void setSunatRetentionDate(SUNATRetentionDateType value) {
        this.sunatRetentionDate = value;
    }

    public SUNATNetTotalPaidType getSunatNetTotalPaid() {
        return sunatNetTotalPaid;
    }

    public void setSunatNetTotalPaid(SUNATNetTotalPaidType value) {
        this.sunatNetTotalPaid = value;
    }

    public ExchangeRateType getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRateType value) {
        this.exchangeRate = value;
    }

}
