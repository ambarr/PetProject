package com.example.dustam.frag;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.dustam.parties.Song;

import java.util.ArrayList;

public class SongFragment extends ListFragment {

    private static String TAG = "SongFragment";
    private String partyId;

    private SongListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
        partyId = getArguments().getString("partyId");

        ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(getActivity(),
                android.R.layout.simple_list_item_1, songs);

        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SongListener) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "Activity " + activity.toString() + " doesn't implement SongListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // request song
        Song song = (Song) getListAdapter().getItem(position);

        Log.d(TAG, "Requested song " + song.toString());
        listener.songRequested((Song) getListAdapter().getItem(position));
    }

    public interface SongListener {
        public void songRequested(Song s);
    }
}
