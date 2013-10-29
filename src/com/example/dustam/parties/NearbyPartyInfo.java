package com.example.dustam.parties;


public class NearbyPartyInfo extends PartyInfo {
    private String partyName;
    private int partyId;
    private boolean hasPassword;

    public NearbyPartyInfo(String partyName, int partyId, boolean hasPassword) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.hasPassword = hasPassword();
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    @Override
    public String toString() {
        return this.partyName;
    }
}
