package edu.poly.nhtr.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.databinding.FragmentRoomGuestContractBinding;


public class RoomGuestContractFragment extends Fragment {

    FragmentRoomGuestContractBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRoomGuestContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editFonts();
        addHomeFailed();
        setListener();
    }

    // Hiển thị thông báo chưa có khách thuê
    private void editFonts() {
        //Set three fonts into one textview
        Spannable text1 = new SpannableString("Bạn chưa có khách thuê\n Hãy nhấn nút ");
        Typeface interLightTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.setText(text1);

        Spannable text2 = new SpannableString("+");
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text2);

        Spannable text3 = new SpannableString(" để thêm hợp đồng.");
        Typeface interLightTypeface2 = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text3);
    }

    private void showHomesRecyclerView() {
        binding.layoutNoRoom.setVisibility(View.GONE);
        binding.txtNotification.setVisibility(View.GONE);
        binding.imgAddGuest.setVisibility(View.GONE);
        binding.guestsRecyclerView.setVisibility(View.VISIBLE);
    }

    public void addHomeFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddGuest.setVisibility(View.VISIBLE);
        binding.guestsRecyclerView.setVisibility(View.INVISIBLE);
    }

    public void setListener() {
        binding.btnAddContract.setOnClickListener(v -> {
            GuestAddContractFragment guestAddContractFragment = new GuestAddContractFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(binding.getRoot().getId(), guestAddContractFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });
    }

}