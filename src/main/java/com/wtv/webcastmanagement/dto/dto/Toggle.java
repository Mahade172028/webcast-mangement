
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Toggle {

    private boolean eventTitle;
    private boolean internalIp;
    private boolean machineName;
    private boolean machineUser;
    private boolean o365Oid;
    private boolean o365UniqueUserDigest;
    private boolean userEmail;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public boolean isEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(boolean eventTitle) {
        this.eventTitle = eventTitle;
    }

    public boolean isInternalIp() {
        return internalIp;
    }

    public void setInternalIp(boolean internalIp) {
        this.internalIp = internalIp;
    }

    public boolean isMachineName() {
        return machineName;
    }

    public void setMachineName(boolean machineName) {
        this.machineName = machineName;
    }

    public boolean isMachineUser() {
        return machineUser;
    }

    public void setMachineUser(boolean machineUser) {
        this.machineUser = machineUser;
    }

    public boolean isO365Oid() {
        return o365Oid;
    }

    public void setO365Oid(boolean o365Oid) {
        this.o365Oid = o365Oid;
    }

    public boolean isO365UniqueUserDigest() {
        return o365UniqueUserDigest;
    }

    public void setO365UniqueUserDigest(boolean o365UniqueUserDigest) {
        this.o365UniqueUserDigest = o365UniqueUserDigest;
    }

    public boolean isUserEmail() {
        return userEmail;
    }

    public void setUserEmail(boolean userEmail) {
        this.userEmail = userEmail;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
