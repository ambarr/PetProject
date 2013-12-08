package com.example.dustam.parties;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Artist implements Parcelable {

    private String artistName;
    private ArrayList<Song> songs;

    public Artist() {
        songs = new ArrayList<Song>();
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public String toString() {
        return artistName;
    }
    @Override
    public int describeContents() {
        return songs.size();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeTypedList(songs);
    }

    public static final Parcelable.Creator<Artist> CREATOR
            = new Parcelable.Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    private Artist(Parcel in) {
        artistName = in.readString();

        songs = new ArrayList<Song>();
        in.readTypedList(songs, Song.CREATOR);
    }
}
