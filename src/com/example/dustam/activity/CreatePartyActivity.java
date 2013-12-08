package com.example.dustam.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.dustam.R;

public class CreatePartyActivity extends Activity implements View.OnClickListener {

    private static String TAG = "CreatePartyActivity";

    private EditText nameField, passwordField;
    private Button createPartyBtn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_party);

        nameField = (EditText) findViewById(R.id.party_name);
        passwordField = (EditText) findViewById(R.id.party_password);

        createPartyBtn = (Button) findViewById(R.id.create_btn);
        createPartyBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id) {
            case R.id.create_btn:
                String name = nameField.getText().toString();
                String password = passwordField.getText().toString();

                if(name.isEmpty()) {
                    Toast.makeText(this, R.string.invalid_party_name, Toast.LENGTH_LONG).show();
                    break;
                }

                Intent intent = new Intent(CreatePartyActivity.this, HostedParty.class);

                Bundle bundle = new Bundle();
                bundle.putString("partyName", name);
                bundle.putString("password", password);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                Log.e(TAG, "what the what?");
                break;
        }
    }
}