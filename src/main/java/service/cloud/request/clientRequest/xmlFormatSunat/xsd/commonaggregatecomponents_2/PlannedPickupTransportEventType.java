/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author VSUSER
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlannedPickupTransportEventType", propOrder = {
        "location"
})

public class PlannedPickupTransportEventType {


    @XmlElement(name = "Location")
    protected LocationType location;

    public LocationType getLocation() {
        return location;
    }

    public void setLocation(LocationType location) {
        this.location = location;
    }


}
