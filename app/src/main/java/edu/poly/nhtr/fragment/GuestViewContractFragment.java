package edu.poly.nhtr.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.DecimalFormat;

import edu.poly.nhtr.databinding.FragmentGuestViewContractBinding;
import edu.poly.nhtr.interfaces.GuestViewContractInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.GuestViewContractPresenter;

public class GuestViewContractFragment extends Fragment implements GuestViewContractInterface {
    private TextInputEditText guestName, guestPhone, guestCCCD, dateOfBirth, gender, totalMembers, createDate, dateIn, roomPrice, expirationDate, payDate, daysUntilDueDate;
    private RoundedImageView imgCCCDFront;
    private RoundedImageView imgCCCDBack;
    private ImageView imgAddCCCDFront;
    private ImageView imgAddCCCDBack;
    private RoundedImageView imgContractFront;
    private RoundedImageView imgContractBack;
    private ImageView imgAddContractFront;
    private ImageView imgAddContractBack;
    private GuestViewContractPresenter presenter;
    private FragmentGuestViewContractBinding binding;
    private Room room;
    private Button btnReturn;
    private CountryCodePicker ccp;
    private RoomContractFragment.OnFragmentInteractionListener mListener;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RoomContractFragment.OnFragmentInteractionListener) {
            mListener = (RoomContractFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

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
                presenter.fetchContractData(roomId);
            } else {
                showToast("Invalid room ID");
            }
        }
        setListeners();
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
        imgAddCCCDFront = binding.imgAddCccdFront;
        imgAddCCCDBack = binding.imgAddCccdBack;
        imgCCCDFront = binding.imgCccdFront;
        imgCCCDBack = binding.imgCccdBack;
        imgAddContractFront = binding.imgAddContractFront;
        imgAddContractBack = binding.imgAddContractBack;
        imgContractFront = binding.imgContractFront;
        imgContractBack = binding.imgContractBack;
        btnReturn = binding.btnReturn;
    }

    private void setListeners() {
        ccp = binding.ccp;
        ccp.setDefaultCountryUsingNameCode("VN");
        ccp.resetToDefaultCountry();
        btnReturn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            if (mListener != null) {
                mListener.showTabLayoutAndViewPager();
            }
        });
    }

    private Bitmap getConversionImageForCCCD(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int heightDp = 250;
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        int widthPx = bitmap.getWidth() * heightPx / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
    }

    private Bitmap getConversionImageForContract(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int heightDp = 300;
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        int widthPx = bitmap.getWidth() * heightPx / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
    }


    @Override
    public void displayContractData(MainGuest mainGuest) {
        if (mainGuest != null) {
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

            // Set CCCD Front Image
            if (mainGuest.getCccdImageFront() != null && !mainGuest.getCccdImageFront().isEmpty()) {
                imgAddCCCDFront.setVisibility(View.GONE);
                imgCCCDFront.setImageBitmap(getConversionImageForCCCD(mainGuest.getCccdImageFront()));
            } else {
                imgAddCCCDFront.setVisibility(View.VISIBLE);
            }

            // Set CCCD Back Image
            if (mainGuest.getCccdImageBack() != null && !mainGuest.getCccdImageBack().isEmpty()) {
                imgAddCCCDBack.setVisibility(View.GONE);
                imgCCCDBack.setImageBitmap(getConversionImageForCCCD(mainGuest.getCccdImageBack()));
            } else {
                imgAddCCCDBack.setVisibility(View.VISIBLE);
            }

            // Set Contract Front Image
            if (mainGuest.getContractImageFront() != null && !mainGuest.getContractImageFront().isEmpty()) {
                imgAddContractFront.setVisibility(View.GONE);
                imgContractFront.setImageBitmap(getConversionImageForContract(mainGuest.getContractImageFront()));
            } else {
                imgAddContractFront.setVisibility(View.VISIBLE);
            }

            // Set Contract Back Image
            if (mainGuest.getContractImageBack() != null && !mainGuest.getContractImageBack().isEmpty()) {
                imgAddContractBack.setVisibility(View.GONE);
                imgContractBack.setImageBitmap(getConversionImageForContract(mainGuest.getContractImageBack()));
            } else {
                imgAddContractBack.setVisibility(View.VISIBLE);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
