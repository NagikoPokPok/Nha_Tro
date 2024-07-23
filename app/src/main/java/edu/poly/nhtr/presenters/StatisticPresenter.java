package edu.poly.nhtr.presenters;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.StatisticListener;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.utilities.Constants;

public class StatisticPresenter {
    private StatisticListener statisticListener;
    private String homeID;

    public StatisticPresenter(StatisticListener statisticListener, String homeID) {
        this.statisticListener = statisticListener;
        this.homeID = homeID;

    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getListRoomByHome(String homeID, OnGetRoomCompleteListener listener) {
        List<Room> roomList = new ArrayList<>();
        db.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Room room = new Room();
                                    room.roomId = document.getId();
                                    room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                    roomList.add(room);
                                }
                            }
                            roomList.sort(Comparator.comparing(Room::getDateObject));
                            listener.onComplete(roomList);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public interface OnGetRoomCompleteListener {
        void onComplete(List<Room> roomList);
    }

    public void getListBillByListRoom(List<Room> roomList, OnGetBillCompleteListener listener)
    {
        List<RoomBill> roomBillList = new ArrayList<RoomBill>();
        for(Room room: roomList){
            db.collection(Constants.KEY_COLLECTION_BILL)
                    .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        RoomBill bill = new RoomBill();
                                        bill.billID = document.getId();
                                        bill.totalOfMoney = Objects.requireNonNull(document.getLong(Constants.KEY_TOTAL_OF_MONEY)).intValue();
                                        bill.month = Objects.requireNonNull(document.getLong(Constants.KEY_MONTH)).intValue();
                                        bill.year = Objects.requireNonNull(document.getLong(Constants.KEY_YEAR)).intValue();
                                        roomBillList.add(bill);
                                    }
                                }
                                roomBillList.sort(Comparator.comparing(RoomBill::getMonth));
                                roomBillList.sort(Comparator.comparing(RoomBill::getYear));
                                listener.onComplete(roomBillList);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    public interface OnGetBillCompleteListener {
        void onComplete(List<RoomBill> roomBillList);
    }
}
