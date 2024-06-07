package edu.poly.nhtr.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomGuestBinding;
// import edu.poly.nhtr.listeners.GuestListener;

public class RoomGuestFragment extends Fragment { // implements GuestListener

    FragmentRoomGuestBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room_guest, container, false);
    }

//    @Override
//    public void hideFrameTop() {
//        @Override
//        public void showFrameTop() {
//            binding.btnAddRoom.setVisibility(View.VISIBLE);
//        }
//    }
}