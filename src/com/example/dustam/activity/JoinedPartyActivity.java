package com.example.dustam.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dustam.R;
import com.example.dustam.api.JoinedPartyRequests;
import com.example.dustam.frag.ArtistFragment;
import com.example.dustam.frag.SongFragment;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;

import java.util.ArrayList;

public class JoinedPartyActivity extends Activity implements SongFragment.SongListener, ArtistFragment.ArtistListener {

    private ArrayList<Artist> partyLibrary;

    private ArrayAdapter<Artist> artistAdapter;
    private ArrayAdapter<Song> songAdapter;

    private ListFragment artistFragment, songFragment;
    private Menu actionMenu;

    private JoinedPartyRequests requestHelper;

    private Artist currentArtist;
    private String partyId;
    private boolean artistActive;

    private static String TAG = "JoinedPartyActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joined_party);

        partyLibrary = getIntent().getParcelableArrayListExtra("library");
        partyId = getIntent().getStringExtra("partyId");
        Log.d(TAG, partyLibrary.toString());

        requestHelper = new JoinedPartyRequests();

        artistAdapter = new ArrayAdapter<Artist>(this, android.R.layout.simple_list_item_1);
        artistAdapter.addAll(partyLibrary);
        songAdapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_1);

        artistFragment = new ArtistFragment();
        artistActive = true;

        getFragmentManager().beginTransaction().add(R.id.library_placeholder, artistFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        artistFragment.setListAdapter(artistAdapter);
    }

    @Override
    public void songRequested(Song song) {
        requestHelper.requestSong(song, currentArtist, partyId,
                new JoinedPartyRequests.RequestCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(JoinedPartyActivity.this,
                        getString(R.id.request_succuess), Toast.LENGTH_SHORT).show();

                artistActive = true;
                artistFragment = new ArtistFragment();
                artistFragment.setListAdapter(artistAdapter);
                getFragmentManager().beginTransaction().replace(R.id.library_placeholder, artistFragment)
                        .setTransition(android.R.anim.slide_out_right)
                        .commit();
            }

            @Override
            public void onFailure() {
                Toast.makeText(JoinedPartyActivity.this,
                        getString(R.string.request_error), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void artistPicked(Artist artist) {
        currentArtist = artist;
        songFragment = new SongFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("songs", artist.getSongs());
        bundle.putString("partyId", partyId);
        bundle.putString("artistName", artist.getArtistName());
        songFragment.setArguments(bundle);

        artistActive = false;
        getFragmentManager().beginTransaction().replace(R.id.library_placeholder, songFragment)
                .setTransition(android.R.anim.slide_in_left)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.hosting_menu, menu);
        actionMenu = menu;
        return true;
    }

    @Override
    public void onBackPressed() {
        if(artistActive) {
            leaveParty();
        } else {
            artistActive = true;
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.cancel_party_action:
                leaveParty();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void leaveParty() {
        final TextView tv = new TextView(JoinedPartyActivity.this);
        tv.setText(getString(R.string.confirm_leave_party));

        // Ask the user if they want to end the party.
        // If the say yes, I guess we will.
        new AlertDialog.Builder(JoinedPartyActivity.this)
                .setTitle(getString(R.string.menu_leave_party))
                .setView(tv)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface aDialog, int aWhich) {
                                Intent intent = new Intent(JoinedPartyActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}