package edu.poly.nhtr.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestViewContractBinding;
import edu.poly.nhtr.interfaces.GuestViewContractInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.GuestViewContractPresenter;

public class GuestViewContractFragment extends Fragment implements GuestViewContractInterface {
    private static final String TAG = "GuestViewContractFragment";
    private TextInputEditText guestName, guestPhone, guestCCCD, dateOfBirth, gender, totalMembers, createDate, dateIn, roomPrice, expirationDate, payDate, daysUntilDueDate;
    private ImageView cccdImageFront, cccdImageBack, contractImageFront, contractImageBack;
    private GuestViewContractPresenter presenter;
    private FragmentGuestViewContractBinding binding;
    private Room room;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGuestViewContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        presenter = new GuestViewContractPresenter(this);

        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            if (room != null) {
                String roomId = room.getRoomId();
                Log.d(TAG, "Fetching contract data for room ID: " + roomId);
                presenter.fetchContractData(roomId);
            } else {
                showToast("Invalid room ID");
            }
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoDataFound() {
        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        guestName = binding.edtTenKhach;
        guestPhone = binding.edtSoDienThoai;
        guestCCCD = binding.edtSoCccdCmnd;
        dateOfBirth = binding.edtNgaySinh;
        gender = binding.edtGioiTinh;
        totalMembers = binding.edtTotalMembers;
        createDate = binding.edtNgayTaoHopDong;
        dateIn = binding.edtNgayVaoO;
        roomPrice = binding.edtGiaPhong;
        expirationDate = binding.edtNgayHetHanHopDong;
        payDate = binding.edtNgayTraTienPhong;
        daysUntilDueDate = binding.edtHanThanhToan;
        cccdImageFront = binding.imgCccdFront;
        cccdImageBack = binding.imgCccdBack;
        contractImageFront = binding.imgContractFront;
        contractImageBack = binding.imgContractBack;
    }

    private Bitmap decodeImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void displayContractData(MainGuest mainGuest) {
        if (mainGuest != null) {
            Log.d(TAG, "Displaying contract data: " + mainGuest);
            guestName.setText(mainGuest.getNameGuest());
            guestPhone.setText(mainGuest.getPhoneGuest());
            guestCCCD.setText(mainGuest.getCccdNumber());
            dateOfBirth.setText(mainGuest.getDateOfBirth());
            gender.setText(mainGuest.getGender());
            totalMembers.setText(String.valueOf(mainGuest.getTotalMembers()));
            createDate.setText(mainGuest.getCreateDate());
            dateIn.setText(mainGuest.getDateIn());
            roomPrice.setText(formatPrice(mainGuest.getRoomPrice()));
            expirationDate.setText(mainGuest.getExpirationDate());
            payDate.setText(mainGuest.getPayDate());
            daysUntilDueDate.setText(String.valueOf(mainGuest.getDaysUntilDueDate()));

            if (mainGuest.getCccdImageFront() != null) {
                cccdImageFront.setImageBitmap(decodeImage(mainGuest.getCccdImageFront()));
            }
            if (mainGuest.getCccdImageBack() != null) {
                cccdImageBack.setImageBitmap(decodeImage(mainGuest.getCccdImageBack()));
            }
            if (mainGuest.getContractImageFront() != null) {
                contractImageFront.setImageBitmap(decodeImage(mainGuest.getContractImageFront()));
            }
            if (mainGuest.getContractImageBack() != null) {
                contractImageBack.setImageBitmap(decodeImage(mainGuest.getContractImageBack()));
            }
        } else {
            showToast("MainGuest data is null");
        }
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
