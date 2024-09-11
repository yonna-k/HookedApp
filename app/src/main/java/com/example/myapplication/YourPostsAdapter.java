package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Adapter class for the 'YourPosts' class. Implemented as a GridView
 */
public class YourPostsAdapter extends BaseAdapter {
    private ArrayList<DataClass> dataList;
    private Context context;
    LayoutInflater layoutInflater;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        //when the delete button is clicked
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener l){
        listener = l;
    }

    public YourPostsAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @Override
    public int getCount() {
        return dataList.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (layoutInflater == null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = layoutInflater.inflate(R.layout.grid_item_yourposts, null);
        }

        ImageView gridimage = view.findViewById(R.id.gridimage);
        TextView gridtitle = view.findViewById(R.id.gridtitle);
        ImageButton delete = view.findViewById(R.id.delete_button);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sets delete button's on click method to the interface method
                listener.onItemClick(i);
            }
        });
        ImageButton imageButton = view.findViewById(R.id.pattern_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when the pattern button is clicked
                inflateDialog(i);
            }
        });
        //display post image
        Glide.with(context).load(dataList.get(i).getImageURL()).into(gridimage);
        //display post title
        gridtitle.setText(dataList.get(i).getTitle());

        return view;
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
        View view = inflater.inflate(R.layout.yourposts_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.your_posts_pattern);
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
