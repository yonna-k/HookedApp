package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Home page where all posts are displayed and user is able to save/view the pattern of posts
 */
public class Home extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    ImageButton profile_pic, savedButton, posts_button, public_posts;
    StorageReference storageRetrieve;
    StorageReference storageReference;
    View decorView;
    RecyclerView recyclerView;
    ArrayList<DataClass> dataList;
    MyAdapter adapter;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    private DatabaseReference saved_reference;


    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        //makes the display full screen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //recycler view used for UI
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<DataClass>();
        adapter = new MyAdapter(dataList, this);
        recyclerView.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
                adapter.setOnItemClickListener(new MyAdapter.onItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        //when the pattern button clicked, display pattern as dialog
                        inflateDialog(position);
                    }

                    @Override
                    public void onSaveClick(int position) {
                        //when save button clicked, add post to separate branch in realtime database
                        uploadToFirebase(position);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (visibility == 0)
            {
                decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        savedButton = findViewById(R.id.saved_button);
        savedButton.setOnClickListener(v -> {
            //takes user to 'saved posts' page
            Intent intent = new Intent(getApplicationContext(), Saved.class);
            startActivity(intent);
            finish();
        });

        posts_button = findViewById(R.id.your_posts_button);
        posts_button.setOnClickListener(v -> {
            //takes user to 'your posts' page
            Intent intent = new Intent(getApplicationContext(), YourPosts.class);
            startActivity(intent);
            finish();
        });

        profile_pic = findViewById(R.id.profile_pic_profile);
        try {
            getProfilePic();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        profile_pic.setOnClickListener(v -> {
            //takes user to 'change profile picture' page
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();
        });

        public_posts = findViewById(R.id.public_post);
        public_posts.setOnClickListener(v -> {
            //takes user to 'upload post' page
            Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
            startActivity(intent);
            finish();
        });

        //gets the branch from the database where saved posts are stored (specific to each user)
        saved_reference = FirebaseDatabase.getInstance().getReference("Saved" + user.getUid());

    }

    /**
     * uploads the user's post to the firebase realtime database
     * @param position - of the post in the datalist
     */
    private void uploadToFirebase(int position)
    {
        String title = dataList.get(position).getTitle();
        String uri1 = dataList.get(position).getImageURL();
        String uid = dataList.get(position).getUid();
        String username = dataList.get(position).getUsername();
        String pattern = dataList.get(position).getPattern();
        String key = saved_reference.push().getKey();
        DataClass dataClass = new DataClass(title, uri1, uid, pattern, key, username);
        //adds post to database under a unique key
        saved_reference.child(key).setValue(dataClass).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), "Post Saved", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //back to home page
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        }, 1000);


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

    /**
     * displays the custom dialog that shows the pattern
     * @param position - of the post in the datalist
     */
    private void inflateDialog(int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        //inflates custom dialog
        View view = inflater.inflate(R.layout.pattern_reading_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.pattern_view_text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        String pattern = dataList.get(position).getPattern();
        if ((pattern == null) || (pattern.trim().equalsIgnoreCase("")))
        {
            //if empty string uploaded, assumed no pattern uploaded
            textView.setText("N/A");
        }
        else
        {
            textView.setText(pattern);
        }
        textView.setTextIsSelectable(false);
        textView.measure(-1, -1);//you can specific other values.
        textView.setTextIsSelectable(true);
        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();
    }

    /**
     * retrieves and displays profile picture from firebase storage
     * @throws IOException
     */
    private void getProfilePic() throws IOException {
        storageRetrieve = FirebaseStorage.getInstance().getReference().child("Users/" + user.getUid());
        File localFile = File.createTempFile("tempImg", "png");
        storageRetrieve.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            profile_pic.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> {
            //if it fails, default pfp is displayed (+uploaded to storage)
            Uri imageUri = Uri.parse("android.resource://"+ getApplicationContext().getPackageName() + "/drawable/" + "star_pink");
            storageReference = FirebaseStorage.getInstance().getReference("Users/"+ auth.getCurrentUser().getUid());
            storageReference.putFile(imageUri).addOnFailureListener(e1 -> {
                Toast.makeText(getApplicationContext(), "Default PFP Error", Toast.LENGTH_SHORT).show();
            });
        });
    }
}