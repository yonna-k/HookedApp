package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Class controls activity related to uploading a post.
 * Done with Firebase Realtime Database and Storage.
 */
public class UploadActivity extends AppCompatActivity {

    View decorView;
    ImageButton cross_button, plus_photo, upload_button;
    ImageView image;
    EditText title_text;
    private Uri imageUri;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressBar progressBar;
    ImageButton plusPattern;
    String pattern = "";
    TextView upload_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.upload_activity);
        //displays content in full screen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cross_button = findViewById(R.id.cross_button);
        cross_button.setOnClickListener(v -> {
            //cancels activity - redirected to home page
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (visibility == 0)
            {
                decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        plus_photo = findViewById(R.id.plusphoto);
        upload_button = findViewById(R.id.upload_button);
        image = findViewById(R.id.uploaded_pic);
        title_text = findViewById(R.id.title_text);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        plusPattern = findViewById(R.id.pluspattern);
        upload_text = findViewById(R.id.upload_text);

        //controls the photo selection from the user's photos
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        image.setImageURI(imageUri);
                    }
                    else
                    {
                        Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        plus_photo.setOnClickListener(v -> {
            //when the button clicked, user is able to select photo
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        plusPattern.setOnClickListener(v -> {
            //when the button is clicked, user is able to upload a pattern (as text)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.edit_text_dialog, null);
            EditText editText = (EditText) view.findViewById(R.id.pattern_text);
            if (pattern != null)
            {
                editText.setText(pattern);
            }
            Button done_dialog = (Button) view.findViewById(R.id.done_dialog);
            Button cancel_dialog = (Button) view.findViewById(R.id.cancel_dialog);
            builder.setView(view);
            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
            done_dialog.setOnClickListener(v1 -> {
                //when 'done' clicked on pattern dialog
                pattern = editText.getText().toString();
                if (!pattern.trim().equalsIgnoreCase(""))
                {
                    upload_text.setText("UPLOADED");
                }
                else
                {
                    upload_text.setText("NOT UPLOADED");
                }
                alert.dismiss();
            });
            cancel_dialog.setOnClickListener(v12 -> {
                //if 'cancel' button clicked on pattern dialog
                alert.dismiss();
            });
        });

        upload_button.setOnClickListener(v -> {
            //when the user clicks 'upload'
            if (imageUri == null)
            {
                Toast.makeText(UploadActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
            }
            else if (title_text.getText().toString().trim().equalsIgnoreCase(""))
            {
                Toast.makeText(UploadActivity.this, "Enter Title", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //uploads to firebase
                uploadToFirebase(imageUri);
            }
        });


    }

    /**
     * uploads the user's post to firebase so that it is displayed in the app
     * @param uri - of the selected image
     */
    private void uploadToFirebase(Uri uri)
    {
        String title = title_text.getText().toString();
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        imageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            imageReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                String key = databaseReference.push().getKey();
                DataClass dataClass = new DataClass(title, uri1.toString(), user.getUid(), pattern, key, user.getEmail().replace("@example.com", ""));
                databaseReference.child(key).setValue(dataClass);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Post Uploaded", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //after upload, redirected back to home page
                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);
            });
        }).addOnProgressListener(snapshot -> {
            progressBar.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        });
    }

    private String getFileExtension(Uri fileUri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
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