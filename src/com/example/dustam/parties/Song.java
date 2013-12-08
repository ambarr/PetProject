package com.example.dustam.parties;

import android.os.Parcel;
import android.os.Parcelable;


public class Song implements Parcelable {

    private String name;
    private String uri;
    private String artistName;
    private String albumName;

    private int numRequests;

    public Song() {
        numRequests = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int numRequests() {
        return numRequests;
    }

    public void request() {
        numRequests++;
    }

    public void clearRequests() {
        numRequests = 0;
    }

    public String toString() {
        return name;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(uri);
    }

    public static final Parcelable.Creator<Song> CREATOR
            = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private Song(Parcel in) {
        name = in.readString();
        uri = in.readString();
    }
}
