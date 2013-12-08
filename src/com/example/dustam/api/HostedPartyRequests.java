package com.example.dustam.api;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;
import com.example.dustam.util.Constants;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class HostedPartyRequests {

    private static String TAG = "HostedPartyAPI";

    public void addParty(String partyName, String password,
                         List<Artist> artists, RequestCallback cb) {
        new AddPartyTask(partyName, password, artists, cb).execute();
    }

    private class AddPartyTask extends AsyncTask<Void, Void, String> {

        private String name, password, regId;
        private List<Artist> artists;
        private RequestCallback callback;

        public AddPartyTask(String name, String password,
                            List<Artist> artists, RequestCallback cb) {
            this.name = name;
            this.password = password;
            this.artists = artists;
            this.callback = cb;
            this.regId = "";
        }

        public String doInBackground(Void... params) {
            String res= null;

            try {
                regId = GoogleCloudMessaging.getInstance(callback.getActivity())
                        .register(Constants.SENDER_ID);

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.CREATE_PARTY_URL);

                post.setEntity(new StringEntity(convertArtistsToJsonString()));
                post.setHeader("Content-Type", "application/json");
                HttpResponse response = client.execute(post);
                StatusLine status = response.getStatusLine();

                if(status.getStatusCode() != Constants.HTTP_SUCCESS) {
                    Log.e(TAG, "Error when posting party to server, status code: " + status.getStatusCode());
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }

                res = builder.toString();
                res = res.replace("\"", "");
                response.getEntity().consumeContent();
            } catch(JSONException e) {
                Log.e(TAG, e.toString());
                return null;
            } catch(IOException e) {
                Log.e(TAG, e.toString());
            }

            return res;
        }

        @Override
        public void onPostExecute(String res) {
            if(res == null) {
                callback.onFailure();
            } else {
                callback.onSuccess(res, regId);
            }
        }

        private String convertArtistsToJsonString() throws JSONException, IOException {
            JSONObject obj = new JSONObject();
            obj.put("Name", name);
            obj.put("Password", password);

            JSONArray artists = new JSONArray();
            for(Artist a : this.artists) {
                JSONObject jsonArtist = new JSONObject();
                jsonArtist.put("Name", a.getArtistName());

                JSONArray jsonSongs = new JSONArray();
                for(Song s : a.getSongs()) {
                    JSONObject jsonSong = new JSONObject();
                    jsonSong.put("Title", s.getName());
                    jsonSong.put("ArtistName", s.getArtistName());
                    jsonSong.put("Source", s.getUri());

                    jsonSongs.put(jsonSong);
                }

                jsonArtist.put("Songs", jsonSongs);
                artists.put(jsonArtist);
            }

            obj.put("Artists", artists);
            obj.put("DeviceID", regId);
            return obj.toString();
        }
    }

    public interface RequestCallback {
        public void onSuccess(String partyId, String regId);
        public void onFailure();
        public Activity getActivity();
    }
}
