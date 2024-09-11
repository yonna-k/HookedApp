package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Where all the posts saved by the user are displayed (in a grid view).
 * Done with Firebase Storage and Realtime Database.
 */
public class Saved extends AppCompatActivity {

    View decorView;
    ImageButton home_button, your_posts_button;

    private DatabaseReference databaseReference;
    FirebaseUser user;
    private SavedAdapter adapter;
    private ArrayList<DataClass> dataList;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.saved);
        //displays content in full screen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        home_button = findViewById(R.id.home_button);
        home_button.setOnClickListener(v -> {
            //go back to the home page
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        });
        your_posts_button = findViewById(R.id.your_posts_button);
        your_posts_button.setOnClickListener(v -> {
            //go to the 'your posts' page
            Intent intent = new Intent(getApplicationContext(), YourPosts.class);
            startActivity(intent);
            finish();
        });

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (visibility == 0)
            {
                decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        //specific branch for each user
        databaseReference = FirebaseDatabase.getInstance().getReference("Saved" + user.getUid());
        gridView = findViewById(R.id.grid_view_saved);
        dataList = new ArrayList<>();
        adapter = new SavedAdapter(this, dataList);
        gridView.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //when new post is added to the database
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    boolean found = false;
                    for (DataClass d : dataList)
                    {
                        if (dataClass.getKey().equals(d.getKey()))
                        {
                            //only adds to the datalist if there are no duplicates
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        dataList.add(dataClass);
                    }
                }
                adapter.setOnItemClickListener(new SavedAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        //when the remove button clicked, remove from saved
                        DataClass d = dataList.get(position);
                        String delete_key = d.getKey();

                        //removes from database
                        databaseReference.child(delete_key).removeValue((error, ref) -> {
                            dataList.remove(position);
                            adapter.notifyDataSetChanged();
                        });

                    }
                });
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }
    private int hideSystemBars()
    {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
}