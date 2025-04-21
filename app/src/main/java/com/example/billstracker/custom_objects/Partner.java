package com.example.billstracker.custom_objects;

import java.io.Serializable;

/** @noinspection unused*/
public class Partner implements Serializable {

    private String partnerUid;
    private boolean sharingAuthorized;
    private String partnerName;

    public Partner (String partnerId, boolean sharingAuthorized, String partnerName) {

        setPartnerUid(partnerId);
        setSharingAuthorized(sharingAuthorized);
        setPartnerName(partnerName);
    }

    public Partner () {

    }

    public String getPartnerUid () {
        return partnerUid;
    }
    public void setPartnerUid (String partnerUid) {
        this.partnerUid = partnerUid;
    }
    public boolean getSharingAuthorized () {
        return sharingAuthorized;
    }
    public void setSharingAuthorized (boolean sharingAuthorized) {
        this.sharingAuthorized = sharingAuthorized;
    }
    public String getPartnerName () {
        return partnerName;
    }
    public void setPartnerName (String partnerName) {
        this.partnerName = partnerName;
    }

}
