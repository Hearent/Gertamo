package com.gertamo.gertamo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    StorageReference storageFile;
    DatabaseReference databaseReference_pop;
    FirebaseAuth fAuth;
    String userID;
    final static String wersja = "7";
    boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        databaseReference_pop = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("pop");
        databaseReference_pop.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot_pop) {
                if(!snapshot_pop.exists()){
                    startActivity(new Intent(MainActivity.this, Pop.class));
                    databaseReference_pop.setValue("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        storageFile = FirebaseStorage.getInstance().getReference("wersja.txt");
        try {
            File localFile = File.createTempFile("wersja", "txt");
            storageFile.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(localFile));
                        String line;
                        line = br.readLine();
                        br.close();
                        if (!line.equals(wersja)) {
                            Intent intent = new Intent(getApplicationContext(), VersionErrActivity.class);
                            startActivity(intent);
                        }

                    } catch (IOException e) {

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home_nav:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment(), "DashboardFragment").addToBackStack("DashboardFragment").commit();
                    break;
                case R.id.competition_nav:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CompetitionFragment(), "DashboardFragment").addToBackStack("DashboardFragment").commit();
                    break;
                case R.id.account_nav:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MoreInfo(), "DashboardFragment").addToBackStack("DashboardFragment").commit();
                    //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(), "DashboardFragment").addToBackStack("DashboardFragment").commit();
                    break;

            }
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        Fragment fragmentInFrame = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (fragmentInFrame.getTag().equals("DashboardFragment")) {
                getSupportFragmentManager().popBackStack("DashboardFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                bottomNavigationView.getMenu().findItem(R.id.home_nav).setChecked(true);
            } else if (fragmentInFrame.getTag().equals("CompetitionFragment")) {
                getSupportFragmentManager().popBackStack("CompetitionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                bottomNavigationView.getMenu().findItem(R.id.competition_nav).setChecked(true);
            } else if (fragmentInFrame.getTag().equals("ProfileFragment")) {
                getSupportFragmentManager().popBackStack("DashboardFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                bottomNavigationView.getMenu().findItem(R.id.account_nav).setChecked(true);
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this,"Cofnij jeszcze raz aby zamknąć aplikację", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    //onDestroy();
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }

}