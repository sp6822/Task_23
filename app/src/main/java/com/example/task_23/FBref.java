package com.example.task_23;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class FBref {
    public static final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.EXPENSES_NODE);
}