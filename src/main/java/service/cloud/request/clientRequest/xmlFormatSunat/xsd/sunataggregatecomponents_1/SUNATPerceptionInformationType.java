package service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ExchangeRateType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for SUNATPerceptionInformationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SUNATPerceptionInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATPerceptionAmount" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATPerceptionDate" minOccurs="0"/>
 *         &lt;element ref="{urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1}SUNATNetTotalCashed" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}ExchangeRate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SUNATPerceptionInformationType", propOrder = {
        "sunatPerceptionAmount",
        "sunatPerceptionDate",
        "sunatNetTotalCashed",
        "exchangeRate"
})
public class SUNATPerceptionInformationType {

    @XmlElement(name = "SUNATPerceptionAmount", required = true)
    protected SUNATPerceptionAmountType sunatPerceptionAmount;

    @XmlElement(name = "SUNATPerceptionDate", required = true)
    protected SUNATPerceptionDateType sunatPerceptionDate;

    @XmlElement(name = "SUNATNetTotalCashed", required = true)
    protected SUNATNetTotalCashedType sunatNetTotalCashed;

    @XmlElement(name = "ExchangeRate", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    protected ExchangeRateType exchangeRate;

    public SUNATPerceptionAmountType getPerceptionAmount() {
        return sunatPerceptionAmount;
    }

    public void setPerceptionAmount(SUNATPerceptionAmountType value) {
        this.sunatPerceptionAmount = value;
    }

    public SUNATPerceptionDateType getPerceptionDate() {
        return sunatPerceptionDate;
    }

    public void setPerceptionDate(SUNATPerceptionDateType value) {
        this.sunatPerceptionDate = value;
    }

    public SUNATNetTotalCashedType getSunatNetTotalCashed() {
        return sunatNetTotalCashed;
    }

    public void setSunatNetTotalCashed(SUNATNetTotalCashedType value) {
        this.sunatNetTotalCashed = value;
    }

    public ExchangeRateType getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRateType value) {
        this.exchangeRate = value;
    }

}
