package com.gertamo.gertamo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.HashMap;
import java.util.Map;


public class DashboardFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Upload> options;
    FirebaseRecyclerAdapter<Upload, MyViewHolder> adapter;
    DatabaseReference databaseReference, databaseReference_user, databaseReference_del_like, databaseReference_winners, databaseCompetition, databaseReference_delete, databaseReference_add_like;
    DatabaseReference DbReference_like, databaseReferenceContestTitle, databaseReference_email, databaseReference_phone_id, databaseReference_phone_id_user;
    FirebaseAuth fAuth;
    String userID, firstLine, UserEmail;
    boolean process, isDoubleCliked = false;
    Handler handler;
    Runnable r;
    Context context;
    AlertDialog alertDialog;
    Spinner spinner;
    String order = "like", pom = "1", android_id, sourceString;
    Thread sender;
    TextView appbar;
    private SharedPreferences sharedPref;
    private static final String[] order_by = {"Najpopularniejsze", "Najnowsze", "Najstarsze"};

    public DashboardFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        FirebaseDatabase.getInstance().goOnline();
        sourceString = "Czy chcesz aby to urządzenie było domyślne dla Twojego konta? Urządzenie można przypisać tylko" + "<b>" + " raz" + "</b>. W przypadku zmiany urządzenia prosimy o kontakt na support@gertamo.pl";
        appbar = view.findViewById(R.id.app_bar_title);
        spinner = view.findViewById(R.id.app_bar_dash);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, order_by);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        databaseReference_user.child("ban").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot_ban) {
                String pom = "1";
                if (pom.equals(snapshot_ban.getValue().toString())) {
                    Intent intent = new Intent(getContext(), BanErrActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //ANDROID ID
        android_id = Secure.getString(getContext().getContentResolver(),
                Secure.ANDROID_ID);
        databaseReference_phone_id_user = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("phoneID");
        databaseReference_phone_id = FirebaseDatabase.getInstance().getReference("PhoneID").child(android_id);
        databaseReference_phone_id_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot_phone_id_user) {
                if (!snapshot_phone_id_user.exists() || !android_id.equals(snapshot_phone_id_user.getValue())) {
                    new AlertDialog.Builder(context)
                            .setTitle("Przypisanie urządzenia do konta")
                            .setMessage(Html.fromHtml(sourceString))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseReference_phone_id.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot_phone_id) {
                                            String x = "1";
                                            if (!snapshot_phone_id_user.exists() && !android_id.equals(snapshot_phone_id.getValue())) {
                                                databaseReference_phone_id_user.setValue(android_id);
                                                databaseReference_phone_id.setValue(android_id);
                                                x = "0";
                                                Toast.makeText(getContext(), "Dziękujemy za weryfikację!", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else if (android_id.equals(snapshot_phone_id.getValue()) && x.equals("0")) {
                                                Toast.makeText(context, "Urządzenie zostało już wcześniej użyte", Toast.LENGTH_SHORT).show();
                                            } else if (x.equals("0") && !android_id.equals(snapshot_phone_id.getValue())) {
                                                Toast.makeText(getContext(), "Konto jest przypisane do innego urządzenia", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                //Actions when Single Clicked
                isDoubleCliked = false;
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseReferenceContestTitle = FirebaseDatabase.getInstance().getReference("Contest").child("title");
        databaseReferenceContestTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firstLine = snapshot.getValue().toString();
                appbar.setText(firstLine);
                databaseReference = FirebaseDatabase.getInstance().getReference("Upload Photos").child(firstLine);
                databaseReference_winners = FirebaseDatabase.getInstance().getReference("Winners").child(firstLine);
                options = new FirebaseRecyclerOptions.Builder<Upload>().setQuery(databaseReference.orderByChild(order), Upload.class).build();
                adapter = new FirebaseRecyclerAdapter<Upload, MyViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(MyViewHolder holder, int position, Upload model) {
                        if (model.getId() != null) {
//to chyba można wyjebać| DatabaseReference DbReference = FirebaseDatabase.getInstance().getReference("Upload Photos").child(model.getId());
                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                            DbReference_like.keepSynced(true);
                            DbReference_like.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.hasChild(userID)) {
                                        holder.textView.setTypeface(null, Typeface.BOLD);
                                        holder.textView.setTextColor(Color.parseColor("#038A31"));
                                    } else {
                                        holder.textView.setTypeface(null, Typeface.NORMAL);
                                        holder.textView.setTextColor(Color.parseColor("#000000"));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                }
                            });
                            holder.contest_name.setText(String.valueOf(model.getName()));
                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                            DbReference_like.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot_like) {
                                    holder.textView.setText(String.valueOf(snapshot_like.getChildrenCount() - 1));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //Picasso.get().load(model.getImageUri()).into(holder.imageView);
                            Glide.with(context).load(model.getImageUri()).into(holder.imageView);
                            databaseReference_user.child("perm").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot_perm_user) {
                                    if (pom.equals(snapshot_perm_user.getValue().toString())) {
                                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final String[] options = {"Dodaj zwycięzcę", "Dodaj drugie miejsce", "Dodaj trzecie miejsce", "Usuń zdjęcie", "Zbanuj użytkownika", "Dodaj like"};
                                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, options);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(model.getAuthor());
                                                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        switch (which) {
                                                            case 0:
                                                                databaseReference_winners.child("1st").addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot snapshot) {
                                                                        if (process) {
                                                                            Upload upload = new Upload(model.getImageUri(), model.getLike(), model.getAuthor(), model.getName(), model.getDate());
                                                                            databaseReference_winners.child("1st").setValue(upload);
                                                                            databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("email");
                                                                            databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    final ProgressDialog dialog = new ProgressDialog(getContext());
                                                                                    dialog.setTitle("Wysyłam email");
                                                                                    dialog.setMessage("Proszę czekaj");
                                                                                    dialog.show();
                                                                                    Thread sender = new Thread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            try {
                                                                                                GMailSender sender = new GMailSender("wsparcie.gertamo@gmail.com", "Zarobimy10m$");
                                                                                                sender.sendMail(firstLine + " - Zwycięzca",
                                                                                                        "Cześć,\n\nTwoje zdjęcie zebrało najwięcej ocen i zwyciężyło w rankingu.\nCzy możesz podać dane do oznaczenia Cię na Instagramie lub Facebooku?\n\nDodatkowo będziemy bardzo wdzięczni za ocenę naszej aplikacji na Sklepie Play :)\nhttps://play.google.com/store/apps/details?id=com.gertamo.gertamo\n\nPozdrawiamy i życzymy dalszych sukcesów\nZespół Gertamo",
                                                                                                        "konkursy@gertamo.pl",
                                                                                                        snapshot.getValue().toString());
                                                                                                dialog.dismiss();

                                                                                            } catch (Exception e) {
                                                                                                Log.e("mylog", "Error: " + e.getMessage());
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                    sender.start();

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                            Toast.makeText(getContext(), "Dodałeś zwycięzcę do bazy danych", Toast.LENGTH_LONG).show();
                                                                            process = false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError error) {
                                                                    }
                                                                });
                                                                break;
                                                            case 1:
                                                                databaseReference_winners.child("2nd").addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot snapshot) {
                                                                        if (process) {
                                                                            Upload upload = new Upload(model.getImageUri(), model.getLike(), model.getAuthor(), model.getName(), model.getDate());
                                                                            databaseReference_winners.child("2nd").setValue(upload);
                                                                            databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("email");
                                                                            databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    final ProgressDialog dialog = new ProgressDialog(getContext());
                                                                                    dialog.setTitle("Wysyłam email");
                                                                                    dialog.setMessage("Proszę czekaj");
                                                                                    dialog.show();
                                                                                    Thread sender = new Thread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            try {
                                                                                                GMailSender sender = new GMailSender("wsparcie.gertamo@gmail.com", "Zarobimy10m$");
                                                                                                sender.sendMail(firstLine + " - Drugie miejsce",
                                                                                                        "Cześć,\n\nTwoje zdjęcie zajęło drugie miejsce! Było bardzo blisko :)\n\nCzy możesz podać dane do oznaczenia Cię na Instagramie lub Facebooku, aby również Twoje zdjęcie zostału opublikowane?\n\nDodatkowo będziemy bardzo wdzięczni za ocenę naszej aplikacji na Sklepie Play :)\nhttps://play.google.com/store/apps/details?id=com.gertamo.gertamo\n\nPozdrawiamy i życzymy dalszych sukcesów\nZespół Gertamo",
                                                                                                        "konkursy@gertamo.pl",
                                                                                                        snapshot.getValue().toString());
                                                                                                dialog.dismiss();

                                                                                            } catch (Exception e) {
                                                                                                Log.e("mylog", "Error: " + e.getMessage());
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                    sender.start();

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                            Toast.makeText(getContext(), "Dodałeś drugie miejsce do bazy danych", Toast.LENGTH_LONG).show();
                                                                            process = false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError error) {
                                                                    }
                                                                });
                                                                break;
                                                            case 2:
                                                                databaseReference_winners.child("3rd").addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot snapshot) {
                                                                        if (process) {
                                                                            Upload upload = new Upload(model.getImageUri(), model.getLike(), model.getAuthor(), model.getName(), model.getDate());
                                                                            databaseReference_winners.child("3rd").setValue(upload);
                                                                            databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("email");
                                                                            databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    final ProgressDialog dialog = new ProgressDialog(getContext());
                                                                                    dialog.setTitle("Wysyłam email");
                                                                                    dialog.setMessage("Proszę czekaj");
                                                                                    dialog.show();
                                                                                    Thread sender = new Thread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            try {
                                                                                                GMailSender sender = new GMailSender("wsparcie.gertamo@gmail.com", "Zarobimy10m$");
                                                                                                sender.sendMail(firstLine + " - Trzecie miejsce",
                                                                                                        "Cześć,\n\nTwoje zdjęcie zajęło trzecie miejsce!\n\nCzy możesz podać dane do oznaczenia Cię na Instagramie lub Facebooku, aby również Twoje zdjęcie zostału opublikowane?\n\nDodatkowo będziemy bardzo wdzięczni za ocenę naszej aplikacji na Sklepie Play :)\nhttps://play.google.com/store/apps/details?id=com.gertamo.gertamo\n\nPozdrawiamy i życzymy dalszych sukcesów\nZespół Gertamo",
                                                                                                        "konkursy@gertamo.pl",
                                                                                                        snapshot.getValue().toString());
                                                                                                dialog.dismiss();

                                                                                            } catch (Exception e) {
                                                                                                Log.e("mylog", "Error: " + e.getMessage());
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                    sender.start();

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                            Toast.makeText(getContext(), "Dodałeś trzecie miejsce do bazy danych", Toast.LENGTH_LONG).show();
                                                                            process = false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError error) {
                                                                    }
                                                                });
                                                                break;
                                                            case 3:
                                                                databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("email");
                                                                databaseReference = FirebaseDatabase.getInstance().getReference("Upload Photos").child(firstLine).child(model.getId());
                                                                databaseReference_del_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                                                                databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        final ProgressDialog dialog = new ProgressDialog(getContext());
                                                                        dialog.setTitle("Wysyłam email");
                                                                        dialog.setMessage("Proszę czekaj");
                                                                        dialog.show();
                                                                        Thread sender = new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    GMailSender sender = new GMailSender("wsparcie.gertamo@gmail.com", "Zarobimy10m$");
                                                                                    sender.sendMail("Usunięte zdjęcie",
                                                                                            "Twoje zdjęcie zostało usunięte ponieważ było niezgodne z Zasadami konkursu lub naruszyło Regulamin. W sprawie wyjaśnienia sytuacji skontaktuj się z nami na maila konkursy@gertamo.pl, lub odpowiedz na tą wiadomość",
                                                                                            "konkursy@gertamo.pl",
                                                                                            snapshot.getValue().toString());
                                                                                    dialog.dismiss();

                                                                                } catch (Exception e) {
                                                                                    Log.e("mylog", "Error: " + e.getMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                        sender.start();

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                Upload upload_delete = new Upload(model.getImageUri(), model.getLike(), model.getDesclike(), model.getId(), model.getUid(), model.getAuthor(), model.getName(), model.getDate(), model.getDesclike());

                                                                databaseReference_delete = FirebaseDatabase.getInstance().getReference("Delete Photos").child(firstLine).child(model.getId());
                                                                databaseReference_delete.setValue(upload_delete);
                                                                databaseReference_del_like.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot_del_like) {
                                                                        databaseReference_del_like.removeValue();
                                                                        databaseReference.removeValue();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                Toast.makeText(getContext(), "Zdjęcie zostało usunięte", Toast.LENGTH_LONG).show();
                                                                break;
                                                            case 4:
                                                                databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid()).child("email");
                                                                databaseReference_user.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        UserEmail = snapshot.getValue().toString();
                                                                        final ProgressDialog dialog = new ProgressDialog(getContext());
                                                                        dialog.setTitle("Wysyłam email");
                                                                        dialog.setMessage("Proszę czekaj");
                                                                        dialog.show();
                                                                        Thread sender = new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    GMailSender sender = new GMailSender("wsparcie.gertamo@gmail.com", "Zarobimy10m$");
                                                                                    sender.sendMail("Ban",
                                                                                            "Dostałeś bana, aby wyjaśnić sytuację napisz do nas na maila support@gertamo.pl",
                                                                                            "konkursy@gertamo.pl",
                                                                                            UserEmail);
                                                                                    dialog.dismiss();
                                                                                } catch (Exception e) {
                                                                                    Log.e("mylog", "Error: " + e.getMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                        sender.start();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                databaseReference_del_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                                                                databaseReference_user = FirebaseDatabase.getInstance().getReference("Users").child(model.getUid());
                                                                Map<String, Object> map = new HashMap<>();
                                                                map.put("ban", 1);
                                                                databaseReference_user.updateChildren(map);
                                                                databaseReference = FirebaseDatabase.getInstance().getReference("Upload Photos").child(firstLine).child(model.getId());
                                                                Upload upload_delete_ban = new Upload(model.getImageUri(), model.getLike(), model.getDesclike(), model.getId(), model.getUid(), model.getAuthor(), model.getName(), model.getDate(), model.getDesclike());
                                                                databaseReference_delete = FirebaseDatabase.getInstance().getReference("Delete Photos").child(firstLine).child(model.getId());
                                                                databaseReference_delete.setValue(upload_delete_ban);
                                                                databaseReference_del_like.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot_del_like) {
                                                                        databaseReference_del_like.removeValue();
                                                                        databaseReference.removeValue();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                Toast.makeText(getContext(), "Użytkownik został zbanowany", Toast.LENGTH_LONG).show();
                                                                break;
                                                            case 5:
                                                                databaseReference_add_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                                                                String like = String.valueOf(model.getLike());
                                                                databaseReference_add_like.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        databaseReference_add_like.child(like).setValue(model.getLike());
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                break;
                                                        }
                                                    }
                                                });
                                                alertDialog = builder.create();
                                                process = true;
                                                if (isDoubleCliked) {
                                                    alertDialog.show();
                                                    isDoubleCliked = false;
                                                    handler.removeCallbacks(r);
                                                } else {
                                                    isDoubleCliked = true;
                                                    handler.postDelayed(r, 500);
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                            holder.imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    databaseCompetition = FirebaseDatabase.getInstance().getReference("Competition").child(firstLine).child("status");
                                    databaseCompetition.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                process = true;
                                                if (isDoubleCliked) {
                                                    if (snapshot.getValue().toString().equals("1")) {
                                                        databaseReference_phone_id_user.addValueEventListener(new ValueEventListener() {
                                                            String x = "1";

                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot_check_phone_ID_U) {
                                                                databaseReference_phone_id.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot_check_phone_ID) {
                                                                        if (android_id.equals(snapshot_check_phone_ID_U.getValue()) && android_id.equals(snapshot_check_phone_ID.getValue())) {
                                                                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                                                                            DbReference_like.keepSynced(true);
                                                                            DbReference_like.addValueEventListener(new ValueEventListener() {
                                                                                int i = model.getLike();
                                                                                int di = model.getDesclike();

                                                                                @Override
                                                                                public void onDataChange(DataSnapshot snapshot_like_us) {
                                                                                    if (process) {
                                                                                        if (userID.equals(snapshot_like_us.child("author").child(userID).getValue())) {
                                                                                            Toast.makeText(getContext(), "Nie możesz oceniać własnego zdjęcia!", Toast.LENGTH_LONG).show();
                                                                                            process = false;
                                                                                        } else {
                                                                                            if (snapshot_like_us.hasChild(userID)) {
//                                                                                    DbReference.child("like").setValue(snapshot_like_us.getChildrenCount()-2);
//                                                                                    DbReference.child("desclike").setValue(snapshot_like_us.getChildrenCount()-2);
                                                                                                DbReference_like.child(userID).removeValue();
//                                                                                    i = (int) snapshot.getChildrenCount();
//                                                                                    model.setLike(i);

//                                                                                    di = -(int) snapshot.getChildrenCount();
//                                                                                    model.setDesclike(di);
//                                                                                    DbReference.setValue(model);
                                                                                                holder.textView.setTypeface(null, Typeface.NORMAL);
                                                                                                holder.textView.setTextColor(Color.parseColor("#000000"));
                                                                                                process = false;
                                                                                            } else {
                                                                                                databaseReference_email = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("email");
                                                                                                databaseReference_email.addValueEventListener(new ValueEventListener() {
                                                                                                    @Override
                                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot_us) {

                                                                                                        DbReference_like.child(userID).setValue(snapshot_us.getValue().toString());
//                                                                                            i = (int) snapshot.getChildrenCount();
//                                                                                            model.setLike(i);
//                                                                                            DbReference.setValue(model);
//                                                                                            di = -(int) snapshot.getChildrenCount();
//                                                                                            model.setDesclike(di);
//                                                                                            DbReference.setValue(model);
                                                                                                        holder.textView.setTypeface(null, Typeface.BOLD);
                                                                                                        holder.textView.setTextColor(Color.parseColor("#038A31"));
                                                                                                        process = false;
                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                                    }
                                                                                                });

                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError error) {
                                                                                }
                                                                            });
                                                                        } else if (!snapshot_check_phone_ID_U.exists() && x.equals("1")) {
                                                                            new AlertDialog.Builder(context)
                                                                                    .setTitle("Przypisanie urządzenia do konta")
                                                                                    .setMessage(Html.fromHtml(sourceString))
                                                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                                            if (!snapshot_check_phone_ID_U.exists() && !android_id.equals(snapshot_check_phone_ID.getValue())) {
                                                                                                databaseReference_phone_id_user.setValue(android_id);
                                                                                                databaseReference_phone_id.setValue(android_id);
                                                                                                x = "0";
                                                                                                Toast.makeText(getContext(), "Dziękujemy za weryfikację!", Toast.LENGTH_SHORT).show();
                                                                                                return;
                                                                                            } else if (android_id.equals(snapshot_check_phone_ID.getValue()) && x.equals("1")) {
                                                                                                Toast.makeText(context, "Urządzenie zostało już wcześniej użyte", Toast.LENGTH_SHORT).show();
                                                                                            } else if (snapshot_check_phone_ID_U.exists() && !android_id.equals(snapshot_check_phone_ID.getValue())) {
                                                                                                Toast.makeText(getContext(), "Konto jest przypisane do innego urządzenia", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    })
                                                                                    .setNegativeButton(android.R.string.no, null)
                                                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                                                    .show();
                                                                            process = false;
                                                                        } else if (!android_id.equals(snapshot_check_phone_ID_U.getValue())&& android_id.equals(snapshot_check_phone_ID.getValue()) && x.equals("1")) {
                                                                            Toast.makeText(context, "Konto jest przypisane do innego urządzenia", Toast.LENGTH_SHORT).show();
                                                                            process = false;
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

                                                        isDoubleCliked = false;
                                                        //remove callbacks for Handlers
                                                        handler.removeCallbacks(r);
                                                    } else if (snapshot.getValue().toString().equals("0")) {
                                                        Toast.makeText(getContext(), "Konkurs został zakończony i nie możesz już oceniać", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    isDoubleCliked = true;
                                                    handler.postDelayed(r, 500);
                                                }


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                            DbReference_like = FirebaseDatabase.getInstance().getReference("Likes").child(firstLine).child(model.getId());
                            DbReference_like.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot_us_like) {
                                    if (snapshot_us_like.exists()) {
                                        DatabaseReference DbReference = databaseReference.child(model.getId());
                                        DbReference.child("like").setValue(snapshot_us_like.getChildrenCount() - 1);
                                        DbReference.child("desclike").setValue((snapshot_us_like.getChildrenCount() - 1) * (-1));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                    @Override
                    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
                        return new MyViewHolder(v);
                    }
                };
                adapter.startListening();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                order = "like";
                onStart();
                break;
            case 1:
                order = "date";
                onStart();
                break;
            case 2:
                order = "descdate";
                onStart();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}