package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Adapter class for the 'Saved' class. Implemented as a GridView
 */
public class SavedAdapter extends BaseAdapter {
    private ArrayList<DataClass> dataList;
    private Context context;
    LayoutInflater layoutInflater;
    private OnItemClickListener listener;
    private StorageReference storageReference;

    public interface OnItemClickListener{
        //when the remove button is clicked
        void onItemClick(int position);
    }

    public SavedAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setOnItemClickListener(SavedAdapter.OnItemClickListener l){
        listener = l;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (layoutInflater == null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = layoutInflater.inflate(R.layout.grid_item_saved, null);
        }

        ImageView gridimage = view.findViewById(R.id.gridimage_saved);
        TextView gridtitle = view.findViewById(R.id.gridtitle_saved);
        ImageView pfp_saved = view.findViewById(R.id.pfp_saved);
        TextView username_saved = view.findViewById(R.id.username_saved);
        ImageButton saved_remove = view.findViewById(R.id.removebutton_saved);
        saved_remove.setOnClickListener(new View.OnClickListener() {
            //sets the on click listener to the interface method
            @Override
            public void onClick(View v) {
                listener.onItemClick(i);
            }
        });
        ImageButton imageButton = view.findViewById(R.id.pattern_button_saved);
        imageButton.setOnClickListener(new View.OnClickListener() {
            //when the pattern button is clicked
            @Override
            public void onClick(View v) {
                inflateDialog(i);
            }
        });
        //post image
        Glide.with(context).load(dataList.get(i).getImageURL()).into(gridimage);
        //post title
        gridtitle.setText(dataList.get(i).getTitle());
        //post username
        username_saved.setText(dataList.get(i).getUsername());
        try {
            //displays pfp of user who posted
            getProfilePic(i, pfp_saved);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return view;
    }

    /**
     * retrieves and displays the user's pfp from storage
     * @param position - of the post in the datalist
     * @param imageView
     * @throws IOException
     */
    private void getProfilePic(int position, ImageView imageView) throws IOException { //retrieves and displays pfp from storage
        storageReference = FirebaseStorage.getInstance().getReference().child("Users/" + dataList.get(position).getUid());
        File localFile = File.createTempFile("tempImg", "png");
        storageReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "PFP Error", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * displays the custom dialog that shows the pattern
     * @param position - of the post in the datalist
     */
    private void inflateDialog(int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        //inflates custom dialog
        View view = inflater.inflate(R.layout.saved_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.saved_pattern);
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
}
