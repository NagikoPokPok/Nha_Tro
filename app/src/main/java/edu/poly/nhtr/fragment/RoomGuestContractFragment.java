package edu.poly.nhtr.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.databinding.FragmentRoomGuestContractBinding;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomGuestContractFragment extends Fragment {

    private FragmentRoomGuestContractBinding binding;
    private PreferenceManager preferenceManager;
    private Room room;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRoomGuestContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            if (room != null) {
                Log.d("RoomGuestContractFragment", "Room ID: " + room.getRoomId());
                setListener();
            } else {
                Log.e("RoomGuestContractFragment", "Room object is null");
            }
        } else {
            Log.e("RoomGuestContractFragment", "Arguments are null");
        }

        editFonts();
        addGuestFailed();
    }

    // Utility method to show a toast message
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Customize the appearance of text views using custom fonts
    private void editFonts() {
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

    // Method to show the message indicating no guests added
    private void addGuestFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddGuest.setVisibility(View.VISIBLE);
        binding.guestsRecyclerView.setVisibility(View.INVISIBLE);
    }

    // Set up a click listener for the "Add Contract" button
    private void setListener() {
        binding.btnAddContract.setOnClickListener(v -> {
            GuestAddContractFragment guestAddContractFragment = new GuestAddContractFragment();
            Bundle args = new Bundle();
            args.putSerializable("room", room);
            guestAddContractFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(binding.getRoot().getId(), guestAddContractFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
}
