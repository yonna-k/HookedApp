package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Adapter class used to control the recycler view in the class 'Home'
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<DataClass> dataList;
    private Context context;

    onItemClickListener mListener;

    StorageReference storageReference;

    /**
     * interface used to implement click methods in the 'Home' class
     */
    interface onItemClickListener{
        //if the pattern button is clicked
        void onItemClick(int position);
        //if the save button is clicked
        void onSaveClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener clickListener)
    {
        mListener = clickListener;
    }

    public MyAdapter(ArrayList<DataClass> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //displays image
        Glide.with(context).load(dataList.get(position).getImageURL()).into(holder.recyclerImage);
        //displays title
        holder.recyclerTitle.setText(dataList.get(position).getTitle());
        //displays username
        holder.username_home.setText(dataList.get(position).getUsername());
        try {
            //displays profile pic
            getProfilePic(position, holder.pfp_recycler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * retrieves and displays the profile picture of the user who posted
     * @param position - of the post in the datalist
     * @param imageView - where the pfp will be displayed
     * @throws IOException
     */
    private void getProfilePic(int position, ImageView imageView) throws IOException {
        //gets user's pfp from firebase storage using their uid
        storageReference = FirebaseStorage.getInstance().getReference().child("Users/" + dataList.get(position).getUid());
        File localFile = File.createTempFile("tempImg", "png");
        storageReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "PFP Error", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView recyclerImage;
        TextView recyclerTitle;
        ImageButton pattern_button;
        ImageButton saved_button;
        ImageView pfp_recycler;
        TextView username_home;
        public MyViewHolder(@NonNull View itemView, onItemClickListener clickListener)
        {
            super(itemView);

            recyclerImage = itemView.findViewById(R.id.recyclerimage);
            recyclerTitle = itemView.findViewById(R.id.recyclertitle);
            pattern_button = itemView.findViewById(R.id.pattern_button);
            pfp_recycler = itemView.findViewById(R.id.pfp_recycler);
            username_home = itemView.findViewById(R.id.username_home);
            saved_button = itemView.findViewById(R.id.save_button);
            {
                //sets on click listeners to the interface methods
                pattern_button.setOnClickListener(v -> {
                    clickListener.onItemClick(getAdapterPosition());
                });
                saved_button.setOnClickListener(v -> {
                    clickListener.onSaveClick(getAdapterPosition());
                });
            }
        }
    }
}