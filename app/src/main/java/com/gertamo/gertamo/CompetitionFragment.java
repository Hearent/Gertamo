package com.gertamo.gertamo;

import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.graphics.Color;

import android.os.Bundle;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CompetitionFragment extends Fragment {
    Fragment mFragment = new CameraFragment();
    Context context;
    String f_line, userID, android_id;
    FirebaseAuth fAuth;
    TextView content, next_contest, tv_register;
    Button btn;
    DatabaseReference databaseCompetition, databaseCompetition_all, databaseReferenceContestBody, databaseReferenceContestNext, databaseReferenceContestTitle, databaseReference_user, databaseReference_phone_id, databaseReference_phone_id_user;

    public CompetitionFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_competition, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        content = view.findViewById(R.id.contest_contents);
        next_contest = view.findViewById(R.id.next_contest);
        android_id = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        databaseReference_phone_id_user = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("phoneID");
        databaseReference_phone_id = FirebaseDatabase.getInstance().getReference("PhoneID").child(android_id);
        databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        databaseReferenceContestTitle = FirebaseDatabase.getInstance().getReference("Contest").child("title");
        databaseReferenceContestTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                f_line = snapshot.getValue().toString();
                databaseCompetition = FirebaseDatabase.getInstance().getReference("Competition").child(f_line);
                databaseCompetition_all = FirebaseDatabase.getInstance().getReference("Competition").child(f_line).child("status");
                databaseCompetition_all.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            databaseReference_phone_id_user.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot_user_phone_ID) {
                                    databaseReference_phone_id.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot_phone_ID) {
                                            if (android_id.equals(snapshot_user_phone_ID.getValue()) && android_id.equals(snapshot_phone_ID.getValue())) {
                                                btn.setEnabled(true);
                                            } else {
                                                btn.setEnabled(false);
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
                        } else if (snapshot.exists()) {
                            if (snapshot.getValue().toString().equals("0")) {
                                btn.setEnabled(false);
                            } else if (snapshot.getValue().toString().equals("1")) {
                                databaseReference_phone_id_user.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot_user_phone_ID) {
                                        databaseReference_phone_id.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot_phone_ID) {
                                                databaseCompetition.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            if (userID.equals(snapshot.child(userID).getValue())) {
                                                                btn.setEnabled(false);
                                                            } else if (android_id.equals(snapshot_user_phone_ID.getValue()) && android_id.equals(snapshot_phone_ID.getValue()) && !userID.equals(snapshot.child(userID).getValue())) {
                                                                btn.setEnabled(true);
                                                            } else {
                                                                btn.setEnabled(false);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError error) {
                                                    }
                                                });
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
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                databaseReference_user.child("perm").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pom = "1";
                        if (pom.equals(snapshot.getValue().toString())) {
                            Button myButton = new Button(getContext());
                            databaseCompetition_all.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        myButton.setText("Wstrzymaj konkurs");
                                        myButton.setBackgroundColor(Color.RED);
                                    } else {
                                        if (snapshot.getValue().toString().equals("1")) {
                                            myButton.setText("Wstrzymaj konkurs");
                                            myButton.setBackgroundColor(Color.RED);
                                        } else if (snapshot.getValue().toString().equals("0")) {
                                            myButton.setText("Wznów konkurs");
                                            myButton.setBackgroundColor(Color.GREEN);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            LinearLayout ll = (LinearLayout) view.findViewById(R.id.buttonlayout);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ll.addView(myButton, lp);
                            myButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (myButton.getText().equals("Wstrzymaj konkurs")) {
                                        databaseCompetition_all.setValue("0");
                                        myButton.setText("Wznów konkurs");
                                        myButton.setBackgroundColor(Color.GREEN);
                                    } else if (myButton.getText().equals("Wznów konkurs")) {
                                        databaseCompetition_all.setValue("1");
                                        myButton.setText("Wstrzymaj konkurs");
                                        myButton.setBackgroundColor(Color.RED);
                                    }
                                }
                            });
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
        databaseReferenceContestBody = FirebaseDatabase.getInstance().getReference("Contest").child("body");
        databaseReferenceContestBody.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                content.setText(snapshot.getValue().toString().replace(";", "\n"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceContestNext = FirebaseDatabase.getInstance().getReference("Contest").child("next");
        databaseReferenceContestNext.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                next_contest.setText(snapshot.getValue().toString().replace(";", "\n"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        tv_register = view.findViewById(R.id.tv_register);
        btn = view.findViewById(R.id.contest_button);
        //fis = null;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("firstLine", f_line);
                mFragment.setArguments(bundle);
                //getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).addToBackStack(null).commit();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment, "CompetitionFragment").addToBackStack("CompetitionFragment").commit();
            }
        });
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("firstLine", f_line);
                Fragment mFragment = new RegulationsFragment();
                mFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment, "tag").addToBackStack(CompetitionFragment.class.getSimpleName()).commit();
            }
        });
    }
}