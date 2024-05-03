package edu.poly.nhtr.Activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.poly.nhtr.Activity.Service.ServiceMainActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.ServiceManagenment.Managerment_ServiceActivity;


public class NotificationActivity extends AppCompatActivity {
    Button btn_service;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        btn_service = findViewById(R.id.btn_service);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // bottomNavigationView.setSelectedItemId(R.id.menu_notification);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0,0);
                return true;
//            } else if (item.getItemId() == R.id.menu_notification) {
//                return true;

            } else if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0,0);
                return true;

            }
            return false;
        });
        btn_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this, ServiceMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}