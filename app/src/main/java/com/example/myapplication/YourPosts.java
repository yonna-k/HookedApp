package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
 * Where all the user's posts are displayed (in a grid view)
 * Done with Firebase Storage and Realtime Database
 */
public class YourPosts extends AppCompatActivity {
    View decorView;
    ImageButton home_button, saved_button;
    private GridView gridView;
    private ArrayList<DataClass> dataList;
    private YourPostsAdapter adapter;
    FirebaseUser user;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.your_posts);
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
        saved_button = findViewById(R.id.saved_button);
        saved_button.setOnClickListener(v -> {
            //go to the 'saved posts' page
            Intent intent = new Intent(getApplicationContext(), Saved.class);
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

        gridView = findViewById(R.id.grid_view);
        dataList = new ArrayList<DataClass>();
        adapter = new YourPostsAdapter(this, dataList);

        gridView.setAdapter(adapter);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //when new post uploaded
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    if (user.getUid().equals(dataClass.getUid()))
                    {
                        //only adds to the datalist if no duplicates and the uid matches the current user's
                        boolean found = false;
                        for (DataClass d : dataList)
                        {
                            if (dataClass.getKey().equals(d.getKey()))
                            {
                                found = true;
                            }
                        }
                        if (!found)
                        {
                            dataList.add(dataClass);
                        }

                    }
                }
                adapter.setOnItemClickListener(new YourPostsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        //delete button clicked -> deleted from firebase and the gridview
                        DataClass d = dataList.get(position);
                        String delete_key = d.getKey();
                        String url = d.getImageURL();
                        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                        databaseReference.child(delete_key).removeValue((error, ref) -> {
                            dataList.remove(position);
                            adapter.notifyDataSetChanged();
                        });
                        reference.delete();
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