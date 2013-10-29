package com.example.dustam.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import com.example.dustam.R;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;

import java.util.ArrayList;

public class JoinedPartyActivity extends Activity {

    private ArrayList<Artist> partyLibrary;

    private ArrayAdapter<Artist> artistAdapter;
    private ArrayAdapter<Song> songAdapter;

    private static float MAX_NP_WEIGHT = 4;
    private static float MIN_NP_WEIGHT = 1;

    float nowPlayingWeight;

    private LinearLayout.LayoutParams nowPlayingLayoutParams;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        partyLibrary = getIntent().getParcelableArrayListExtra("library");

        artistAdapter = new ArrayAdapter<Artist>(this, android.R.layout.simple_list_item_1);
        artistAdapter.addAll(partyLibrary);
        songAdapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_1);

        ListFragment artistFragment = new ListFragment();
        final ListFragment songFragment = new ListFragment();

        artistFragment.getListView().setAdapter(artistAdapter);
        getFragmentManager().beginTransaction().add(R.id.library_placeholder, artistFragment).commit();

        artistFragment.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = partyLibrary.get(position);
                songAdapter.clear();
                songAdapter.addAll(artist.getSongs());
                getFragmentManager().beginTransaction().replace(R.id.library_placeholder, songFragment)
                        .setTransition(android.R.anim.slide_in_left)
                        .commit();
            }
        });


        songFragment.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}