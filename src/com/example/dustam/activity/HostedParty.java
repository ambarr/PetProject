package com.example.dustam.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dustam.R;
import com.example.dustam.api.HostedPartyRequests;
import com.example.dustam.frag.ArtistFragment;
import com.example.dustam.frag.SongFragment;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.Song;
import com.example.dustam.util.PartyService;

import java.util.ArrayList;

public class HostedParty extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        SongFragment.SongListener, ArtistFragment.ArtistListener {

    private static String TAG = "HostedParty";

    private Menu actionMenu;
    private ListFragment artistFragment;
    private SongFragment songFragment;

    private HostedPartyRequests requestHelper;
    private PartyService partyService;
    private boolean isBound;

    private ArrayList<Artist> artists;
    private ArrayAdapter<Artist> artistAdapter;
    private String partyName, partyPassword, partyId, regId;
    private Artist currentArtist;
    private boolean artistActive;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Party service connected");
            partyService = ((PartyService.PartyBinder)service).getService();
            partyService.setArtistList(artists);
            partyService.setPartyId(partyId);
        }

        public void onServiceDisconnected(ComponentName className) {
            partyService = null;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hosted_party);

        Bundle extras = this.getIntent().getExtras();

        partyName = extras.getString("partyName");
        partyPassword = extras.getString("password");
        artists = new ArrayList<Artist>();

        artistActive = true;
        artistFragment = new ArtistFragment();
        getFragmentManager().beginTransaction().add(R.id.library_placeholder, artistFragment).commit();
        artistAdapter = new ArrayAdapter<Artist>(HostedParty.this,
                android.R.layout.simple_list_item_1, artists);

        requestHelper = new HostedPartyRequests();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.hosting_menu, menu);
        actionMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.cancel_party_action:
                endParty();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(artistActive) {
            endParty();
        } else {
            super.onBackPressed();
        }
    }

    public void endParty() {
        final TextView tv = new TextView(HostedParty.this);
        tv.setText(getString(R.string.confirm_end_party));

        // Ask the user if they want to end the party.
        // If the say yes, I guess we will.
        new AlertDialog.Builder(HostedParty.this)
                .setTitle(getString(R.string.menu_end_party))
                .setView(tv)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface aDialog, int aWhich) {
                                if(isBound) {
                                    unbindService(mConnection);
                                    stopService(new Intent(HostedParty.this, PartyService.class));
                                    isBound = false;
                                }

                                Intent intent = new Intent(HostedParty.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void libraryLoaded() {
        requestHelper.addParty(partyName, partyPassword, artists, new HostedPartyRequests.RequestCallback() {
            @Override
            public void onSuccess(final String partyId, final String regId) {
                HostedParty.this.partyId = partyId;
                HostedParty.this.regId = regId;

                artistFragment.setListAdapter(artistAdapter);

                // Need to spin off party service
                Intent intent = new Intent(HostedParty.this, PartyService.class);
                startService(intent);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                isBound = true;

            }

            @Override
            public void onFailure() {
                Toast.makeText(HostedParty.this,
                        getString(R.string.post_party_error), Toast.LENGTH_LONG).show();
            }

            @Override
            public Activity getActivity() {
                return HostedParty.this;
            }
        });
    }

    @Override
    public void songRequested(Song s) {
        artistActive = true;
        getFragmentManager().beginTransaction()
                .replace(R.id.library_placeholder, artistFragment)
                .commit();
        ArrayList<Song> songs = new ArrayList<Song>();
        songs.add(s);
        partyService.requestSongs(songs);
    }

    @Override
    public void artistPicked(Artist artist) {
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
    public void onConfigurationChanged(Configuration newConfig) {
        // I ain't gon do nuthin.
        super.onConfigurationChanged(newConfig);
    }

    /*** Query Stuffs below here ***/
    private static final Uri ARTIST_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Return a cursor loader for the query.
        return new CursorLoader(this, ARTIST_URI, PROJECTION, SELECTION, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // data should have all the music we want at this point.
        data.moveToFirst();
        while(data.moveToNext()) {
            Song song = new Song();
            song.setName(data.getString(3));
            song.setUri(data.getString(4));
            song.setAlbumName(data.getString(2));
            song.setArtistName(data.getString(1));

            boolean found = false;
            for(Artist artist : artists) {
                if(artist.getArtistName().equals(song.getArtistName())) {
                    artist.getSongs().add(song);
                    found = true;
                }
            }

            if(!found) {
                Artist artist = new Artist();
                artist.setArtistName(song.getArtistName());
                artist.getSongs().add(song);
                artists.add(artist);
            }
        }

        this.libraryLoaded();
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        // Android will clean up our cursor. Thanks, Android.
    }
    /*** End query stuffs ***/
}