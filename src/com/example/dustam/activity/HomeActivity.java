package com.example.dustam.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.dustam.R;
import com.example.dustam.api.PartyRequests;
import com.example.dustam.parties.Artist;
import com.example.dustam.parties.NearbyPartyInfo;

import java.util.ArrayList;

public class HomeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainView";

    private ListView mNearbyListView;
    private ArrayList<NearbyPartyInfo> mNearbyParties;
    private ArrayAdapter<NearbyPartyInfo> mNearbyAdapter;

    private PartyRequests requestHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        requestHelper = new PartyRequests();

        mNearbyParties = new ArrayList<NearbyPartyInfo>();

        mNearbyListView = (ListView) findViewById(R.id.nearby_listview);
        mNearbyAdapter = new ArrayAdapter<NearbyPartyInfo>(this,
                android.R.layout.simple_list_item_1);

        mNearbyListView.setAdapter(mNearbyAdapter);
        mNearbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg) {
                NearbyPartyInfo info = mNearbyParties.get(position);
                if(info.hasPassword()) {

                } else {
                    joinPartyClicked(info);
                }
            }
        });

        populateNearbyList();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.create_party_btn:
                Intent intent = new Intent(HomeActivity.this, CreatePartyActivity.class);
                startActivity(intent);
                break;
            default:
                Log.d(TAG, "Error in on click, received click from " + v.getId());
                break;
        }
    }

    private void populateNearbyList() {
        requestHelper.getNearbyParties(new PartyRequests.NearbyPartyCallback() {
            @Override
            public void onSuccess(ArrayList<NearbyPartyInfo> result) {
                mNearbyParties.clear();
                mNearbyAdapter.clear();
                mNearbyParties = result;
                mNearbyAdapter.addAll(mNearbyParties);
            }

            @Override
            public void onFailure() {
                Toast.makeText(HomeActivity.this, getString(R.string.error_nearby),
                        Toast.LENGTH_LONG);
            }
        });
    }

    private void joinPartyClicked(final NearbyPartyInfo info) {
        if(info.hasPassword()) {
            final EditText password = new EditText(this);
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(getString(R.string.enter_password))
                    .setView(password)
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int id) {
                                    joinParty(password.getText().toString(), info);
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
        else {
            final TextView tv = new TextView(this);
            tv.setText(getString(R.string.confirm_join_party));
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(getString(R.string.join_party_dialog))
                    .setView(tv)
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int id) {
                                    joinParty(null, info);
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void joinParty(String passwordAttempt, NearbyPartyInfo info) {
        requestHelper.joinParty(info.getPartyId(), passwordAttempt, joinCallback);
    }

    private PartyRequests.JoinPartyCallback joinCallback = new PartyRequests.JoinPartyCallback() {
        @Override
        public void onSuccess(ArrayList<Artist> result) {
            Intent intent = new Intent(HomeActivity.this, JoinedPartyActivity.class);
            intent.putParcelableArrayListExtra("library", result);
            startActivity(intent);
        }

        @Override
        public void onFailure() {
            Toast.makeText(HomeActivity.this, getString(R.string.error_join),
                    Toast.LENGTH_LONG);
        }
    };
}