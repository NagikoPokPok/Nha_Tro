package edu.poly.nhtr.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomContractBinding;
import edu.poly.nhtr.interfaces.RoomContractInterface;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomContractPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomContractFragment extends Fragment implements RoomContractInterface.View {

    public interface OnFragmentInteractionListener {
        void onHideTabLayoutAndViewPager();
    }

    private FragmentRoomContractBinding binding;
    private PreferenceManager preferenceManager;
    private RoomContractInterface.Presenter presenter;
    private Room room;
    private String roomPrice;
    private Home home;
    private OnFragmentInteractionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
        presenter = new RoomContractPresenter(this);

        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            roomPrice = getArguments().getString("room_price");
            home = (Home) getArguments().getSerializable("home");
            if (room != null) {
                Log.d("RoomContractFragment", "Room ID: " + room.getRoomId());
                setListeners();
            } else {
                Log.e("RoomContractFragment", "Room object is null");
            }
        } else {
            Log.e("RoomContractFragment", "Arguments are null");
        }

        setListeners();
    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    @Override
    public String getInfoRoomFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_ROOM_ID);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        binding.btnCreateContract.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHideTabLayoutAndViewPager();
            }
            openAddGuestContractFragment();
        });
        binding.btnViewContract.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHideTabLayoutAndViewPager();
            }
            viewContract();
        });
        binding.btnEditContract.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHideTabLayoutAndViewPager();
            }
            editContract();
        });
        binding.btnPrintContract.setOnClickListener(v -> printContract());
        binding.btnDeleteContract.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHideTabLayoutAndViewPager();
            }
            deleteContract();
        });
    }

    private void openAddGuestContractFragment() {
        GuestAddContractFragment guestAddContractFragment = new GuestAddContractFragment();
        Bundle args = new Bundle();
        args.putSerializable("room", room);
        args.putString("room_price", roomPrice);
        args.putSerializable("home", home);
        guestAddContractFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, guestAddContractFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void viewContract() {
        GuestViewContractFragment viewContractFragment = new GuestViewContractFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);
        bundle.putString("room_price", roomPrice);
        viewContractFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, viewContractFragment)
                .addToBackStack(null)
                .commit();
    }

    private void editContract() {
        GuestEditContractFragment editContractFragment = new GuestEditContractFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        editContractFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editContractFragment)
                .addToBackStack(null)
                .commit();
    }

    private void printContract() {
        presenter.printContract(room);
    }

    private void deleteContract() {
        presenter.deleteContract(room);
    }

    @Override
    public void onContractDeleted() {
        Toast.makeText(requireContext(), "Contract deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContractPrinted() {
        Toast.makeText(requireContext(), "Contract printed successfully", Toast.LENGTH_SHORT).show();
    }
}
