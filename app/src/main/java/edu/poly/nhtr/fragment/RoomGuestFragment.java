package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Adapter.MainGuestAdapter;
import edu.poly.nhtr.databinding.FragmentRoomGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomGuestFragment extends Fragment implements RoomGuestInterface.View {

    private final List<MainGuest> mainGuestList = new ArrayList<>();
    private FragmentRoomGuestBinding binding;
    private RecyclerView recyclerView;
    private MainGuestAdapter adapter;
    private Room room; // Store Room object directly
    private PreferenceManager preferenceManager;

    public RoomGuestFragment() {
        // Required empty public constructor
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

        RoomGuestInterface.Presenter presenter = new RoomGuestPresenter(this);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            String roomId = Objects.requireNonNull(room).getRoomId();
            preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
        }

        recyclerView = binding.guestsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MainGuestAdapter(mainGuestList);
        recyclerView.setAdapter(adapter);

        if (room != null) {
            presenter.getMainGuests(room.getRoomId());
        } else {
            showError("Room data is not available");
        }
    }

    @Override
    public void showMainGuest(List<MainGuest> mainGuests) {
        mainGuestList.clear();
        mainGuestList.addAll(mainGuests);
        adapter.notifyDataSetChanged();

        // Hide loading indicator and show RecyclerView
        binding.progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // Hide loading indicator
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showNoDataFound() {
        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        // Hide loading indicator
        binding.progressBar.setVisibility(View.GONE);
    }
}
