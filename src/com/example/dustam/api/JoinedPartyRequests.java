package com.example.dustam.api;

import android.os.AsyncTask;
import android.util.Log;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;
import com.example.dustam.util.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class JoinedPartyRequests {

    private static String TAG = "JoinedPartyAPIRequests";

    public void requestSong(Song song, Artist artist, String partyId, RequestCallback callback) {
        new RequestSongTask(song, artist.getArtistName(), partyId, callback).execute();
    }

    private class RequestSongTask extends AsyncTask<Void, Void, Boolean> {

        Song song;
        String artistName;
        String partyId;
        RequestCallback callback;

        public RequestSongTask(Song song, String artistName, String partyId, RequestCallback cb) {
            this.song = song;
            this.artistName = artistName;
            this.partyId = partyId;
            this.callback = cb;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.REQUEST_SONGS_URL);

            try {
                JSONObject obj = new JSONObject();
                obj.put("Title", song.getName());
                obj.put("ArtistName", artistName);
                JSONArray arr = new JSONArray();
                arr.put(obj);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("hostID", partyId);
                jsonBody.put("songNames", arr);

                post.setEntity(new StringEntity(jsonBody.toString()));
                post.setHeader("Content-Type", "application/json");

                HttpResponse response = client.execute(post);
                StatusLine status = response.getStatusLine();
                response.getEntity().consumeContent();

                if(status.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
                else {
                    Log.e(TAG, "Error code when requesting songs: " + status.getStatusCode());
                    return false;
                }
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean res) {
            if(res) {
                callback.onSuccess();
            } else {
                Log.e(TAG, "Failure requesting song");
                callback.onFailure();
            }
        }
    }

    public interface RequestCallback {
        public void onSuccess();
        public void onFailure();
    }
}
