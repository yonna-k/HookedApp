package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Controls all activity related to the profile page, where the user can change
 * their profile picture.
 * Done with Firebase Storage.
 */
public class Profile extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    Button log_out;
    Button done;
    TextView display;
    ImageButton profile_pic;
    StorageReference storageReference;
    String name;
    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);
        //displays content in full screen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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
        log_out = findViewById(R.id.log_out);
        display = findViewById(R.id.textView3);
        done = findViewById(R.id.done);
        profile_pic = findViewById(R.id.profile_pic_profile);

        if (user == null)
        {
            //no-one logged in -> redirected to log in page
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
        {
            //displays username
            String display_name = user.getEmail().replace("@example.com", "");
            display.setText(display_name);
            try {
                //displays pfp
                getProfilePic();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logs the user out
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (name != null)
                    {
                        //updates pfp
                        updateProfilePic(name);//save image to storage
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

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

    /**
     * retrieves and displays the user's pfp from storage
     * @throws IOException
     */
    private void getProfilePic() throws IOException {
        storageReference = FirebaseStorage.getInstance().getReference().child("Users/" + user.getUid());
        File localFile = File.createTempFile("tempImg", "png");
        storageReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            profile_pic.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> {
            //default pfp displayed (+stored) if fails
            Uri imageUri = Uri.parse("android.resource://"+ getApplicationContext().getPackageName() + "/drawable/" + "star_pink");
            storageReference = FirebaseStorage.getInstance().getReference("Users/"+ auth.getCurrentUser().getUid());
            storageReference.putFile(imageUri).addOnFailureListener(e1 -> {
                Toast.makeText(getApplicationContext(), "Default PFP Error", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * the image that the user clicks (to choose their profile picture) is set as their new
     * profile picture
     * @param view
     */
    public void buttonClicked(View view) {
        ImageButton button = findViewById(view.getId());
        name = button.getResources().getResourceEntryName(view.getId());
        Drawable drawable = button.getDrawable();
        profile_pic.setImageDrawable(drawable);
    }

    /**
     * executed when the user already has a profile picture and wishes to change it
     * @param name - id of the image they want to update to
     * @throws IOException
     */
    private void updateProfilePic(String name) throws IOException {
        Uri imageUri = Uri.parse("android.resource://"+ getApplicationContext().getPackageName() + "/drawable/" + name);
        storageReference = FirebaseStorage.getInstance().getReference("Users/"+ auth.getCurrentUser().getUid());
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        });
    }

}