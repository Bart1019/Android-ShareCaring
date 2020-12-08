package com.example.sharecaring.service;

import androidx.annotation.NonNull;

import com.example.sharecaring.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseService {
    private static String databaseOut;
    DataSnapshot dataSnapshot;
   // public String databaseOut;

    public static void writeNewUser(String userId, String fName, String lName, String email) {
        User user = new User(fName, lName, email);
        FirebaseDatabase.getInstance().getReference("Users").child(userId).setValue(user);
    }

    public static String loadDataFromUsers(final String field) {
       // final String[] databaseOutput = new String[1];
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        String path = "Users/" + userid + "/" + field;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

        /*ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseOutput[0] = snapshot.child(field).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //setDatabaseOutput(snapshot.getValue().toString());
                databaseOut = snapshot.getValue().toString();
                System.out.println("inside::::" + databaseOut);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return databaseOut;
    }



}
