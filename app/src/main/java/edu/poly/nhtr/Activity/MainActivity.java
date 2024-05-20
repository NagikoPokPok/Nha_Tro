package edu.poly.nhtr.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;


import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.fragment.NotificationFragment;
import edu.poly.nhtr.fragment.SettingFragment;


public class MainActivity extends AppCompatActivity  {

    ActivityMainBinding binding;
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private String currentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cài đặt binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        } else {
            replaceFragment(new HomeFragment(), "HomeFragment");
        }

        // Load bottom menu
        setClickNavigationBottomMenu();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
    }

    public void setClickNavigationBottomMenu() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.menu_home) {
                fragment = new HomeFragment();
                currentFragmentTag = "HomeFragment";
            } else if (item.getItemId() == R.id.menu_settings) {
                fragment = new SettingFragment();
                currentFragmentTag = "SettingFragment";
            } else if (item.getItemId() == R.id.menu_notification) {
                fragment = new NotificationFragment();
                currentFragmentTag = "NotificationFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, currentFragmentTag);
            }
            return true;
        });
    }

    // Hàm thay thế Fragment
    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }



//    // Lấy ảnh đại diện và tên từ Google
//    private void getInfoFromGoogle() {
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
//        if (account != null) {
//            String userName = account.getDisplayName();
//            binding.name.setText(userName);
//
//            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
//            new MainActivity.DownloadImageTask(binding.imgProfile).execute(photoUrl);
//        }
//    }
//
//    private void openAddHomeDialog(int gravity) {
//        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.layout_dialog_add_home);
//
//        //Anh xa view cho dialog
//        TextView nameHome = dialog.findViewById(R.id.txt_name_home);
//        TextView addressHome = dialog.findViewById(R.id.txt_address_home);
//
//        // Set dấu * đỏ cho TextView
//        Typeface interLightTypeface = Typeface.createFromAsset(getAssets(), "font/inter_light.ttf");
//        Typeface interBoldTypeface = Typeface.createFromAsset(getAssets(), "font/inter_bold.ttf");
//        Spannable text1  = new SpannableString(" *");
//        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        nameHome.append(text1);
//        addressHome.append(text1);
//
//
//
//        Window window = dialog.getWindow();
//        if(window == null)
//        {
//            return;
//        }
//
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        WindowManager.LayoutParams windowAttributes = window.getAttributes();
//        windowAttributes.gravity = gravity;
//        window.setAttributes(windowAttributes);
//
//        if(Gravity.CENTER == gravity)
//        {
//            dialog.setCancelable(true); //Nếu nhấp ra bên ngoài thì cho phép đóng dialog
//        }
//        dialog.show();
//
//
//
//        EditText edtNameHome = dialog.findViewById(R.id.edt_name_home);
//        EditText edtAddress = dialog.findViewById(R.id.edt_address);
//        Button btnAddHome = dialog.findViewById(R.id.btn_add_home);
//        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
//
//        // Xử lý/ hiệu chỉnh màu nút button add home
//        TextWatcher textWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                updateButtonState(edtNameHome, edtAddress, btnAddHome);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        };
//
//        // Thêm TextWatcher cho cả hai EditText
//        edtNameHome.addTextChangedListener(textWatcher);
//        edtAddress.addTextChangedListener(textWatcher);
//
//
//        // Xử lý sự kiện cho button
//        btnAddHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (edtNameHome.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter home name", Toast.LENGTH_SHORT).show();
//
//                } else if (edtAddress.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter home address", Toast.LENGTH_SHORT).show();
//                } else {
//
//                    FirebaseFirestore database = FirebaseFirestore.getInstance();
//                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
//                    HashMap<String, Object> home = new HashMap<>();
//                    home.put(Constants.KEY_NAME_HOME, edtNameHome.getText().toString());
//                    home.put(Constants.KEY_ADDRESS, edtAddress.getText().toString());
//                    home.put(Constants.KEY_TIMESTAMP, new Date());
//                    home.put(Constants.KEY_USER_ID, currentUserId);
//                    database.collection(Constants.KEY_COLLECTION_HOMES)
//                            .add(home)
//                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                @Override
//                                public void onSuccess(DocumentReference documentReference) {
//                                    preferenceManager.putString(Constants.KEY_HOME_ID, documentReference.getId());
//                                    preferenceManager.putString(Constants.KEY_NAME_HOME, edtNameHome.getText().toString());
//                                    preferenceManager.putString(Constants.KEY_ADDRESS, edtAddress.getText().toString());
//
//                                    Toast.makeText(MainActivity.this, "Add success.",
//                                            Toast.LENGTH_SHORT).show();
//                                    getHomes();
//                                    dialog.dismiss();
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(MainActivity.this, "Add failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                    loading(false);
//
//                                }
//                            });
//                }
//            }
//        });
//
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//
//    }
//
//    // Cập nhật màu cho button
//    private void updateButtonState(EditText edtNameHome, EditText edtAddress, Button btnAddHome) {
//        String name = edtNameHome.getText().toString().trim();
//        String address = edtAddress.getText().toString().trim();
//        if (name.isEmpty() || address.isEmpty()) {
//            btnAddHome.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
//        } else {
//            btnAddHome.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
//        }
//    }
//
//
//    private void getHomes(){
//        loading(true);
//        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        database.collection(Constants.KEY_COLLECTION_HOMES)
//                .whereEqualTo(Constants.KEY_USER_ID, currentUserId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        loading(false);
//
//                        if(task.isSuccessful() && task.getResult() != null) {
//                            List<Home> homes = new ArrayList<>();
//                            for(QueryDocumentSnapshot document : task.getResult()){
//                                // Duyệt qua document và lấy danh sách các nhà trọ
//                                Home home = new Home();
//                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
//                                home.addressHome = document.getString(Constants.KEY_ADDRESS);
//                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
//                                home.idHome = document.getId();
//                                homes.add(home);
//                            }
//
//                            Log.d("MainActivity", "Number of homes: " + homes.size());
//
//                            if (!homes.isEmpty()) {
//                                HomeAdapter homesAdapter = new HomeAdapter(homes, MainActivity.this);
//                                binding.usersRecyclerView.setAdapter(homesAdapter);
//                                Collections.sort(homes,(obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
//                                homesAdapter.notifyDataSetChanged();
//                                binding.usersRecyclerView.smoothScrollToPosition(0);
//
//
//                                // Do trong activity_users.xml, usersRecycleView đang được setVisibility là Gone, nên sau
//                                // khi setAdapter mình phải set lại là VISIBLE
//
//                                binding.txtNotification.setVisibility(View.GONE);
//                                binding.imgAddHome.setVisibility(View.GONE);
//                                binding.usersRecyclerView.setVisibility(View.VISIBLE);
//
//                                Log.d("MainActivity", "Adapter set successfully");
//                            }else{
//                                binding.txtNotification.setVisibility(View.VISIBLE);
//                                binding.imgAddHome.setVisibility(View.VISIBLE);
//                            }
//
//                        }else {
//                            showErrorMessage("Error fetching users");
//                        }
//                    }
//                });
//    }
//
//    private void showErrorMessage(String message) {
//        binding.txtErrorMessage.setText(message);
//        binding.txtErrorMessage.setVisibility(View.VISIBLE);
//    }
//
//
//    private void loading(Boolean isLoading) {
//        if (isLoading) {
//            // Hiển thị thanh tiến trình nếu đang tải
//            binding.progressBar.setVisibility(View.VISIBLE);
//        } else {
//            // Ẩn thanh tiến trình nếu không có tải
//            binding.progressBar.setVisibility(View.INVISIBLE);
//        }
//    }
//    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        private ImageView imageView;
//
//        public DownloadImageTask(ImageView imageView) {
//            this.imageView = imageView;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String imageUrl = urls[0];
//            Bitmap bitmap = null;
//            try {
//                InputStream in = new java.net.URL(imageUrl).openStream();
//                bitmap = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", Objects.requireNonNull(e.getMessage()));
//            }
//            return bitmap;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            if (result != null) {
//                imageView.setImageBitmap(result);
//            }
//        }
//    }
//
//

//        //Set three fonts into one textview
//        Spannable text1  = new SpannableString("Bạn chưa có nhà trọ\n Hãy nhấn nút ");
//        Typeface interLightTypeface = Typeface.createFromAsset(getAssets(), "font/inter_light.ttf");
//        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        binding.txtNotification.setText(text1);

//        Spannable text2  = new SpannableString("+");
//        Typeface interBoldTypeface = Typeface.createFromAsset(getAssets(), "font/inter_bold.ttf");
//        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        binding.txtNotification.append(text2);
//
//        Spannable text3  = new SpannableString(" để thêm nhà trọ.");
//        Typeface interLightTypeface2 = Typeface.createFromAsset(getAssets(), "font/inter_light.ttf");
//        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        binding.txtNotification.append(text3);
//
//
//        //Set preference
//        preferenceManager = new PreferenceManager(getApplicationContext());
//
//        // Set up RecyclerView layout manager
//        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//
//        // Load user's information
//        loadUserDetails();
//
//        // Load home information
//        getHomes();
//    private void setListeners() {
//        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            List<? extends UserInfo> providerData = currentUser.getProviderData();
//            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
//            for (UserInfo userInfo : providerData) {
//                String providerId = userInfo.getProviderId();
//                if (providerId.equals("google.com")) {
//                    // TH đăng nhập bằng tài khoản Google
//                    getInfoFromGoogle();
//                    return; // Thoát khỏi vòng lặp khi thấy đúng tài khoản Google
//                }
//            }
//            // Nếu là tài khoản Email thì tải thông tin người dùng từ SharedPreferences
//            loadUserDetails();
//        } else {
//            // Không có người dùng nào đang đăng nhập, tải thông tin từ SharedPreferences
//            loadUserDetails();
//        }
//    }

//    private Bitmap getConversionImage(String encodedImage) {
//        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        int width = 150;
//        int height = 150;
//        return Bitmap.createScaledBitmap(bitmap, width, height, true);
//    }

//    private void loadUserDetails() {
//        String encodedImg = preferenceManager.getString(Constants.KEY_IMAGE);
//        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
//        if (encodedImg != null && !encodedImg.isEmpty()) {
//            try {
//                Bitmap profileImage = getConversionImage(encodedImg);
//                binding.imgProfile.setImageBitmap(profileImage);
//                binding.txtAddImage.setVisibility(View.INVISIBLE);
//            } catch (Exception e) {
//                binding.txtAddImage.setVisibility(View.VISIBLE); // Nếu không có ảnh thì để mặc định
//                Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            // Nếu không có ảnh, hiển thị ảnh mặc định và ẩn ảnh người dùng
//            binding.txtAddImage.setVisibility(View.VISIBLE);
//        }
//    }

    // setListeners();

    // Xử lý Dialog Thêm nhà trọ
    // binding.btnAddHome.setOnClickListener(view -> openAddHomeDialog(Gravity.CENTER));
}
