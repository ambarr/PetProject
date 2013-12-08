package com.example.dustam.parties;

import java.util.ArrayList;

public class JoinedPartyInfo extends PartyInfo {

    private String partyName;
    private String partyId;
    private ArrayList<Artist> artists;

    public JoinedPartyInfo() {

    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }
}
