package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.Collator;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Adapter.ServiceAdapter;
import edu.poly.nhtr.Class.ServiceUtils;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomServiceBinding;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.listeners.RoomServiceListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.presenters.RoomServicePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomServiceFragment extends Fragment implements RoomServiceListener {
    private PreferenceManager preferenceManager;
    private FragmentRoomServiceBinding binding;
    private Dialog dialog, dialogConfirm;
    private RoomServicePresenter presenter;
    private List<RoomService> roomServices;
    private List<Service> services;
    private String roomId, homeId;
    private Room room;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        roomId = preferenceManager.getString(Constants.KEY_ROOM_ID);
        homeId = preferenceManager.getString(Constants.KEY_HOME_ID);
        presenter = new RoomServicePresenter(this);
        dialog = new Dialog(requireActivity());
        dialogConfirm = new Dialog(requireActivity());

        binding = FragmentRoomServiceBinding.inflate(getLayoutInflater());

        roomServices = new ArrayList<>();
        presenter.getRoomServices(roomId, new RoomServicePresenter.OnGetRoomServiceListener() {
            @Override
            public void onGetRoomService(List<RoomService> listRoomService) {
                roomServices.clear();
                roomServices.addAll(listRoomService);
                Log.e("roomService", "sl: "+roomServices.size());
                if (roomServices.isEmpty()){
                    setAutoRequiredService();
                }else {
                    presenter.getAvailableService(homeId, roomServices, new RoomServicePresenter.OnGetAvailableServiceListener() {
                        @Override
                        public void onGetAvailableService(List<Service> servicesList) {
                            if (services == null) services = new ArrayList<>();
                            services.clear();
                            services.addAll(servicesList);
                            setRecyclerView();
                        }
                    });
                }

                Log.e("roomIdFragment", roomId);
            }
        });
        room = presenter.getRoom(roomId);


        listener();

//        setAutoRequiredService();
    }

    private void setAutoRequiredService() {
            presenter.setNewRoomServiceAuto(homeId, roomId, new RoomServicePresenter.OnRoomServiceAutoSetListener() {
                @Override
                public void onRoomServiceAutoSet() {
                    presenter.getRoomServices(roomId, new RoomServicePresenter.OnGetRoomServiceListener() {
                        @Override
                        public void onGetRoomService(List<RoomService> listRoomService) {
                            roomServices.clear();
                            roomServices.addAll(listRoomService);
                            presenter.getAvailableService(homeId, roomServices, new RoomServicePresenter.OnGetAvailableServiceListener() {
                                @Override
                                public void onGetAvailableService(List<Service> listServices) {
                                    if(services == null) services = new ArrayList<>();
                                    services.clear();
                                    services.addAll(listServices);
                                    setRecyclerView();
                                }
                            });

                        }
                    });
                }
            });

    }

    private void setRecyclerView() {
        //Log.e("serviceList", "1");
        List<Service> serviceList = new ArrayList<>();
        presenter.getServiceOfRoom(roomServices, new RoomServicePresenter.OnGetServiceOfRoomListener() {

            @Override
            public void OnGetServiceOfRoom(List<Service> services) {
                serviceList.clear(); // Xóa các phần tử cũ (nếu có)
                serviceList.addAll(services); // Thêm các phần tử mới
                buildRecyclerView(serviceList); // Gọi phương thức buildRecyclerView với danh sách đã được cập nhật
                Log.e("serviceList", serviceList.size()+"s");
            }
        });
    }

    private void buildRecyclerView(List<Service> serviceList) {
        ServiceAdapter adapter = new ServiceAdapter(this.requireActivity(), serviceList, this, binding.recyclerServiceOfRoom);
        binding.recyclerServiceOfRoom.setAdapter(adapter);
        binding.recyclerServiceOfRoom.setVisibility(View.VISIBLE);
    }

    private void listener() {
        binding.btnAddService.setOnClickListener(v -> openAddServiceDialog());
    }

    private void openAddServiceDialog() {
        setupDialog(dialog, R.layout.room_service_dialog_add_service, Gravity.CENTER);

        //Ánh xạ id
        RecyclerView recyclerViewService = dialog.findViewById(R.id.recycler_applyService);
        Button btn_add = dialog.findViewById(R.id.btn_continue_addNewService);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel_addNewService);
        ImageView exit = dialog.findViewById(R.id.img_exit);

        //Set data
        ServiceAdapter adapter = new ServiceAdapter(requireActivity().getApplicationContext(), services, this, recyclerViewService);
        recyclerViewService.setAdapter(adapter);

        //Set listener
        btn_add.setOnClickListener(v -> {
            presenter.updateDataBeforeAdd(adapter, roomServices, services);
        });
        btn_cancel.setOnClickListener(v -> adapter.cancelChooseListener());
        exit.setOnClickListener(v -> {
            dialog.cancel();
            adapter.cancelChooseListener();
        });
    }

    private void setupDialog(Dialog dialog, int idLayout, int gravity) {
        dialog.setContentView(idLayout);
        Window window = dialog.getWindow();
        if(window != null){
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = gravity;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(Gravity.CENTER == gravity);
            dialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_room__service_, container, false);

        return binding.getRoot();
    }

    @Override
    public void ShowToast(String message) {
        Toast.makeText(this.requireActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onServiceItemCLick(Service service, RecyclerView recyclerView, int position) {
        setupDialog(dialog, R.layout.room_service_dialog_detail, Gravity.CENTER);

        // Ánh xạ id
        ImageView exit = dialog.findViewById(R.id.img_exit);
        ImageView img_service = dialog.findViewById(R.id.image_service);
        TextView txt_name = dialog.findViewById(R.id.txt_name_service);
        TextView txt_price = dialog.findViewById(R.id.txt_fee_service);
        TextView txt_unit = dialog.findViewById(R.id.txt_service_unit);
        EditText edt_quantity = dialog.findViewById(R.id.edt_quantity);
        TextInputLayout layout_quantity = dialog.findViewById(R.id.layout_quantity);
        Button btn_delete = dialog.findViewById(R.id.btn_delete_service);
        Button btn_update = dialog.findViewById(R.id.btn_update_service);

        // Set data
        RoomService roomService = getRoomService(service);

        img_service.setImageBitmap(ServiceUtils.getConversionImage(service.getCodeImage()));
        txt_name.setText(service.getName().toLowerCase());
        txt_price.setText(service.getPrice()+"");

        String temp = "Số lượng " + service.getUnit().toLowerCase();
        String txt_quantity = roomService.getQuantity() + "";
        Log.e("quantity", txt_quantity + "  " + roomService.getQuantity());
        edt_quantity.setText(txt_quantity);
        if(service.getFee_base() == 0){
            edt_quantity.setVisibility(View.GONE);
            txt_unit.setVisibility(View.GONE);
        } else if (service.getFee_base() == 3) {
            edt_quantity.setText(txt_quantity);
            edt_quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
            edt_quantity.setFocusable(true);
            edt_quantity.setCursorVisible(true);
        }
        else {
            edt_quantity.setInputType(InputType.TYPE_NULL);
            edt_quantity.setFocusable(false);
            edt_quantity.setCursorVisible(false);
        }
        txt_unit.setText(temp);

        if(service != null && (service.getName().equalsIgnoreCase("điện") || service.getName().equalsIgnoreCase("nước"))){
            btn_delete.setEnabled(false);
            btn_delete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B1B1B1")));
        }else
            btn_delete.setOnClickListener(v -> openDialogConfirmDelete(service));

        if(service.getFee_base() == 3) {
            edt_quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                }
            });

            btn_update.setOnClickListener(v -> {
                try {
                    int quantity = Integer.parseInt(edt_quantity.getText().toString());
                    if (quantity >= 0)
                        presenter.updateServiceQuantity(quantity, roomService.getRoomServiceId());
                    else
                        layout_quantity.setError("Số lượng phải là số tự nhiên lớn hơn 0");
                }catch (Exception e){
                    layout_quantity.setError("Số lượng phải là số tự nhiên lớn hơn 0");
                }
            });
        }
        else {
            btn_update.setEnabled(false);
            btn_update.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B1B1B1")));
        }

        exit.setOnClickListener(v -> dialog.cancel());

        edt_quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                    if(hasFocus && edt_quantity.getText().toString().equals("0")) edt_quantity.setText("");
                    if(!hasFocus && edt_quantity.getText().toString().isEmpty()) edt_quantity.setText("0");
                
            }
        });

    }

    private RoomService getRoomService(Service service) {
        RoomService roomServicePresent;
        roomServicePresent = roomServices.stream()
                .filter(roomService -> roomService.getServiceId().equals(service.getIdService()))
                .findFirst()
                .orElse(null);
        return roomServicePresent;
    }

    private void openDialogConfirmDelete(Service service) {
        setupDialog(dialogConfirm, R.layout.service_dialog_confirm_delete_service, Gravity.CENTER);

        //Ánh xạ View
        TextView edt_content_confirm = dialogConfirm.findViewById(R.id.txt_content_confirm);
        Button btn_delete = dialogConfirm.findViewById(R.id.btn_delete_service);
        Button btn_cancel = dialogConfirm.findViewById(R.id.btn_cancel);

        //Set data
        edt_content_confirm.setText("Bạn có chắc là muốn bỏ sử dụng dịch vụ này?");

        //Set listener
        btn_cancel.setOnClickListener(v -> dialogConfirm.dismiss());
        btn_delete.setOnClickListener(v -> {
            presenter.deleteService(service, roomId);
        });
    }

    @Override
    public void customPosition(RecyclerView recyclerView, int spanCount) {

    }

    @Override
    public void onServiceClicked(Service service) {

    }

    @Override
    public void openPopup(View view, Service service, ItemServiceBinding binding) {

    }

    @Override
    public void deleteSuccessfully(Service service) {
        dialogConfirm.dismiss();
        dialog.dismiss();

        //add to list of service can add
        services.add(service);
        services.sort(Comparator.comparing(Service::getName, Collator.getInstance(new Locale("vi", "VN"))));

        //remove from list of room service
        roomServices.removeIf(roomService -> roomService.getService() == service);

        //Reset recyclerView
        setRecyclerView();
    }

    @Override
    public void updateSuccessfully(int quantity, String roomServiceId) {
        RoomService roomServicePresent;
        roomServicePresent = roomServices.stream()
                .filter(roomService -> roomService.getRoomServiceId().equals(roomServiceId))
                .findFirst()
                .orElse(null);
        if (roomServicePresent != null) {
            roomServicePresent.setQuantity(quantity);
            ShowToast("Cập nhật thông tin thành công");
        }
    }

    @Override
    public void onChooseServiceClicked(Service service, int position) {
        ShowToast("Hello");
    }

    @Override
    public void updateDataBeforeAddSuccessfully(ServiceAdapter adapter) {
        adapter.cancelChooseListener();
        dialog.cancel();
        ShowToast("Thêm thành công");
        setRecyclerView();
    }


}