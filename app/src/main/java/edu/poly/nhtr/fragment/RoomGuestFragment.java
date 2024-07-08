package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.MainGuestAdapter;
import edu.poly.nhtr.databinding.FragmentRoomGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.presenters.RoomGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomGuestFragment extends Fragment implements RoomGuestInterface.View {

    private final List<MainGuest> mainGuestsList = new ArrayList<>();
    private FragmentRoomGuestBinding binding;
    private RecyclerView recyclerView;
    private MainGuestAdapter adapter;
    private PreferenceManager preferenceManager;
    private RoomViewModel roomViewModel;

    public RoomGuestFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomGuestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        roomViewModel = new ViewModelProvider(requireActivity()).get(RoomViewModel.class);
        RoomGuestInterface.Presenter presenter = new RoomGuestPresenter(this, roomViewModel);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        recyclerView = binding.guestsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MainGuestAdapter(mainGuestsList);
        recyclerView.setAdapter(adapter);

        // Observe the room data from the ViewModel
        roomViewModel.getRoom().observe(getViewLifecycleOwner(), room -> {
            if (room != null) {
                String roomId = room.getRoomId();
                preferenceManager.putString(Constants.PREF_KEY_ROOM_ID, roomId);
                Log.d("RoomGuestFragment", "Room ID: " + roomId);
                presenter.getMainGuests(roomId);
            } else {
                Log.e("RoomGuestFragment", "Room object is null");
                showError("Room data is not available");
            }
        });

        // Observe the main guests data from the ViewModel
        roomViewModel.getMainGuests().observe(getViewLifecycleOwner(), mainGuests -> {
            if (mainGuests != null && !mainGuests.isEmpty()) {
                showMainGuest(mainGuests);
            } else {
                showNoDataFound();
            }
        });

        // If arguments are provided, set the room in the ViewModel
        if (getArguments() != null) {
            Room room = (Room) getArguments().getSerializable("room");
            roomViewModel.setRoom(room);
        }
    }

    @Override
    public void showMainGuest(List<MainGuest> mainGuests) {
        mainGuestsList.clear();
        mainGuestsList.addAll(mainGuests);
        adapter.notifyDataSetChanged();

        binding.progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showNoDataFound() {
        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }
}
