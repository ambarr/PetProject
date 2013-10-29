package com.example.dustam.parties;

import java.util.ArrayList;

public class JoinedPartyInfo extends PartyInfo {

    private String partyName;
    private int partyId;
    private ArrayList<Artist> artists;

    public JoinedPartyInfo() {

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

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }
}
