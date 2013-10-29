package com.example.dustam.api;

import android.os.AsyncTask;
import android.util.Log;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.NearbyPartyInfo;
import com.example.dustam.parties.Song;
import com.example.dustam.util.Constants;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class PartyRequests {

    private static String TAG="PartyAPIRequest";

    public void getNearbyParties(NearbyPartyCallback callback) {
        new GetNearbyPartiesTask(callback).execute(new Double[]{});
    }

    public void joinParty(int id, String password, JoinPartyCallback callback) {
        new JoinPartyTask(id, password, callback).execute(new Void[]{});
    }

    private class GetNearbyPartiesTask extends AsyncTask<Double, Void, String> {

        NearbyPartyCallback callback;
        public GetNearbyPartiesTask(NearbyPartyCallback callback) {
            this.callback = callback;
        }

        protected String doInBackground(Double... params) {
            ArrayList<NearbyPartyInfo> partyList = new ArrayList<NearbyPartyInfo>();

            byte[] bytes;
            String res = "";

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(Constants.FIND_NEARBY_URL);
            get.setHeader("Content-Type", "application/json");

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
            Log.d(TAG, res);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonResult = new JSONArray(result);
                ArrayList<NearbyPartyInfo> list = new ArrayList<NearbyPartyInfo>();
                for(int i = 0; i < jsonResult.length(); i++) {
                    NearbyPartyInfo info = new NearbyPartyInfo(
                            jsonResult.getJSONObject(i).getString("name"),
                            jsonResult.getJSONObject(i).getInt("id"),
                            jsonResult.getJSONObject(i).getBoolean("hasPassword"));

                    list.add(info);
                }
                callback.onSuccess(list);
            } catch(Exception e) {
                Log.e(TAG, "Result from nearby parties request not valid json.");
                callback.onFailure();
            }
        }
    }

    private class JoinPartyTask extends AsyncTask<Void, Void, Void> {

        private int partyId;
        private String passwordAttempt;
        private JSONObject requestJson;
        private JoinPartyCallback callback;

        public JoinPartyTask(int partyId, String passwordAttempt,
                             JoinPartyCallback callback) {
            this.partyId = partyId;
            this.passwordAttempt = passwordAttempt;
            this.callback = callback;
            try {
                requestJson = this.createJsonRequest();
            } catch(JSONException e) {
                e.printStackTrace();
                callback.onFailure();
            }
        }

        public JSONObject createJsonRequest() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", partyId);
            json.put("password", passwordAttempt);
            return json;
        }

        protected Void doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.JOIN_PARTY_URL);

            try {
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(requestJson.toString()));

                HttpResponse response = client.execute(post);
                StatusLine status = response.getStatusLine();

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONObject jsonResult = new JSONObject(builder.toString());

                ArrayList<Artist> artists = new ArrayList<Artist>();
                JSONArray artistJson = jsonResult.getJSONArray("artists");
                for(int i = 0; i < artistJson.length(); i++) {
                    Artist artist = new Artist();
                    artist.setArtistName(artistJson.getJSONObject(i).getString("name"));
                    JSONArray jsonSongs = artistJson.getJSONObject(i).getJSONArray("songs");

                    ArrayList<Song> songs = new ArrayList<Song>();
                    for(int j = 0; j < jsonSongs.length(); j++) {
                        Song song = new Song();
                        song.setName(jsonSongs.getJSONObject(i).getString("name"));
                        songs.add(song);
                    }
                    artist.setSongs(songs);
                    artists.add(artist);
                }
            } catch(Exception e) {
                Log.e(TAG, "Join party request error: " + e.toString());
                callback.onFailure();
            }
            return null;
        }
    }

    public interface NearbyPartyCallback {
        public void onSuccess(ArrayList<NearbyPartyInfo> result);
        public void onFailure();
    }

    public interface JoinPartyCallback {
        public void onSuccess(ArrayList<Artist> result);
        public void onFailure();
    }

}


