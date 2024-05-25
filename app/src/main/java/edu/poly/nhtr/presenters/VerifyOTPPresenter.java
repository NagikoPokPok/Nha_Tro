package edu.poly.nhtr.presenters;


import android.os.CountDownTimer;

import java.util.Random;

import edu.poly.nhtr.interfaces.VerifyOTPInterface;
import edu.poly.nhtr.models.OTP;


public class VerifyOTPPresenter {
    final private VerifyOTPInterface view;
    final private int resendTime = 60;
    private CountDownTimer countDownTimer;
    private int random = 0;


    public VerifyOTPPresenter(VerifyOTPInterface view) {
        this.view = view;

    }

    public void verifyOTP(OTP otp) {
        view.showLoading();
        if (otp.isEmpty()) {
            view.showToast("Vui lòng nhập đủ OTP");
            view.hideLoading();
        } else {
            String code = otp.fullCode();
            if (!code.equals(String.valueOf(random))) {
                view.showToast("Sai OTP");
                view.clearOTP();
                view.hideLoading();
            } else {
                view.verifySuccess();
            }
        }
    }


    public void random() {
        Random randomOtp = new Random();
        random = randomOtp.nextInt(900000) + 100000; // Generate a random 6-digit OTP

        view.sendEmail(random);
        //"nhatrohomemate@gmail.com", "orjz scow qdli loqh"
        //"iuxq ggco nwld zvyx
    }

    public void startCountDownTimer() {
        view.setTextEnabled(false);
        view.setBeforeTextColor();
        countDownTimer = new CountDownTimer(resendTime * 1000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
                view.setText("Gửi lại OTP (" + (millisUntilFinished / 1000) + ")");

            }

            @Override
            public void onFinish() {
                view.setTextEnabled(true);
                view.setText("Gửi lại OTP");
                random = 0;
                view.setAfterTextColor();
                view.showToast("Quá giờ để nhập OTP. Vui lòng chọn Resend để nhận OTP mới!");
            }
        }.start();
    }

    public void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
