package com.example.dustam.frag;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.example.dustam.parties.Artist;

public class ArtistFragment extends ListFragment {

    private static final String TAG = "ArtistFragment";

    private ArtistListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ArtistListener) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "Activity " + activity.toString() + " doesn't implement SongListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // request song
        Artist artist = (Artist) getListAdapter().getItem(position);

        Log.d(TAG, "Picked artist " + artist.toString());
        listener.artistPicked(artist);
    }

    public interface ArtistListener {
        public void artistPicked(Artist a);
    }
}
