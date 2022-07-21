package com.gertamo.gertamo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView imageView;
    TextView textView, contest_name;
    public MyViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.DImage);
        textView = itemView.findViewById(R.id.DText);
        contest_name = itemView.findViewById(R.id.contest_name_tv);
    }
}
