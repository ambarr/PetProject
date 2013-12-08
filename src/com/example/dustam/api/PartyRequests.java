package com.example.dustam.api;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.NearbyPartyInfo;
import com.example.dustam.parties.Song;
import com.example.dustam.util.Constants;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class PartyRequests {

    private static String TAG="PartyAPIRequest";

    public void getNearbyParties(NearbyPartyCallback callback) {
        new GetPartiesTask(callback).execute(new Double[]{});
    }

    public void joinParty(String id, String password, JoinPartyCallback callback) {
        new JoinPartyTask(id, password, callback).execute(new Void[]{});
    }

    private class GetPartiesTask extends AsyncTask<Double, Void, String> {

        NearbyPartyCallback callback;
        public GetPartiesTask(NearbyPartyCallback callback) {
            this.callback = callback;
        }

        protected String doInBackground(Double... params) {
            ArrayList<NearbyPartyInfo> partyList = new ArrayList<NearbyPartyInfo>();

            byte[] bytes;
            String res = "";

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(Constants.GET_ALL_PARTIES);
            //get.setHeader("Content-Type", "application/json");

            try {
                HttpResponse response = client.execute(get);
                StatusLine status = response.getStatusLine();
                final HttpEntity entity = response.getEntity();

                if(status.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    bytes = EntityUtils.toByteArray(entity);
                    res = new String(bytes, "UTF-8");
                }
                else {
                    Log.e(TAG, "Error retrieving nearby parties. Error code: " + status.getStatusCode());
                    callback.onFailure();
                }
                entity.consumeContent();
            }
            catch(Exception e) {
                Log.e(TAG, "Nearby parties error: " + e.toString());
                callback.onFailure();
            }

            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonResult = new JSONArray(result);
                ArrayList<NearbyPartyInfo> list = new ArrayList<NearbyPartyInfo>();
                for(int i = 0; i < jsonResult.length(); i++) {
                    boolean password;
                    if(jsonResult.getJSONObject(i).getString("Password").equals("")) {
                        password = false;
                    } else {
                        password = true;
                    }

                    NearbyPartyInfo info = new NearbyPartyInfo(
                            jsonResult.getJSONObject(i).getString("Name"),
                            jsonResult.getJSONObject(i).getString("id"),
                            password);

                    list.add(info);
                }
                callback.onSuccess(list);
            } catch(Exception e) {
                Log.e(TAG, "Result from nearby parties request not valid json. " + e.getMessage());
                callback.onFailure();
            }
        }
    }

    private class JoinPartyTask extends AsyncTask<Void, Void, String> {

        private String partyId;
        private String passwordAttempt;
        private String regId;
        private JSONObject requestJson;
        private JoinPartyCallback callback;

        public JoinPartyTask(String partyId, String passwordAttempt,
                             JoinPartyCallback callback) {
            this.partyId = partyId;
            this.passwordAttempt = passwordAttempt;
            this.callback = callback;
        }

        public JSONObject createJsonRequest() throws JSONException, IOException {
            regId = GoogleCloudMessaging.getInstance(callback.getActivity())
                    .register(Constants.SENDER_ID);

            JSONObject json = new JSONObject();
            json.put("id", partyId);
            json.put("password", passwordAttempt);
            json.put("deviceID", regId);
            return json;
        }

        protected String doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.JOIN_PARTY_URL);
            String res;

            try {
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Accept", "application/json");

                requestJson = this.createJsonRequest();
                post.setEntity(new StringEntity(requestJson.toString()));

                HttpResponse response = client.execute(post);
                StatusLine status = response.getStatusLine();

                if(response.getStatusLine().getStatusCode() != Constants.HTTP_SUCCESS) {
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }

                res = builder.toString();
            } catch(Exception e) {
                Log.e(TAG, "Join party request error: " + e.toString());
                return null;
            }

            return res;
        }

        @Override
        public void onPostExecute(String res) {
            if(res == null) {
                callback.onFailure();
            } else {
                try {
                    JSONArray artistJson = new JSONArray(res);
                    ArrayList <Artist> artists = new ArrayList<Artist>();
                    for(int i = 0; i < artistJson.length(); i++) {
                        Artist artist = new Artist();
                        artist.setArtistName(artistJson.getJSONObject(i).getString("Name"));
                        JSONArray jsonSongs = artistJson.getJSONObject(i).getJSONArray("Songs");

                        ArrayList<Song> songs = new ArrayList<Song>();
                        for(int j = 0; j < jsonSongs.length(); j++) {
                            Song song = new Song();
                            song.setName(jsonSongs.getJSONObject(j).getString("Title"));
                            songs.add(song);
                        }
                        artist.setSongs(songs);
                        artists.add(artist);
                        callback.onSuccess(artists);
                }
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    callback.onFailure();
                }
            }
        }
    }

    public interface NearbyPartyCallback {
        public void onSuccess(ArrayList<NearbyPartyInfo> result);
        public void onFailure();
        public String getGCMRegId();
    }

    public interface JoinPartyCallback {
        public void onSuccess(ArrayList<Artist> result);
        public void onFailure();
        public Activity getActivity();
    }

}


