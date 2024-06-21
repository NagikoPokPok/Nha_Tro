package edu.poly.nhtr.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.poly.nhtr.Adapter.CustomSpinnerAdapter;
import edu.poly.nhtr.Adapter.LibraryImageAdapter;
import edu.poly.nhtr.Adapter.ServiceAdapter;
import edu.poly.nhtr.Class.ServiceUtils;
import edu.poly.nhtr.Class.SpacesItemDecoration;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentServiceBinding;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.presenters.ServicePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;


public class ServiceFragment extends Fragment implements ServiceListener {
    private View view;
    private PreferenceManager preferenceManager;
    private ServiceAdapter.ViewHolder viewHolder;
    private FragmentServiceBinding binding;
    private ServicePresenter presenter;
    private Dialog dialog;
    private Dialog dialogLibrary;
    private List<Service> services;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Home home = (Home) getArguments().getSerializable("home");
        String homeId = home.getIdHome();
        Boolean isHomeHaveService = home.getHaveService();

        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        loadData();

        presenter = new ServicePresenter(this);
        binding = FragmentServiceBinding.inflate(getLayoutInflater());
        dialog = new Dialog(requireActivity());
        dialogLibrary = new Dialog(requireActivity());


        setListener();

        }

    private void loadData() {
        //set Preference of KEY_HOME_IS_HAVE_SERVICE
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES)
                .document(preferenceManager.getString(Constants.KEY_HOME_ID))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                if(document.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE)!=null){
                                    preferenceManager.putBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE, document.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE));
                                    Log.e("isHaveServiceDoc", document.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE).toString()+" document");
                                }
                                else preferenceManager.putBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE, false);
                                Log.e("isHaveService", "exists");
                            }else
                                preferenceManager.putBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE, false);
                            Log.e("isHaveService", "Access successfully");
                        }
                        else Log.e("isHaveService", "Don't access");


                        loadServicesData();


                    }
                });
    }

    private void loadServicesData() {
        //Load data of service
        if(!preferenceManager.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE)) {
            services = ServiceUtils.addAvailableService(preferenceManager.getString(Constants.KEY_HOME_ID), getContext());
            setRecyclerViewData();
        }
        else {
            FirebaseFirestore data = FirebaseFirestore.getInstance();
            ServiceUtils.getAvailableService(data, preferenceManager.getString(Constants.KEY_HOME_ID), new ServiceUtils.OnServicesLoadedListener() {
                @Override
                public void onServicesLoaded(List<Service> listServices) {
                    services = listServices;
                    setRecyclerViewData();
                }
            });
        }
    }


    private void setRecyclerViewData() {
        // Ensure services are not null before setting adapters
        if (services == null) {
            Log.e("RecyclerView", "Services data is null, skipping setting adapters.");
            return;
        }
        Log.e("ServicesCount", String.valueOf(services.stream().count()));

        // Dịch vụ đang sử dụng
        ServiceAdapter serviceUsedAdapter = new ServiceAdapter(this.requireActivity(), ServiceUtils.usedService(services), this);
        binding.recyclerServiceUsed.setAdapter(serviceUsedAdapter);
        binding.recyclerServiceUsed.setVisibility(View.VISIBLE);
        Log.e("ServicesCountUsed", String.valueOf(ServiceUtils.usedService(services).stream().count()));

        //Dịch vụ chưa sử dụng
        ServiceAdapter serviceUnusedAdapter = new ServiceAdapter(this.requireActivity(), ServiceUtils.unusedService(services), this);
        binding.recyclerServiceUnused.setAdapter(serviceUnusedAdapter);
        binding.recyclerServiceUnused.setVisibility(View.VISIBLE);
        Log.e("ServicesCountUnused", String.valueOf(ServiceUtils.unusedService(services).stream().count()));

        customPosition(binding.recyclerServiceUsed,3);
        customPosition(binding.recyclerServiceUnused,3);
    }

    private void setListener() {
        binding.btnAddNewService.setOnClickListener(v -> openAddNewServiceDialog());
    }

    private void openAddNewServiceDialog() {
        setupDialog(dialog, R.layout.service_dialog_add_new_service, Gravity.CENTER);

        //Ánh xạ view cho dialog
        EditText edt_name = dialog.findViewById(R.id.edt_newServiceName);
        ImageView image = dialog.findViewById(R.id.img_newService);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel_addNewService);
        Button btn_continue = dialog.findViewById(R.id.btn_continue_addNewService);

        //Xử lí ảnh
        String encodeImage = ServiceUtils.encodedImage(((BitmapDrawable) image.getDrawable()).getBitmap());

        //Xử xí button cho dialog
        btn_cancel.setOnClickListener(v -> dialog.cancel());

        btn_continue.setOnClickListener(v -> {
            dialog.cancel();
            openApplyServiceDialog(edt_name.getText().toString(), encodeImage);
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLibraryOfServiceImage();
            }
        });
    }

    private void openLibraryOfServiceImage() {
        setupDialog(dialogLibrary, R.layout.service_dialog_image_library, Gravity.CENTER);

        //Ánh xạ view
        RecyclerView recyclerView = dialogLibrary.findViewById(R.id.recycler_image_library);

        //setup Adapter for library
        LibraryImageAdapter adapter = new LibraryImageAdapter(ServiceUtils.getImageLibraryData(this.requireActivity()), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);

        customPosition(recyclerView, 4);
    }

    private void openApplyServiceDialog(String name, String encodeImage) {
        setupDialog(dialog, R.layout.service_dialog_apply_service, Gravity.CENTER);

        //Ánh xạ view
        ImageView image = dialog.findViewById(R.id.img_service);
        TextView txt_name = dialog.findViewById(R.id.txt_serviceName);
        Spinner spinner = dialog.findViewById(R.id.spinner_feeBased);
        EditText edt_unit = dialog.findViewById(R.id.edt_unit);
        EditText edt_fee = dialog.findViewById(R.id.edt_fee);
        EditText edt_note = dialog.findViewById(R.id.edt_note);
        RecyclerView recycler_applyFor = dialog.findViewById(R.id.recycler_apllyFor);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_apply = dialog.findViewById(R.id.btn_apply_service);
        TextInputLayout layout_unit = dialog.findViewById(R.id.layout_unit);
        TextInputLayout layout_fee = dialog.findViewById(R.id.layout_fee);

        //Đổ dữ liệu cho dialog
        image.setImageBitmap(ServiceUtils.getConversionImage(encodeImage));
        txt_name.setText(name);
          //Spinner
        String[] items = {"Dựa trên lũy tiến theo chỉ số", "Dựa trên từng phòng", "Dựa trên số người", "Dựa trên số lượng khác"};
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(requireActivity().getApplicationContext(), items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setDropDownVerticalOffset(Gravity.BOTTOM);
        spinner.setAdapter(adapter);

        //Xử lí button cho dialog
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceDetail(layout_fee, layout_unit)){
                    int price = Integer.parseInt(edt_fee.getText().toString());
                    String idHomeParent = preferenceManager.getString(Constants.KEY_HOME_ID);
                    Service service = new Service(idHomeParent, name.toString(), encodeImage.toString(), price, edt_unit.getText().toString(), spinner.getSelectedItemPosition(), edt_note.getText().toString(), false, false);
                    presenter.saveToFirebase(service);
                }
            }

        });

        //Xử lí hành động cho spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
                setFeedbackForUnit(position);
            }

            private void setFeedbackForUnit(int position) {
                if(position==0){
                    edt_unit.setText("Chỉ số");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                } else if (position == 1) {
                    edt_unit.setText("Phòng");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                } else if (position == 2) {
                    edt_unit.setText("Người");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                }else {
                    edt_unit.setText("");
                    edt_unit.setHint("Ví dụ: Xe, cái,...");
                    edt_unit.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        //Xử lí các hành động cho editText khi được truy cập
        edt_fee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && edt_fee.getText().toString().equals("0")) edt_fee.setText("");
                if(!hasFocus && edt_fee.getText().toString().isEmpty()) edt_fee.setText("0");
            }
        });

        edt_note.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                edt_note.setHint("");
                if(!hasFocus && edt_note.getText().toString().isEmpty()) edt_note.setHint("Viết những lưu ý của bạn ta đây");
            }
        });



    }

    private boolean isServiceDetail(TextInputLayout layoutFee, TextInputLayout layoutUnit) {
        layoutUnit.setErrorEnabled(false);
        layoutFee.setErrorEnabled(false);
        boolean isFeeNunber = false;
        boolean isTrue = true;
        int fee = 0;
        if(!layoutFee.getEditText().getText().toString().isEmpty()){
            try {
                fee = Integer.parseInt(layoutFee.getEditText().getText().toString());
                isFeeNunber = true;
            }catch (Exception e){
                layoutFee.setError("Chỉ có thể nhập số");
                isTrue = false;
            }
        }
        if(layoutUnit.getEditText().getText().toString().isEmpty()){
            layoutUnit.setError("Không được để trống đơn vị");
            isTrue = false;
        } else if (isFeeNunber && fee < 0) {
            layoutFee.setError("Phí dịch vụ không thể là số âm");
            isTrue = false;
        }

        return isTrue;
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
        view = inflater.inflate(R.layout.fragment_service, container, false);
        // Kiểm tra xem Bundle có tồn tại hay không
        if (getArguments() != null) {
            // Nhận dữ liệu từ Bundle
            Home home = (Home) getArguments().getSerializable("home");

            // Sử dụng dữ liệu 'home' như mong muốn
            // ...
        }

        return binding.getRoot();
    }

    @Override
    public void onServiceClicked(Service service) {

    }

    @Override
    public void openPopup(View view, Service service, ItemServiceBinding binding) {

    }

    @Override
    public void onServiceItemCLick(Service service) {
        //button apply service
        Button use_service;

        if(service.getApply()) setupDialog(dialog, R.layout.service_dialog_detail_service_used, Gravity.CENTER);
        else setupDialog(dialog, R.layout.service_dialog_detail_service_unused, Gravity.CENTER);


        //Ánh xạ id
        ImageView exit = dialog.findViewById(R.id.img_exit);
        ImageView image = dialog.findViewById(R.id.img_service);
        TextView txt_name = dialog.findViewById(R.id.txt_serviceName);
        Spinner spinner = dialog.findViewById(R.id.spinner_feeBased);
        EditText edt_unit = dialog.findViewById(R.id.edt_unit);
        EditText edt_fee = dialog.findViewById(R.id.edt_fee);
        EditText edt_note = dialog.findViewById(R.id.edt_note);
        RecyclerView recycler_applyFor = dialog.findViewById(R.id.recycler_apllyFor);
        Button btn_delete = dialog.findViewById(R.id.btn_delete_service);
        Button btn_apply = dialog.findViewById(R.id.btn_update_service);
        TextInputLayout layout_unit = dialog.findViewById(R.id.layout_unit);
        TextInputLayout layout_fee = dialog.findViewById(R.id.layout_fee);

        // Set data for dialog
        //Đổ dữ liệu cho dialog
        txt_name.setText(service.getName());
        image.setImageBitmap(ServiceUtils.getConversionImage(service.getCodeImage()));

        //Spinner
        String[] items = {"Dựa trên lũy tiến theo chỉ số", "Dựa trên từng phòng", "Dựa trên số người", "Dựa trên số lượng khác"};
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(requireActivity().getApplicationContext(), items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setDropDownVerticalOffset(Gravity.BOTTOM);
        spinner.setAdapter(adapter);
        spinner.setSelection(service.getFee_base());

        if(service.getFee_base()==3) edt_unit.setText(service.getUnit());
        edt_fee.setText(""+service.getPrice());
        edt_note.setText(service.getNote());

        //Xử lí button cho dialog
        exit.setVisibility(View.VISIBLE);
        if(service.getApply()){
            //Nút bỏ sử dụng
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.setApply(false);
                    dialog.cancel();
                    setRecyclerViewData();
                }
            });
        }else{
            //Ánh xạ id cho nút sử dụng và hiện nó lên
            use_service = dialog.findViewById(R.id.btn_use_service);
            use_service.setVisibility(View.VISIBLE);
            // Set listener
            use_service.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.setApply(true);
                    dialog.cancel();
                    setRecyclerViewData();
                }
            });
        }
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceDetail(layout_fee, layout_unit)){

                }
            }

        });

        //Xử lí hành động cho spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
                setFeedbackForUnit(position);
            }

            private void setFeedbackForUnit(int position) {
                if(position==0){
                    edt_unit.setText("Chỉ số");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                } else if (position == 1) {
                    edt_unit.setText("Phòng");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                } else if (position == 2) {
                    edt_unit.setText("Người");
                    edt_unit.setInputType(View.NOT_FOCUSABLE);
                }else {
                    edt_unit.setText(service.getUnit());
                    edt_unit.setHint("Ví dụ: Xe, cái,...");
                    edt_unit.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        //Xử lí các hành động cho editText khi được truy cập
        edt_fee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && edt_fee.getText().toString().equals("0")) edt_fee.setText("");
                if(!hasFocus && edt_fee.getText().toString().isEmpty()) edt_fee.setText("0");
            }
        });

        edt_note.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                edt_note.setHint("");
                if(!hasFocus && edt_note.getText().toString().isEmpty()) edt_note.setHint("Viết những lưu ý của bạn ta đây");
            }
        });

    }

    @Override
    public void customPosition(RecyclerView recyclerView, int spanCount) {
        // Clear all item decorations first
        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }
        // Set padding between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spanCount, spacingInPixels, false));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spanCount, spacingInPixels, false));
    }

    @Override
    public void ShowToast(String message) {
        Toast.makeText(this.requireActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void CloseDialog() {
        dialog.dismiss();
    }

}