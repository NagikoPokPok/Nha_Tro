package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.Adapter.RoomAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomFragment extends Fragment implements RoomListener, SwipeRefreshLayout.OnRefreshListener {

    private View view;

    private PreferenceManager preferenceManager;
    private FragmentRoomBinding binding;
    private Dialog dialog;
    private RoomPresenter roomPresenter;
    private RoomAdapter roomAdapter;
    private List<Room> currentListRooms = new ArrayList<>();
    private Home home;
    private boolean isLoadingFinished = false;

    public RoomFragment() {
    }

    public List<Room> getCurrentListRooms() {
        return currentListRooms;
    }

    private boolean isCheckBoxClicked = false;

    public void setCurrentListRooms(List<Room> currentListRooms) {
        this.currentListRooms = currentListRooms;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        dialog = new Dialog(requireActivity());
        assert getArguments() != null;
        home = (Home) getArguments().getSerializable("home");
        assert home != null;
        String homeId = home.getIdHome();
        String nameHome = home.getNameHome();
        preferenceManager.putString(Constants.KEY_HOME_ID, homeId);
        preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);
        roomPresenter = new RoomPresenter(this);
        binding = FragmentRoomBinding.inflate(getLayoutInflater());

        binding.rootLayoutRoom.setOnClickListener(v -> {
            binding.edtSearchRoom.clearFocus();
            binding.rootLayoutRoom.requestFocus();
        });

        binding.rootLayoutRoom.setOnRefreshListener(this);


        editFonts();

        //Set preference
        preferenceManager = new PreferenceManager(requireContext().getApplicationContext());


        // Set up RecyclerView layout manager
        binding.roomsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));


        // Load room information
        roomPresenter.getRooms("init");

        roomAdapter = new RoomAdapter(requireContext(), getCurrentListRooms(), this, roomPresenter, this);

        removeStatusOfCheckBoxFilterRoom();


        // Xử lý Dialog Thêm phòng trọ
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding.btnAddRoom.setOnClickListener(view -> {
            openAddRoomDialog(Gravity.CENTER);
        });


        customizeLayoutSearch();
        setListenersForTools(); // Set listeners for sort and filter
    }


    private void customizeLayoutSearch() {
        binding.layoutSearchRoom.setEndIconDrawable(R.drawable.ic_search_orange);
        binding.layoutSearchRoom.setEndIconVisible(true);
        binding.layoutSearchRoom.setHint("Tìm kiếm phòng ...");

        binding.edtSearchRoom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.layoutSearchRoom.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });


        binding.edtSearchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchNameRoom = Objects.requireNonNull(binding.edtSearchRoom.getText()).toString().trim();
                roomPresenter.searchRoom(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.layoutSearchRoom.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchNameRoom = Objects.requireNonNull(binding.edtSearchRoom.getText().toString().trim());
                roomPresenter.searchRoom(searchNameRoom);
                if (searchNameRoom.isEmpty()) {
                    binding.edtSearchRoom.clearFocus();
                }

            }
        });
    }

    private void editFonts() {
        //Set three fonts into one textview
        Spannable text1 = new SpannableString("Bạn chưa có phòng\n Hãy nhấn nút ");
        Typeface interLightTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.setText(text1);

        Spannable text2 = new SpannableString("+");
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text2);

        Spannable text3 = new SpannableString(" để thêm phòng");
        Typeface interLightTypeface2 = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text3);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_room, container, false);

        // Kiểm tra xem Bundle có tồn tại hay không
        if (getArguments() != null) {
            // Nhận dữ liệu từ Bundle
            Home home = (Home) getArguments().getSerializable("home");
        }
        roomAdapter = new RoomAdapter(requireContext(), getCurrentListRooms(), this, roomPresenter, this);


        return binding.getRoot();
    }

    private void openMenuForEachRoom(View view, Room room, ItemContainerRoomBinding binding) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Thực hiện hành động cho mục chỉnh sửa
                openUpdateRoomDialog(Gravity.CENTER, room);
                return true;
            } else if (itemId == R.id.menu_delete) {
                // Thực hiện hành động cho mục xóa
                openDeleteRoomDialog(Gravity.CENTER, room);
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                binding.frmImage2.setVisibility(View.INVISIBLE);
                binding.frmImage.setVisibility(View.VISIBLE);
            }
        });

        popupMenu.inflate(R.menu.menu_edit_delete);
        popupMenu.show();
    }


    private void openAddRoomDialog(int gravity) {
        setupDialog(R.layout.layout_dialog_add_room, Gravity.CENTER);
        TextView nameRoom = dialog.findViewById(R.id.txt_name_room);
        TextView title = dialog.findViewById(R.id.txt_title_dialog_room);
        TextView price = dialog.findViewById(R.id.txt_price);
        TextView describe = dialog.findViewById(R.id.txt_describe);

        EditText edtNameRoom = dialog.findViewById(R.id.edt_name_room);
        EditText edtPrice = dialog.findViewById(R.id.edt_price);
        EditText edtDescribe = dialog.findViewById(R.id.edt_describe);

        TextInputLayout layoutNameRoom = dialog.findViewById(R.id.layout_name_room);
        TextInputLayout layoutPrice = dialog.findViewById(R.id.layout_price);

        Button btnAddRoom = dialog.findViewById(R.id.btn_update_room);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Set dấu * đỏ cho TextView
        nameRoom.append(customizeText(" *"));
        price.append(customizeText(" *"));

        //Set thông tin cho dialog
        title.setText("Tạo mới phòng trọ");
        edtNameRoom.setHint("Ví dụ: Phòng 1");
        edtPrice.setHint("Ví dụ: 1.500.000");
        edtDescribe.setHint("Ví dụ: View Biển");
        btnAddRoom.setText("Tạo");


        edtNameRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtNameRoom.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutNameRoom.setErrorEnabled(false);
                    layoutNameRoom.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtPrice.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private static final int MAX_DIGITS = 12;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    edtPrice.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("\\D", "");

                    if (!cleanString.isEmpty()) {
                        if (cleanString.length() > MAX_DIGITS) {
                            layoutPrice.setError("Không được nhập quá 9 chữ số");
                        } else {
                            layoutPrice.setError(null);
                        }

                        cleanString = cleanString.length() > MAX_DIGITS ? cleanString.substring(0, MAX_DIGITS) : cleanString;

                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(parsed);

                        current = formatted;
                        edtPrice.setText(formatted);
                        edtPrice.setSelection(formatted.length());
                    } else {
                        current = "";
                        edtPrice.setText("");
                        layoutPrice.setError(null);
                    }

                    edtPrice.addTextChangedListener(this);

                    String price = edtPrice.getText().toString().trim();
                    if (!price.isEmpty()) {
                        layoutPrice.setErrorEnabled(false);
                        layoutPrice.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // Xử lý/ hiệu chỉnh màu nút button add room
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameRoom, edtPrice, btnAddRoom);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edtNameRoom.addTextChangedListener(textWatcher);
        edtPrice.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho button
        btnAddRoom.setOnClickListener(v -> {
            String name = edtNameRoom.getText().toString().trim();
            String priceRoom = edtPrice.getText().toString().trim();
            String describeRoom = edtDescribe.getText().toString().trim();
            Room room = new Room(name, priceRoom, describeRoom);
            roomPresenter.addRoom(room);

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());


    }

    // Cập nhật màu cho button
    private void updateButtonState(EditText edtNameRoom, EditText edtPrice, Button btn) {
        String name = edtNameRoom.getText().toString().trim();
        String address = edtPrice.getText().toString().trim();
        if (name.isEmpty() || address.isEmpty()) {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        } else {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
        }
    }


    private Spannable customizeText(String s)  // Hàm set mau va font chu cho Text
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }


    @Override
    public void openPopup(View view, Room room, ItemContainerRoomBinding binding) {
        openMenuForEachRoom(view, room, binding);
    }


    @Override
    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_room = dialog.findViewById(id);
        layout_name_room.setError(message);

    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    @Override
    public void putRoomInfoInPreferences(String nameRoom, String priceRoom, String describeRoom, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_NAME_ROOM, documentReference.getId());
        preferenceManager.putString(Constants.KEY_PRICE, priceRoom);
        preferenceManager.putString(Constants.KEY_DESCRIBE, describeRoom);
    }

    @Override
    public void dialogClose() {
        dialog.dismiss();
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.roomsRecyclerView.setVisibility(View.VISIBLE);

        isLoadingFinished = true;
    }

    @Override
    public void showLoading() {
        binding.roomsRecyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void addRoom(List<Room> rooms, String action) {
        RoomAdapter roomAdapter = new RoomAdapter(requireContext(), rooms, this, roomPresenter, this);
        binding.roomsRecyclerView.setAdapter(roomAdapter);
        if (Objects.equals(action, "init") || Objects.equals(action, "search")) {
            // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
            rooms.sort(Comparator.comparing(obj -> obj.dateObject));
            binding.roomsRecyclerView.smoothScrollToPosition(0);
        } else if (Objects.equals(action, "add")) {
            // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
            rooms.sort(Comparator.comparing(obj -> obj.dateObject));
            roomAdapter.addRoom(rooms);
            roomAdapter.notifyItemInserted(roomAdapter.getLastActionPosition());
            binding.roomsRecyclerView.smoothScrollToPosition(roomAdapter.getLastActionPosition());
        } else if (Objects.equals(action, "sort")) {
            //roomAdapter.addRoom(rooms);
            updateRecyclerView(roomAdapter, 0);
        } else if (Objects.equals(action, "update")) {
            int position = roomPresenter.getPosition();
            roomAdapter.updateRoom(position);
            roomAdapter.notifyItemChanged(roomAdapter.getLastActionPosition());
            binding.roomsRecyclerView.smoothScrollToPosition(roomAdapter.getLastActionPosition());
        } else if (Objects.equals(action, "delete")) {
            int position = roomPresenter.getPosition();
            if (position == 1) {
                binding.roomsRecyclerView.smoothScrollToPosition(0);
            } else {
                roomAdapter.removeRoom(position);
                roomAdapter.notifyItemRemoved(roomAdapter.getLastActionPosition());
                binding.roomsRecyclerView.smoothScrollToPosition(roomAdapter.getLastActionPosition());
            }
        }
        //binding.homesRecyclerView.smoothScrollToPosition(0);

        // Do trong activity_users.xml, usersRecycleView đang được setVisibility là Gone, nên sau
        // khi setAdapter mình phải set lại là VISIBLE
        binding.txtNotification.setVisibility(View.GONE);
        binding.imgAddHome.setVisibility(View.GONE);
        binding.roomsRecyclerView.setVisibility(View.VISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
        Log.d("RoomFragment", "Adapter set successfully");
    }

    private void updateRecyclerView(RoomAdapter roomAdapter, int position) {
        binding.roomsRecyclerView.setAdapter(roomAdapter);
        //binding.layoutNoData.setVisibility(View.GONE);
        roomAdapter.notifyDataSetChanged();
        binding.roomsRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void addRoomFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddHome.setVisibility(View.VISIBLE);
        binding.roomsRecyclerView.setVisibility(View.INVISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
    }

    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void hideFrameTop() {
        binding.frmMenuTools.setVisibility(View.GONE);
        binding.btnAddRoom.setVisibility(View.GONE);
    }

    @Override
    public void showFrameTop() {
        binding.frmMenuTools.setVisibility(View.VISIBLE);
        binding.btnAddRoom.setVisibility(View.VISIBLE);
    }

    @Override
    public void openDialogSuccess(int id) {
        setupDialog(id, Gravity.CENTER);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void showLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    private void openUpdateRoomDialog(int gravity, Room room) {

        setupDialog(R.layout.layout_dialog_update_room, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_update_room = dialog.findViewById(R.id.btn_update_room);
        EditText edt_new_name_room = dialog.findViewById(R.id.edt_name_room);
        EditText edt_new_price = dialog.findViewById(R.id.edt_price);
        EditText edt_new_describe = dialog.findViewById(R.id.edt_describe);
        TextView title = dialog.findViewById(R.id.txt_title_dialog_room);
        TextView txt_name_room = dialog.findViewById(R.id.txt_name_room);
        TextView txt_price = dialog.findViewById(R.id.txt_price);
        TextInputLayout layoutNameRoom = dialog.findViewById(R.id.layout_name_room);
        TextInputLayout layotPrice = dialog.findViewById(R.id.layout_price);


        //Hiện thông tin lên edt
        edt_new_name_room.setText(room.getNameRoom());
        edt_new_price.setText(room.getPrice());
        edt_new_describe.setText(room.getDescribe());
        title.setText("Chỉnh sửa thông tin phòng trọ");
        btn_update_room.setText("Cập nhật");
        txt_name_room.append(customizeText(" *"));
        txt_price.append(customizeText(" *"));
        btn_update_room.setBackground(getResources().getDrawable(R.drawable.custom_button_add));

        edt_new_name_room.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edt_new_name_room.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutNameRoom.setErrorEnabled(false);
                    layoutNameRoom.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_new_price.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private static final int MAX_DIGITS = 12;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    edt_new_price.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("\\D", "");

                    if (!cleanString.isEmpty()) {
                        if (cleanString.length() > MAX_DIGITS) {
                            layotPrice.setError("Không được nhập quá 9 chữ số");
                        } else {
                            layotPrice.setError(null);
                        }

                        cleanString = cleanString.length() > MAX_DIGITS ? cleanString.substring(0, MAX_DIGITS) : cleanString;

                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(parsed);

                        current = formatted;
                        edt_new_price.setText(formatted);
                        edt_new_price.setSelection(formatted.length());
                    } else {
                        current = "";
                        edt_new_price.setText("");
                        edt_new_price.setError(null);
                    }

                    edt_new_price.addTextChangedListener(this);

                    String price = edt_new_price.getText().toString().trim();
                    if (!price.isEmpty()) {
                        layotPrice.setErrorEnabled(false);
                        layotPrice.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edt_new_name_room, edt_new_price, btn_update_room);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edt_new_name_room.addTextChangedListener(textWatcher);
        edt_new_price.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(v -> dialog.dismiss());

        btn_update_room.setOnClickListener(v -> {
            // Lấy dữ liệu
            String newNameRoom = edt_new_name_room.getText().toString().trim();
            String newPrice = edt_new_price.getText().toString().trim();
            String newDescribe = edt_new_describe.getText().toString().trim();

            roomPresenter.updateRoom(newNameRoom, newPrice, newDescribe, room);
        });
    }

    @Override
    public void openConfirmUpdateRoom(int gravity, String newNameRoom, String newPrice, String newDescribe, Room room) {
        setupDialog(R.layout.layout_dialog_confirm_update_room, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_confirm_update_home = dialog.findViewById(R.id.btn_confirm_update_room);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_confirm_update_home.setOnClickListener(v -> roomPresenter.updateSuccess(newNameRoom, newPrice, newDescribe, room));
    }


    private void openDeleteRoomDialog(int gravity, Room room) {

        setupDialog(R.layout.layout_dialog_delete_room, Gravity.CENTER);

        // Ánh xạ ID
        TextView txt_confirm_delete = dialog.findViewById(R.id.txt_confirm_delete);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_delete_room = dialog.findViewById(R.id.btn_delete_room);

        // Hiệu chỉnh TextView
        String text = " " + room.getNameRoom() + " ?";
        txt_confirm_delete.append(text);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_delete_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomPresenter.deleteRoom(room);
            }
        });
    }


    private void setupDialog(int layoutId, int gravity) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = gravity;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(Gravity.CENTER == gravity);
            dialog.show();
        }
    }

    // Xử lí click vào container Room
    @Override
    public void onRoomClick(Room room) {
        binding.edtSearchRoom.clearFocus();
        Intent intent = new Intent(getContext(), MainDetailedRoomActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("home", home);
        intent.putExtra("room_price", room.getPrice());
        startActivity(intent);
    }

    @Override
    public void getListRooms(List<Room> listRoom) {
        setCurrentListRooms(listRoom);
    }

    @Override
    public void noRoomData() {
        binding.roomsRecyclerView.setVisibility(View.INVISIBLE);
        binding.layoutNoRoom.setVisibility(View.VISIBLE);
    }

    @Override
    public void setDelectAllUI() {
        binding.layoutDeleteAll.setVisibility(View.VISIBLE);
        binding.btnAddRoom.setVisibility(View.GONE);
    }

    @Override
    public void cancelDelectAll() {
        binding.layoutDeleteAll.setVisibility(View.GONE);
        binding.btnAddRoom.setVisibility(View.VISIBLE);
    }

    @Override
    public void openDeleteListDialog(List<Room> listRoom) {
        dialog.setContentView(R.layout.layout_dialog_delete_room);
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
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_delete_room = dialog.findViewById(R.id.btn_delete_room);


        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_delete_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                roomPresenter.deleteListRooms(listRoom);

            }
        });

    }

    @Override
    public void deleteListAll(List<Room> list) {
        binding.txtDeleteHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteListDialog(list);
            }
        });
        binding.txtCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomAdapter.cancelDeleteAll();
            }
        });

    }

    @Override
    public void dialogAndModeClose(ActionMode mode) {
        dialog.dismiss();
        mode.finish();
    }


    private void openSortRoomDialog() {

        if (binding.edtSearchRoom.isFocused()) {
            binding.edtSearchRoom.clearFocus();
            binding.edtSearchRoom.setText("");
            roomPresenter.getRooms("init");
        } else if (binding.layoutTypeOfFilterRoom.getVisibility() == View.VISIBLE) {
            removeStatusOfCheckBoxFilterRoom();
            binding.layoutTypeOfFilterRoom.setVisibility(View.GONE);
            roomPresenter.getRooms("init");
        }

        setupDialog(R.layout.layout_dialog_sort_room, Gravity.CENTER);

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group_sort_room);
        Button btnApply = dialog.findViewById(R.id.btn_confirm_apply);

        // Disable btnApply and set background color to gray initially
        btnApply.setEnabled(false);
        btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));


        // Listen for changes in the RadioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    // Enable btnApply and change background color to blue
                    btnApply.setEnabled(true);
                    btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
                }
            }
        });


        // Save the status of radio buttons
        int selectedRadioButtonId = preferenceManager.getInt(Constants.KEY_SELECTED_RADIO_BUTTON);

        // If a RadioButton was selected before, check it
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = dialog.findViewById(selectedRadioButtonId);
            if (selectedRadioButton != null) {
                selectedRadioButton.setChecked(true);
            }
        }

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.txtTitleSortFilterRoom.setText("Sắp xếp theo:  ");
                binding.layoutTypeOfSortRoom.setVisibility(View.VISIBLE);

                // Get the selected RadioButton ID
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.radio_btn_number_people_living_asc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_number_people_living_asc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterRoom.setText(selectedText);
                    roomPresenter.sortRooms("number_of_people_living_asc", currentListRooms);

                } else if (selectedId == R.id.radio_btn_price_asc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_price_asc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterRoom.setText(selectedText);
                    roomPresenter.sortRooms("price_asc", currentListRooms);

                } else if (selectedId == R.id.radio_btn_name_room) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_name_room);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterRoom.setText(selectedText);
                    roomPresenter.sortRooms("name_room", currentListRooms);

                } else {
                    showToast("No option selected");
                }

                // Save the selected RadioButton ID to SharedPreferences
                preferenceManager.putInt(Constants.KEY_SELECTED_RADIO_BUTTON, selectedId);
            }
        });

        binding.btnCancelSortRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBackgroundOfFrameButton(binding.frameRoundSortHome, binding.imgSortRoom);
                binding.layoutTypeOfSortRoom.setVisibility(View.GONE);
                preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
                roomPresenter.getRooms("init");
            }
        });


        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.layoutTypeOfSortRoom.getVisibility() == View.GONE) {
                    removeBackgroundOfFrameButton(binding.frameRoundSortHome, binding.imgSortRoom);
                }
                dialog.dismiss();
            }
        });
    }

    public void removeStatusOfCheckBoxFilterRoom() {
        preferenceManager.removePreference("cbxRoom1");
        preferenceManager.removePreference("cbxRoom2");
        preferenceManager.removePreference("cbxRoom3");
        preferenceManager.removePreference("cbxRoom4");
    }

    private void openFilterRoomDialog() {

        if (binding.edtSearchRoom.isFocused()) {
            binding.edtSearchRoom.clearFocus();
            binding.edtSearchRoom.setText("");
            roomPresenter.getRooms("init");
        } else if (binding.layoutTypeOfSortRoom.getVisibility() == View.VISIBLE) {
            // Clear the selected RadioButton ID from SharedPreferences
            preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
            binding.layoutTypeOfSortRoom.setVisibility(View.GONE);
            roomPresenter.getRooms("init");
        }


        setupDialog(R.layout.layout_dialog_filter_room, Gravity.CENTER);
        //roomPresenter.getListRooms();

        AppCompatCheckBox cbxByRoom1 = dialog.findViewById(R.id.cbx_paid);
        AppCompatCheckBox cbxByRoom2 = dialog.findViewById(R.id.cbx_waiting_for_paid);
        AppCompatCheckBox cbxByRoom3 = dialog.findViewById(R.id.cbx_overdue_paid);
        AppCompatCheckBox cbxByRoom4 = dialog.findViewById(R.id.cbx_no_people_rent);

        Button btnApply = dialog.findViewById(R.id.btn_confirm_apply);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        cbxByRoom1.setChecked(preferenceManager.getBoolean("cbxRoom1"));
        cbxByRoom2.setChecked(preferenceManager.getBoolean("cbxRoom2"));
        cbxByRoom3.setChecked(preferenceManager.getBoolean("cbxRoom3"));
        cbxByRoom4.setChecked(preferenceManager.getBoolean("cbxRoom4"));

        // Add CheckBoxes to a list
        List<AppCompatCheckBox> checkBoxList = new ArrayList<>();
        checkBoxList.add(cbxByRoom1);
        checkBoxList.add(cbxByRoom2);
        checkBoxList.add(cbxByRoom3);
        checkBoxList.add(cbxByRoom4);


        customizeButtonApplyInDialogHaveCheckBox(btnApply, checkBoxList);


        // Create a method to check the state of all checkboxes
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeButtonApplyInDialogHaveCheckBox(btnApply, checkBoxList);
            }
        };

        // Set the listener to all checkboxes
        cbxByRoom1.setOnClickListener(checkBoxListener);
        cbxByRoom2.setOnClickListener(checkBoxListener);
        cbxByRoom3.setOnClickListener(checkBoxListener);
        cbxByRoom4.setOnClickListener(checkBoxListener);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutTypeOfFilterRoom.setVisibility(View.VISIBLE);
                List<String> selectedOptions = new ArrayList<>();

                // Check which CheckBoxes are selected and save their state
                boolean filterByRoom1 = cbxByRoom1.isChecked();
                boolean filterByRoom2 = cbxByRoom2.isChecked();
                boolean filterByRoom3 = cbxByRoom3.isChecked();
                boolean filterByRoom4 = cbxByRoom4.isChecked();

                if (filterByRoom1) {
                    selectedOptions.add(cbxByRoom1.getText().toString());
                    preferenceManager.putBoolean("cbxRoom1", true);
                } else {
                    preferenceManager.putBoolean("cbxRoom1", false);
                    removeFromListAndSave(cbxByRoom1.getText().toString());
                }
                if (filterByRoom2) {
                    selectedOptions.add(cbxByRoom2.getText().toString());
                    preferenceManager.putBoolean("cbxRoom2", true);
                } else {
                    preferenceManager.putBoolean("cbxRoom2", false);
                    removeFromListAndSave(cbxByRoom2.getText().toString());
                }
                if (filterByRoom3) {
                    selectedOptions.add(cbxByRoom3.getText().toString());
                    preferenceManager.putBoolean("cbxRoom3", true);
                } else {
                    preferenceManager.putBoolean("cbxRoom3", false);
                    removeFromListAndSave(cbxByRoom3.getText().toString());
                }
                if (filterByRoom4) {
                    selectedOptions.add(cbxByRoom4.getText().toString());
                    preferenceManager.putBoolean("cbxRoom4", true);
                } else {
                    preferenceManager.putBoolean("cbxRoom4", false);
                    removeFromListAndSave(cbxByRoom4.getText().toString());
                }

                // If 3 check boxes are unchecked -> Hide layoutTypeOfFilterHomes
                if (!filterByRoom1 && !filterByRoom2 && !filterByRoom3 && !filterByRoom4) {
                    binding.layoutNoRoom.setVisibility(View.GONE);
                    binding.layoutTypeOfFilterRoom.setVisibility(View.GONE);
                    roomPresenter.getRooms("init");
                } else {
                    filterListRooms(currentListRooms); // After put status of checkboxes in preferences, check and add them into the list
                }

                // Add selected options as LinearLayouts with TextView and ImageView to the main LinearLayout
                for (String option : selectedOptions) {
                    // Check if the checkbox is already in the listTypeOfFilterHome
                    boolean alreadyExists = false;
                    for (int i = 0; i < binding.listTypeOfFilterRoom.getChildCount(); i++) {
                        View view = binding.listTypeOfFilterRoom.getChildAt(i);
                        if (view instanceof LinearLayout) {
                            TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                            if (textView.getText().toString().equals(option)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }


                    // If the checkbox does not exist, add it to the listTypeOfFilterHome
                    if (!alreadyExists) {

                        // Inflate the layout containing the TextView and ImageView
                        LinearLayout filterItemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_filter_home_layout, null);

                        // Get references to the TextView and ImageView
                        TextView txtTypeOfFilterRoom = filterItemLayout.findViewById(R.id.txt_type_of_filter_home);
                        ImageView iconCancel = filterItemLayout.findViewById(R.id.btn_cancel_filter_home);

                        // Set the text for the TextView
                        txtTypeOfFilterRoom.setText(option);

                        // Optionally set an OnClickListener for the ImageView to remove the filter
                        iconCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Remove the filter
                                binding.listTypeOfFilterRoom.removeView(filterItemLayout);

                                // Update SharedPreferences to uncheck the checkbox in the dialog
                                if (option.equals(cbxByRoom1.getText().toString())) {
                                    preferenceManager.putBoolean("cbxRoom1", false);
                                    cbxByRoom1.setChecked(false);
                                } else if (option.equals(cbxByRoom2.getText().toString())) {
                                    preferenceManager.putBoolean("cbxRoom2", false);
                                    cbxByRoom2.setChecked(false);
                                } else if (option.equals(cbxByRoom3.getText().toString())) {
                                    preferenceManager.putBoolean("cbxRoom3", false);
                                    cbxByRoom3.setChecked(false);
                                } else if (option.equals(cbxByRoom4.getText().toString())) {
                                    preferenceManager.putBoolean("cbxRoom4", false);
                                    cbxByRoom4.setChecked(false);
                                }

                                if (binding.listTypeOfFilterRoom.getChildCount() == 0) {
                                    // If no filter left in the list -> Set GONE
                                    binding.layoutTypeOfFilterRoom.setVisibility(View.GONE);
                                    binding.layoutNoRoom.setVisibility(View.GONE);
                                    removeBackgroundOfFrameButton(binding.frameRoundFilterRoom, binding.imgFilterRoom);
                                    // And update list homes as initial
                                    roomPresenter.getRooms("init");
                                } else {
                                    // Update list homes after deleting some check boxes
                                    filterListRooms(currentListRooms);
                                }

                            }
                        });

                        // Set layout parameters for filterItemLayout
                        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                (int) getResources().getDimension(R.dimen.filter_item_height) // Assuming filter_item_height is 40dp
                        );
                        int margin = (int) getResources().getDimension(R.dimen.filter_item_margin);
                        params.setMargins(0, margin, margin, 0);
                        filterItemLayout.setLayoutParams(params);


                        // Add the inflated layout to the main LinearLayout
                        binding.listTypeOfFilterRoom.addView(filterItemLayout);
                    }
                }
                //dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.layoutTypeOfFilterRoom.getVisibility() == View.GONE) {
                    removeBackgroundOfFrameButton(binding.frameRoundFilterRoom, binding.imgFilterRoom);
                }
                dialog.dismiss();
            }
        });
    }

    private void customizeButtonApplyInDialogHaveCheckBox(Button btnApply, List<AppCompatCheckBox> checkBoxList) {
        boolean isAnyChecked = false;
        for (AppCompatCheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                isAnyChecked = true;
                break;
            }
        }
        if (isAnyChecked) {
            btnApply.setEnabled(true);
            btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
        } else {
            btnApply.setEnabled(false);
            btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        }
    }

    private void removeFromListAndSave(String option) { // Remove from listType
        for (int i = 0; i < binding.layoutTypeOfFilterRoom.getChildCount(); i++) {
            View view = binding.layoutTypeOfFilterRoom.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                if (textView.getText().toString().equals(option)) {
                    binding.layoutTypeOfFilterRoom.removeView(view);
                    preferenceManager.removePreference(option);
                    break;
                }
            }
        }
    }

    private void filterListRooms(List<Room> currentListRooms) {
        showLoadingOfFunctions(R.id.btn_confirm_apply);
        boolean filterByRoom1 = preferenceManager.getBoolean("cbxRoom1");
        boolean filterByRoom2 = preferenceManager.getBoolean("cbxRoom2");
        boolean filterByRoom3 = preferenceManager.getBoolean("cbxRoom3");
        boolean filterByRoom4 = preferenceManager.getBoolean("cbxRoom4");


        List<Room> filteredNoMembers = new ArrayList<>();
        for (Room room : currentListRooms) {
            showToast(room.getNumberOfMemberLiving());
            int numberOfMembers = Integer.parseInt(room.getNumberOfMemberLiving());
            if (filterByRoom4 && numberOfMembers == 0) {
                filteredNoMembers.add(room);
            } else if (filterByRoom1 && room.getStatus().equals("Đã thanh toán")) {
                filteredNoMembers.add(room);
            } else if (filterByRoom2 && room.getStatus().equals("Chưa thanh toán")) {
                filteredNoMembers.add(room);
            } else if (filterByRoom3 && room.getStatus().equals("Trễ hạn thanh toán")) {
                filteredNoMembers.add(room);
            }
        }


        roomPresenter.filterRoom(filteredNoMembers);
    }

    private void setListenersForTools() {
        binding.btnSortRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSortRoom();
            }
        });

        binding.imgSortRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSortRoom();
            }
        });

        binding.btnFilterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFilterRoom();
            }
        });

        binding.imgFilterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFilterRoom();
            }
        });

    }

    private void clickSortRoom() {
        removeBackgroundOfFrameButton(binding.frameRoundFilterRoom, binding.imgFilterRoom);
        changeBackgroundOfFrameButton(binding.frameRoundSortHome, binding.imgSortRoom);
        openSortRoomDialog();
    }

    private void clickFilterRoom() {
        changeBackgroundOfFrameButton(binding.frameRoundFilterRoom, binding.imgFilterRoom);
        removeBackgroundOfFrameButton(binding.frameRoundSortHome, binding.imgSortRoom);
        openFilterRoomDialog();
    }

    private void changeBackgroundOfFrameButton(RoundedImageView roundedImageView, ImageButton imageButton) {
        roundedImageView.setBackground(getResources().getDrawable(R.drawable.custom_btn_sort));
        imageButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
    }

    private void removeBackgroundOfFrameButton(RoundedImageView roundedImageView, ImageButton imageButton) {
        roundedImageView.setBackground(getResources().getDrawable(R.drawable.background_delete_index_normal));
        imageButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
    }


    //    private void setupDeleteRows() {
//        binding.layoutDelete.setOnClickListener(v -> {
//            binding.btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
//            binding.txtDelete.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
//            binding.layoutDelete.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
//            binding.layoutDeleteManyRows.setVisibility(View.VISIBLE);
//            adapter.isDeleteClicked(true);
//        });
//
//        binding.checkboxSelectAll.setOnClickListener(v -> {
//            if (isCheckBoxClicked) {
//                binding.checkboxSelectAll.setChecked(false);
//                adapter.isCheckBoxClicked(false);
//                isCheckBoxClicked = false;
//            } else {
//                binding.checkboxSelectAll.setChecked(true);
//                adapter.isCheckBoxClicked(true);
//                isCheckBoxClicked = true;
//            }
//        });
//    }
    @Override
    public void onResume() {
        super.onResume();
        roomPresenter.getRooms("init");
    }

    @Override
    public void onRefresh() {
        isLoadingFinished = false;
        roomPresenter.getRooms("init");

        // Sử dụng Handler để kiểm tra trạng thái tải
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoadingFinished) {
                    binding.rootLayoutRoom.setRefreshing(false);
                } else {
                    // Kiểm tra lại sau một khoảng thời gian ngắn nếu cần thiết
                    new Handler(Looper.getMainLooper()).postDelayed(this, 500);
                }
            }
        }, 500); // Thời gian kiểm tra ban đầu
    }
}