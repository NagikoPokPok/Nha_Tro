package edu.poly.nhtr.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityBillManagementBinding;
import edu.poly.nhtr.databinding.ActivityMainBinding;

public class BillManagement extends AppCompatActivity {

    ActivityBillManagementBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_management);
        binding = ActivityBillManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListners();
    }

    public void setListners(){
        binding.btnBack.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }

}