package edu.poly.nhtr.fragment;

import android.icu.text.Collator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomMakeBillBinding;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.utilities.Constants;


public class RoomMakeBillFragment extends Fragment {

    private String roomId;
    private FragmentRoomMakeBillBinding binding;
    private Room room;
    private MainGuest mainGuest;
    private List<RoomService> roomServiceList;
    private RoomBill bill;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentRoomMakeBillBinding.inflate(getLayoutInflater());

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            bill = (RoomBill) arguments.getSerializable("bill");
            if (bill != null) {
                roomId = bill.getRoomID();
                showToast(roomId);
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        getRoomFromFirebase(roomId);
        mainGuest = getMainGuest(roomId);
        getListRoomService(roomId);

        setData();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void getListRoomService(String roomId) {
        List<RoomService> roomServices = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            for (DocumentSnapshot document : task.getResult()){
                                RoomService roomService = new RoomService(
                                        document.getId(),
                                        document.getString(Constants.KEY_ROOM_ID),
                                        document.getString(Constants.KEY_SERVICE_ID),
                                        Math.toIntExact(document.getLong(Constants.KEY_ROOM_SERVICE_QUANTITY))
                                );



                                roomServices.add(roomService);

                            }

                            roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));

                        }
                    }
                });
    }

    private MainGuest getMainGuest(String roomId) {
        final MainGuest[] mainGuest1 = new MainGuest[1];
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                            mainGuest1 = new MainGuest(
//                                    Math.toIntExact(document.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS)),
//
//                            );
                            mainGuest1[0] = new MainGuest();
                        }
                    }
                });
        return mainGuest1[0];
    }

    private Room getRoomFromFirebase(String roomId) {
        final Room[] room1 = new Room[1];
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            room1[0] = new Room(
                                    document.getString(Constants.KEY_NAME_ROOM),
                                    document.getString(Constants.KEY_PRICE),
                                    document.getString(Constants.KEY_DESCRIBE)
                                    );
                        }
                    }
                });
        return room1[0];
    }


    private void setData() {
        //Set date time
        setDateTime();

        //Set price of room
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            binding.txtRoomPrice.setText(task.getResult().getString(Constants.KEY_PRICE));
                        }
                    }
                });
    }

    private void setDateTime() {
        // Lấy ngày hiện tại
        Date date = new Date();

        // Định dạng ngày
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String createBillDate = formatter.format(date);
        String monthYear = "Tháng " + date.getMonth() + ", " + date.getYear();
        String datePay = mainGuest.getPayDate();

        binding.txtMonthYear.setText(monthYear);
        binding.txtCreateBillDate.setText(createBillDate);
        binding.txtPayDate.setText(datePay);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_room_make_bill, container, false);

        return binding.getRoot();
    }
}