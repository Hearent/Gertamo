package com.gertamo.gertamo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LastContest extends Fragment {
    Context context;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Upload> options;
    FirebaseRecyclerAdapter<Upload, MyViewHolder> adapter;
    DatabaseReference databaseReference, DbReference_like, databaseReference_photo;
    FirebaseAuth fAuth;
    String userID;
    ArrayList<String> photo_list = new ArrayList<>();

    public LastContest() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_last_contest, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//        recyclerView = view.findViewById(R.id.recyclerview_lastContest);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//        fAuth = FirebaseAuth.getInstance();
//        userID = fAuth.getCurrentUser().getUid();
//        databaseReference = FirebaseDatabase.getInstance().getReference("Upload Photos");
//
//        ValueEventListener eventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    String name = ds.getKey();
//                    databaseReference_photo = databaseReference.child(name);
//                    ValueEventListener eventListener_photo = new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot_photo) {
//                            for (DataSnapshot ds2 : dataSnapshot_photo.getChildren()) {
//                                String id_photo = ds2.getKey();
//                                Toast.makeText(getContext(), id_photo, Toast.LENGTH_SHORT).show();
////                                options = new FirebaseRecyclerOptions.Builder<Upload>().setQuery(databaseReference_photo.child(id_photo), Upload.class).build();
////                                adapter = new FirebaseRecyclerAdapter<Upload, MyViewHolder>(options) {
////                                    @Override
////                                    protected void onBindViewHolder(MyViewHolder holder, int position, Upload model) {
////                                        if (model.getId() != null) {
////                                            Toast.makeText(getContext(), model.getImageUri(), Toast.LENGTH_SHORT).show();
////                                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(name).child(model.getId());
////                                            DbReference_like.keepSynced(true);
////                                            DbReference_like.addListenerForSingleValueEvent(new ValueEventListener() {
////                                                @Override
////                                                public void onDataChange(DataSnapshot snapshot) {
////                                                    if (snapshot.hasChild(userID)) {
////                                                        holder.textView.setTypeface(null, Typeface.BOLD);
////                                                        holder.textView.setTextColor(Color.parseColor("#038A31"));
////                                                    } else {
////                                                        holder.textView.setTypeface(null, Typeface.NORMAL);
////                                                        holder.textView.setTextColor(Color.parseColor("#000000"));
////                                                    }
////                                                }
////
////                                                @Override
////                                                public void onCancelled(DatabaseError error) {
////                                                }
////                                            });
////                                            holder.contest_name.setText(String.valueOf(model.getName()));
////                                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(name).child(model.getId());
////                                            DbReference_like.addValueEventListener(new ValueEventListener() {
////                                                @Override
////                                                public void onDataChange(@NonNull DataSnapshot snapshot_like) {
////                                                    holder.textView.setText(String.valueOf(snapshot_like.getChildrenCount() - 1));
////                                                }
////
////                                                @Override
////                                                public void onCancelled(@NonNull DatabaseError error) {
////
////                                                }
////                                            });
////                                            Glide.with(context).load(model.getImageUri()).into(holder.imageView);
////                                        }
////                                    }
////
////                                    @Override
////                                    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
////                                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
////                                        return new MyViewHolder(v);
////                                    }
////                                };
////                                adapter.startListening();
////                                recyclerView.setAdapter(adapter);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.d("TAG", databaseError.getMessage()); //Don't ignore potential errors!
//                        }
//
//                    };
//                    databaseReference_photo.addListenerForSingleValueEvent(eventListener_photo);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d("TAG", databaseError.getMessage()); //Don't ignore potential errors!
//            }
//        };
//        databaseReference.addListenerForSingleValueEvent(eventListener);

    }
}