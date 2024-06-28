package edu.poly.nhtr.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.MainGuestAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.MainGuestListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class RoomGuestFragment extends Fragment {

    private RecyclerView recyclerView;
    private MainGuestAdapter adapter;
    private List<MainGuest> mainGuestList = new ArrayList<>();
    private MainGuestListener mainGuestListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_guest, container, false);
        recyclerView = view.findViewById(R.id.guestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MainGuestAdapter(mainGuestList);
        recyclerView.setAdapter(adapter);

        // Call method to fetch data from Firebase and update adapter
        fetchMainGuestData();

        return view;
    }

    private void fetchMainGuestData() {
        // Query Firebase to fetch main guest data
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount())
                .whereEqualTo(Constants.KEY_ROOM_ID, mainGuestListener.getInfoRoomFromGoogleAccount())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mainGuestList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        MainGuest mainGuest = documentSnapshot.toObject(MainGuest.class);
                        mainGuestList.add(mainGuest);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to fetch main guest data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}