package edu.poly.nhtr.Activity.Service;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import edu.poly.nhtr.R;

public class ServiceMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_main);
        //        edt_unit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                edt_unit.setHint("");
//            }
//        });
//        edt_unit.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    float x = event.getX();
//                    float y = event.getY();
//
//                    // Kiểm tra xem người dùng có vuốt khỏi EditText hay không
//                    if (x < 0 || x > edt_unit.getWidth() || y < 0 || y > edt_unit.getHeight()) {
//                        // Thực hiện hành động khi mất quyền truy cập;
//                        if(edt_unit.getText().toString().isEmpty()) edt_unit.setHint("Ví dụ: Xe, cái, ...");
//                    }
//                }
//                return false;
//            }
//        });
    }
}