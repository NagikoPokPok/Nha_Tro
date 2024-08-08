package edu.poly.nhtr.presenters;

import android.content.Intent;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.RoomGuestViewInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class RoomViewGuestPresenter implements RoomGuestViewInterface.Presenter {

    private final RoomGuestViewInterface.View view;
    private final FirebaseFirestore db;

    public RoomViewGuestPresenter(RoomGuestViewInterface.View view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void fetchGuestDetails(String guestId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_GUESTS)
                .document(guestId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        Guest guest = new Guest();
                        guest.setGuestId(document.getId());
                        guest.setNameGuest(document.getString(Constants.KEY_GUEST_NAME));
                        guest.setPhoneGuest(document.getString(Constants.KEY_GUEST_PHONE));
                        guest.setDateIn(document.getString(Constants.KEY_GUEST_DATE_IN));
                        guest.setCccdNumber(document.getString(Constants.KEY_GUEST_CCCD));
                        guest.setCccdImageFront(document.getString(Constants.KEY_GUEST_CCCD_IMAGE_FRONT));
                        guest.setCccdImageBack(document.getString(Constants.KEY_GUEST_CCCD_IMAGE_BACK));
                        guest.setFileStatus(document.getBoolean(Constants.KEY_CONTRACT_STATUS));
                        view.showGuestDetails(guest);
                    }
                });
    }

    public void checkMainGuest(String guestId, OnMainGuestCheckListener listener) {
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .document(guestId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        Boolean isMainGuest = document.getBoolean(Constants.KEY_IS_MAIN_GUEST);
                        listener.onCheckCompleted(isMainGuest != null && isMainGuest);
                    } else {
                        listener.onCheckFailed(task.getException());
                    }
                });
    }

    public interface OnMainGuestCheckListener {
        void onCheckCompleted(boolean isMainGuest);
        void onCheckFailed(Exception e);
    }


    @Override
    public void deleteGuest(Guest guest) {
        view.showLoadingOfFunctions(R.id.btn_delete_guest);
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, view.getInfoRoomFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentSnapshots = sortDocumentsByTimestamp(task.getResult().getDocuments());
                        boolean found = checkIfGuestExists(documentSnapshots, guest.getGuestId());

                        if (found) {
                            db.collection(Constants.KEY_COLLECTION_GUESTS)
                                    .document(guest.getGuestId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                                        view.dialogClose();
                                        view.openDialogSuccess(R.layout.layout_dialog_delete_guest_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                                        view.showToast("Xoá khách thất bại");
                                    });
                        } else {
                            view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                            view.showToast("Không tìm thấy homeId");
                        }
                    } else {
                        view.hideLoadingOfFunctions(R.id.btn_delete_guest);
                        view.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }

    private List<DocumentSnapshot> sortDocumentsByTimestamp(List<DocumentSnapshot> documents) {
        documents.sort((o1, o2) -> {
            Timestamp timestamp1 = o1.getTimestamp(Constants.KEY_TIMESTAMP);
            Timestamp timestamp2 = o2.getTimestamp(Constants.KEY_TIMESTAMP);
            if (timestamp1 != null && timestamp2 != null) {
                return timestamp1.compareTo(timestamp2);
            }
            return 0;
        });
        return documents;
    }

    private boolean checkIfGuestExists(List<DocumentSnapshot> documentSnapshots, String guestId) {
        for (DocumentSnapshot doc : documentSnapshots) {
            if (guestId.equals(doc.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateGuestInFirebase(Guest guest) {
        view.showLoadingOfFunctions(R.id.btn_add_new_guest);
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, view.getInfoRoomFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentSnapshots = sortDocumentsByTimestamp(task.getResult().getDocuments());
                        boolean found = checkIfGuestExists(documentSnapshots, guest.getGuestId());

                        if (found) {
                            updateGuestInfoInFirebase(guest);
                        } else {
                            view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                            view.showToast("Không tìm thấy khách");
                        }
                    } else {
                        view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                        view.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }

    private void updateGuestInfoInFirebase(Guest guest) {
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_GUEST_NAME, guest.getNameGuest());
        updateInfo.put(Constants.KEY_GUEST_PHONE, guest.getPhoneGuest());
        updateInfo.put(Constants.KEY_GUEST_DATE_IN, guest.getDateIn());
        updateInfo.put(Constants.KEY_CONTRACT_STATUS, guest.isFileStatus());

        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .document(guest.getGuestId())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    fetchGuestDetails(guest.getGuestId());
                    view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                    view.dialogClose();
                    view.openDialogSuccess(R.layout.layout_dialog_update_guest_success);
                })
                .addOnFailureListener(e -> {
                    view.hideLoadingOfFunctions(R.id.btn_add_new_guest);
                    view.showToast("Cập nhật thông tin khách thất bại");
                });
    }

    @Override
    public void handleNameChanged(String name, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) return;

        if (TextUtils.isEmpty(name)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!isValidName(name)) {
            textInputLayout.setError("Tên không được chứa số hoặc ký tự đặc biệt");
        } else {
            textInputLayout.setErrorEnabled(false);
            textInputLayout.setBoxStrokeColor(boxStrokeColor);
        }
    }

    private boolean isValidName(String name) {
        String regex = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỂưạảấầẩẫậắằẳẵặẹẻẽềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễếệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\\s]+$";
        return name.matches(regex);
    }

    @Override
    public void handlePhoneChanged(String phone, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) return;

        if (TextUtils.isEmpty(phone)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!isValidPhoneNumber(phone)) {
            textInputLayout.setError("Số điện thoại không hợp lệ");
        } else {
            checkDuplicatePhoneNumber(phone, textInputLayout, boxStrokeColor);
        }
    }

    private boolean isValidPhoneNumber(CharSequence target) {
        return target.length() == 10 && android.util.Patterns.PHONE.matcher(target).matches();
    }

    private void checkDuplicatePhoneNumber(String phone, TextInputLayout textInputLayout, int boxStrokeColor) {
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_HOME_ID, view.getInfoHomeFromGoogleAccount())
                .whereEqualTo(Constants.KEY_GUEST_PHONE, phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            textInputLayout.setError("Số điện thoại đã tồn tại trong nhà trọ này");
                        } else {
                            textInputLayout.setError(null);
                            textInputLayout.setBoxStrokeColor(boxStrokeColor);
                        }
                    } else {
                        textInputLayout.setError("Có lỗi xảy ra khi kiểm tra số điện thoại");
                    }
                });
    }

    @Override
    public void handleCheckInDateChanged(String checkInDate, String roomId, TextInputLayout textInputLayout, int boxStrokeColor) {
        if (textInputLayout == null) return;

        if (TextUtils.isEmpty(checkInDate)) {
            textInputLayout.setError("Không được bỏ trống");
        } else {
            isCheckInDateValid(checkInDate, roomId, isValid -> {
                if (!isValid) {
                    textInputLayout.setError("Ngày vào ở không hợp lệ");
                } else {
                    textInputLayout.setErrorEnabled(false);
                    textInputLayout.setBoxStrokeColor(boxStrokeColor);
                }
            });
        }
    }

    private void isCheckInDateValid(String inputDate, String roomId, final RoomGuestPresenter.OnDateValidationResult callback) {
        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .whereEqualTo(Constants.KEY_IS_MAIN_GUEST, true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String mainGuestDate = document.getString("checkInDate");

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date inputDateParsed = sdf.parse(inputDate);
                                Date mainGuestDateParsed = sdf.parse(mainGuestDate);

                                callback.onResult(inputDateParsed == null || !inputDateParsed.before(mainGuestDateParsed));
                            } catch (ParseException e) {
                                callback.onResult(false);
                            }
                        }
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    public Intent prepareImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }
}
