package com.gertamo.gertamo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MoreInfo extends Fragment {
    Context context;
    Button button, button_send, ocena, message;
    EditText editText;
    String email, userID, text, f_line;
    FirebaseAuth fAuth;
    DatabaseReference databaseReference_user, databaseReferenceContestTitle, databaseReference_UP, databaseReference;
    BottomNavigationView bottomNavigationView;
    LinearLayout instagram, tik_tok, facebook, sklep;

    public MoreInfo() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moreinfo, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        text = null;
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getUid();
        ocena = view.findViewById(R.id.ocena);
        facebook = view.findViewById(R.id.facebook);
        instagram = view.findViewById(R.id.instagram);
        tik_tok = view.findViewById(R.id.tik_tok);
        sklep = view.findViewById(R.id.sklep);
        databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("email");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        editText = view.findViewById(R.id.editTextTextMultiLine);
        button = view.findViewById(R.id.logout);
        message = view.findViewById(R.id.message);
        button_send = view.findViewById(R.id.send_btn);
        ocena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.gertamo.gertamo")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.gertamo.gertamo")));
                }
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/GertamoApp")));
            }
        });
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/gertamo.app/")));
            }
        });
        tik_tok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@gertamo.app")));
            }
        });
        sklep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://gertamo.teetres.com/")));
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().matches("")) {
                    Toast.makeText(getContext(), "Nie wysyłaj nam pustego maila ;)", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            email = snapshot.getValue().toString();
                            final ProgressDialog dialog = new ProgressDialog(getContext());
                            dialog.setTitle("Wysyłam email");
                            dialog.setMessage("Proszę czekaj");
                            dialog.show();
                            Thread sender = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender("kazontest@gmail.com", "zaq1@WSX");
                                        sender.sendMail(email,
                                                editText.getText().toString(),
                                                "kazontest@gmail.com",
                                                "opinie@gertamo.pl");
                                        dialog.dismiss();
                                    } catch (Exception e) {
                                        Log.e("mylog", "Error: " + e.getMessage());
                                    }
                                }
                            });
                            sender.start();
                            Fragment mFragment = new DashboardFragment();
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).commit();
                            bottomNavigationView.getMenu().findItem(R.id.home_nav).setChecked(true);
                            Toast.makeText(getContext(), "Dziękujemy za opinię :)", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
        });
        databaseReferenceContestTitle = FirebaseDatabase.getInstance().getReference("Contest").child("title");
        databaseReferenceContestTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                f_line = snapshot.getValue().toString();
                databaseReference_UP = FirebaseDatabase.getInstance().getReference("Upload Photos").child(f_line);
                databaseReference.child("perm").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pom = "1";
                        if (pom.equals(snapshot.getValue().toString())) {
                            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            TextView tv = new TextView(getContext());
                            TextView tv_count = new TextView(getContext());
                            tv.setLayoutParams(lparams);
                            tv_count.setLayoutParams(lparams);
                            tv.setText("Liczba przesłanych zdjęć: ");
                            tv.setTextSize(20);
                            tv_count.setTextSize(20);
                            tv_count.setTypeface(Typeface.DEFAULT_BOLD);
                            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.photoCount);
                            databaseReference_UP.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        tv_count.setText("0");
                                    } else {
                                        tv_count.setText(String.valueOf(snapshot.getChildrenCount()));
                                        tv_count.setTextSize(20);
                                        tv_count.setTypeface(Typeface.DEFAULT_BOLD);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            linearLayout.addView(tv);
                            linearLayout.addView(tv_count);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Pop.class));
            }
        });
    }
}