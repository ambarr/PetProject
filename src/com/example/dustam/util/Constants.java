package com.example.dustam.util;


public final class Constants {
    //Sender ID for use in GCM communications
    public static final String SENDER_ID = "916866207665";

    public static final String BASE_URL = "http://warm-forest-7457.herokuapp.com/";
    public static final String CREATE_PARTY_URL = BASE_URL + "api/add_party/";
    public static final String GET_REQUESTS_URL = BASE_URL + "api/get_requests/";
    public static final String NEARBY_PARTIES_URL = BASE_URL + "api/find_nearby";
    public static final String GET_ALL_PARTIES = BASE_URL + "api/get_all_parties";
    public static final String JOIN_PARTY_URL = BASE_URL + "api/join_party";
    public static final String REQUEST_SONGS_URL = BASE_URL + "api/request_songs";
    public static final String END_PARTY_URL = BASE_URL + "api/end_party/";

    public static final String NOTIFY_HOST = "song_requests";
    public static final String LISTENER_END_PARTY = "end_party";

    // HTTP Constants
    public static final int HTTP_SUCCESS=200;
}
