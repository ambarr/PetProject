package com.example.dustam.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.example.dustam.R;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class PartyService extends Service implements MediaPlayer.OnCompletionListener {

    private static String TAG = "PartyService";

    private String mPartyID;

    private ArrayList<Artist> mArtistList;
    private Location mLocation;
    private SongQueue         mSongQueue;
    private MediaPlayer mPlayer;
    private String            mPartyName,
            mPartyPassword;
    private Song mCurrentSong;

    private Random            mRandom;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.NOTIFY_HOST)) {
                Log.d(TAG, "Requests available on server");
                new GetRequestsTask().execute();
            }
            else {
                Log.e(TAG, "Unhandled action received by mReceiver: " + action);
            }
        }
    };

    @Override
    public void onCreate() {
        //Register broadcast receiver to handle messages from server
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.NOTIFY_HOST);
        registerReceiver(mReceiver, filter);

        mArtistList = new ArrayList<Artist>();
        mSongQueue = new SongQueue();
        mRandom = new Random();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        new EndPartyTask().execute(new String[0]);
        Toast.makeText(this, getText(R.string.party_service_stopped), Toast.LENGTH_SHORT).show();

        if(mPlayer != null)
            mPlayer.stop();
        mPlayer = null;
    }

    public void setArtistList(ArrayList<Artist> list) {
        Log.i(TAG, "Set artist list");
        mArtistList = list;
        Collections.sort(mArtistList, new ArtistComparator());

        startPlaying();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        playSong();
    }

    public void playSong() {
        Song nextSong = mSongQueue.getNextSong();

        if(nextSong == null) {
            // Get a random song to play
            int rand = mRandom.nextInt(mArtistList.size());
            Artist randArtist = mArtistList.get(rand);
            rand = mRandom.nextInt(randArtist.getSongs().size());
            nextSong = randArtist.getSongs().get(rand);
        }

        mCurrentSong = nextSong;

        Log.i(TAG, "Playing next song");
        try {
            mPlayer.reset();
            mPlayer.setDataSource(nextSong.getUri());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestSongs(ArrayList<Song> songTitles) {
        mSongQueue.addAll(songTitles);
        Log.d(TAG, "Updated queue: " + mSongQueue);
    }

    public void setPartyId(String partyId) {
        this.mPartyID = partyId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playSong();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new PartyBinder();

    public class PartyBinder extends Binder {
        public PartyService getService() {
            // Return this instance of PartyService so clients can call public methods
            return PartyService.this;
        }
    }

    private class SongQueue {
        ArrayList<Song> songQueue;
        Comparator<Song> songComparator;

        public SongQueue() {
            songComparator = new SongComparator();
            songQueue = new ArrayList<Song>();
        }

        public void addRequest(Song song) {
            int index = songQueue.indexOf(song);
            if(index > -1) {
                songQueue.get(index).request();
            }
            else {
                songQueue.add(song);
            }

            Collections.sort(songQueue, songComparator);
        }

        public void addAll(ArrayList<Song> requests) {
            for(Song song : requests) {
                int index = songQueue.indexOf(song);
                if(index > -1) {
                    songQueue.get(index).request();
                }
                else {
                    song.request();
                    if(song.getUri() == null) {
                        //Find this song in the party library for the source path
                        int artistIndex = -1, songIndex = -1;
                        for(int i = 0; i < mArtistList.size(); i++) {
                            if(mArtistList.get(i).getArtistName().equals(song.getArtistName())) {
                                artistIndex = i;
                                break;
                            }
                        }

                        if(artistIndex == -1) {
                            Log.e(TAG, "Received song request w/ invalid artist name: " + song.getArtistName());
                            return;
                        }

                        ArrayList<Song> songList = mArtistList.get(artistIndex).getSongs();
                        for(int i = 0; i < songList.size(); i++) {
                            if(songList.get(i).getName().equals(song.getName())) {
                                songIndex = i;
                                break;
                            }
                        }

                        if(songIndex == -1) {
                            Log.e(TAG, "Received song request w/ invalid title: " + song.getName());
                            return;
                        }

                        String source = mArtistList.get(artistIndex).getSongs()
                                .get(songIndex).getUri();
                        song.setUri(source);
                    }
                    songQueue.add(song);
                }
            }
            Collections.sort(songQueue, songComparator);
        }

        public Song getNextSong() {
            if(songQueue.isEmpty())
                return null;

            Song next = songQueue.remove(0);
            next.clearRequests();
            return next;
        }

        @Override
        public String toString() {
            String ret = "{";
            for(Song song : songQueue)
                ret = ret + song.getName() + ":" + song.numRequests() + ", ";
            ret = ret + "}";
            return ret;
        }

        private class SongComparator implements Comparator<Song> {
            @Override
            public int compare(Song s1, Song s2) {
                if(s1.numRequests() < s2.numRequests()) {
                    return 1;
                }
                else if(s1.numRequests() > s2.numRequests()) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
    }

    private class ArtistComparator implements Comparator<Artist> {
        @Override
        public int compare(Artist a1, Artist a2) {
            return a1.getArtistName().compareTo(a2.getArtistName());
        }
    }

    private class GetRequestsTask extends AsyncTask<Void, Void, String> {
        @Override
        public String doInBackground(Void... params) {
            String res = "";
            byte[] bytes;

            String url = Constants.GET_REQUESTS_URL + mPartyID;
            url = url.replaceAll("[^A-Za-z0-9_./:-]", "");

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);

            try {
                HttpResponse response = client.execute(get);
                StatusLine status = response.getStatusLine();
                final HttpEntity entity = response.getEntity();
                if(status.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    bytes = EntityUtils.toByteArray(entity);
                    res = new String(bytes, "UTF-8");
                }
                else {
                    Log.e(TAG, "Couldn't get song requests from server. Error code: " + status.getStatusCode());
                }

                entity.consumeContent();
            } catch(Exception e) {
                Log.e(TAG, "Exception caught while getting song requests: " + e.toString());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONArray json = new JSONArray(res);
                ArrayList<Song> songs = new ArrayList<Song>();
                for(int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String title = obj.getString("Title");
                    String artist = obj.getString("ArtistName");
                    Song s = new Song();
                    s.setName(title);
                    s.setArtistName(artist);
                    songs.add(s);
                }
                PartyService.this.requestSongs(songs);
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class EndPartyTask extends AsyncTask<String, String, String> {
        @Override
        public String doInBackground(String... params) {
            String url = Constants.END_PARTY_URL + mPartyID;
            url = url.replaceAll("[^A-Za-z0-9_./:-]", "");

            HttpClient client = new DefaultHttpClient();
            HttpDelete req = new HttpDelete(url);

            try {
                HttpResponse response = client.execute(req);
                StatusLine status = response.getStatusLine();
                while(status.getStatusCode() != HttpURLConnection.HTTP_OK) {
                    response = client.execute(req);
                    status = response.getStatusLine();
                }
                response.getEntity().consumeContent();
            } catch(Exception e) {
                Log.e(TAG, "Exception during End Party: " + e.toString());
            }

            return "end";
        }

        @Override
        protected void onPostExecute(String res) {
            Log.d(TAG, "Party removed from server");
        }
    }
}
