package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.Offer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddOfferActivity extends AppCompatActivity {

    private CheckBox checkBoxAnimals, checkBoxShopping, checkBoxMedication, checkBoxTransport, checkBoxEverything;
    private EditText editTextAddress, editTextDescription;
    private Button btnAddNewOffer;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);
        checkBoxAnimals = (CheckBox)findViewById(R.id.checkBoxAnimals);
        checkBoxShopping = (CheckBox)findViewById(R.id.checkBoxFoodShopping);
        checkBoxMedication = (CheckBox)findViewById(R.id.checkBoxMedication);
        checkBoxTransport = (CheckBox)findViewById(R.id.checkBoxTransport);
        checkBoxEverything = (CheckBox)findViewById(R.id.checkBoxEverything);
        editTextAddress = (EditText)findViewById(R.id.editTextAddress);
        editTextDescription = (EditText)findViewById(R.id.editTextDescription);
        btnAddNewOffer = (Button)findViewById(R.id.btnAddNewOffer);
        btnAddNewOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewOffer();
            }
        });
    }

    private void addNewOffer() {
        boolean animals = checkBoxAnimals.isChecked();
        boolean shopping = checkBoxShopping.isChecked();
        boolean medication = checkBoxMedication.isChecked();
        boolean transport = checkBoxTransport.isChecked();

        if(checkBoxEverything.isChecked()) {
            animals = true;
            shopping = true;
            medication = true;
            transport = true;
        }

        final String description = editTextDescription.getText().toString().trim();
        final String address = editTextAddress.getText().toString().trim();

        Offer offer = new Offer(description, address, animals, shopping, medication, transport, false);
        FirebaseDatabase.getInstance().getReference("Offers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                //.push() allows to save data without overriding
                .push().setValue(offer).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(AddOfferActivity.this, "Offer has been created", Toast.LENGTH_LONG).show();
                    IntentOpener.openIntent(AddOfferActivity.this, MapsActivity.class);
                }else {
                    Toast.makeText(AddOfferActivity.this, "Failed to create offer", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}