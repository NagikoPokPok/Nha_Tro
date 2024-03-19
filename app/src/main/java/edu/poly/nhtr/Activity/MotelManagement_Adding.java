package edu.poly.nhtr.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMotelManagementAddingBinding;

public class MotelManagement_Adding extends AppCompatActivity {

    private ActivityMotelManagementAddingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motel_management_adding);
        binding = ActivityMotelManagementAddingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }

    private void setListeners() {
        setDropDownMenu();
    }

    private void setDropDownMenu() {
        // Lấy mảng chuỗi từ strings.xml
        String[] loaiHinhTro = getResources().getStringArray(R.array.motels);

        // Thiết lập ArrayAdapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_layout, loaiHinhTro);
        binding.loaiHinhTro.setAdapter(arrayAdapter);


    }
}