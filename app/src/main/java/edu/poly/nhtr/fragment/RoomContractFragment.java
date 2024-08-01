package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import edu.poly.nhtr.R;
import edu.poly.nhtr.alarmManager.AlarmService;
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

        void showTabLayoutAndViewPager();
    }

    private FragmentRoomContractBinding binding;
    private PreferenceManager preferenceManager;
    private RoomContractInterface.Presenter presenter;
    private Room room;
    private String roomPrice;
    private Home home;
    private OnFragmentInteractionListener listener;
    private Dialog dialog;
    private AlarmService alarmService;
    private AlarmService alarmService2;
    private String header1, body1, header2, body2;

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
        dialog = new Dialog(requireContext());

        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            roomPrice = getArguments().getString("room_price");
            home = (Home) getArguments().getSerializable("home");
            if (room != null) {
                header1 = "Sắp tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
                body1 = "Bạn cần lập hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();

                header2 = "Đã tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
                body2 = "Bạn cần gửi hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();

                alarmService = new AlarmService(requireContext(), home, room, header1, body1);
                alarmService2 = new AlarmService(requireContext(), home, room, header2, body2);
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
//            if (listener != null) {
//                listener.onHideTabLayoutAndViewPager();
//            }
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
        openConfirmDeleteContractDialog();

    }

    private void openConfirmDeleteContractDialog() {
        setupDialog(R.layout.layout_dialog_delete_contract);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnDeleteContract = dialog.findViewById(R.id.btn_confirm_delete_contract);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDeleteContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRepetitiveAlarm();
                presenter.deleteContract(room);
            }
        });
    }

    public void cancelRepetitiveAlarm()
    {
        showToast("Delete contract => Cancel alarm");
        int requestCode1 = Integer.parseInt(preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, room.getRoomId()+"code1"));
        alarmService.cancelRepetitiveAlarm(requestCode1);

        int requestCode2 = Integer.parseInt(preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, room.getRoomId()+"code2"));
        alarmService2.cancelRepetitiveAlarm(requestCode2);
    }

    private void setupDialog(int layoutId) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    public void showButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
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
