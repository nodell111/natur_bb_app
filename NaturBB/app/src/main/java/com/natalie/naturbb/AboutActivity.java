package com.natalie.naturbb;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import com.natalie.naturbb.databinding.ActivityMainBinding;



public class AboutActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.about_activity);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        binding.BottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.favorites:
                    replaceFragment(new FavoritesFragment());
                    break;
            }
            return true;

        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

}
