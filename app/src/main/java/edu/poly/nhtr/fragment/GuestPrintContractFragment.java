package edu.poly.nhtr.fragment;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestPrintContractBinding;
import edu.poly.nhtr.interfaces.GuestPrintContractInterface;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class GuestPrintContractFragment extends Fragment implements GuestPrintContractInterface {

    private FirebaseFirestore db;
    private Button printContract;
    private ProgressBar progressBar;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private Room room;
    private Home home;
    // private String userID;
    private String pdfUrl;
    private FragmentGuestPrintContractBinding binding;
    private WebView webView;
    private PreferenceManager preferenceManager;

    private RoomContractFragment.OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RoomContractFragment.OnFragmentInteractionListener) {
            mListener = (RoomContractFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGuestPrintContractBinding.inflate(inflater, container, false);
        printContract = binding.btnPrintContract;
        progressBar = binding.progressBar;
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = binding.webviewPdf;
        preferenceManager = new PreferenceManager(requireContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            // userID = arguments.getString("userID");

            if (room != null && home != null) {
                printContract.setOnClickListener(v -> {
                    if (checkPermissions()) {
                        downloadPdf();
                    } else {
                        requestPermissions();
                    }
                });

                // Load the contract automatically when the fragment is opened
                fetchDataAndDisplayPdf(room.getRoomId(), home.getIdHome());
            } else {
                showToast("Invalid room or home data");
            }
        } else {
            showToast("Invalid arguments");
        }
    }

    private void showProgressBar(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception ex) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse(String.format("package:%s", requireActivity().getPackageName())));
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (room != null && home != null) {
                    fetchDataAndDisplayPdf(room.getRoomId(), home.getIdHome());
                }
            } else {
                showToast("Quyền truy cập bị từ chối");
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getInfoUserFromGoogleAccount() {
        // Lấy thông tin người dùng từ tài khoản Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        String currentUserId = "";

        if (account != null) {
            currentUserId = account.getId();
        } else {
            // Fallback to preference manager
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }

        return currentUserId;
    }



    private void fetchDataAndDisplayPdf(String roomId, String homeId) {
        showProgressBar(true);

        // Lấy User ID từ Google Sign-In
        String userID = getInfoUserFromGoogleAccount();
        if (userID.isEmpty()) {
            showToast("User ID is not available");
            showProgressBar(false);
            return;
        }

        // Truy vấn dữ liệu từ Firestore
        db.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> contractSnapshots = task.getResult().getDocuments();

                        if (!contractSnapshots.isEmpty()) {
                            DocumentSnapshot contractDoc = contractSnapshots.get(0);
                            String fullName = contractDoc.getString(Constants.KEY_GUEST_NAME);
                            String cccdNumber = contractDoc.getString(Constants.KEY_GUEST_CCCD);
                            String contractCreateDate = contractDoc.getString(Constants.KEY_CONTRACT_CREATED_DATE);
                            String contractExpireDate = contractDoc.getString(Constants.KEY_CONTRACT_EXPIRATION_DATE);
                            String dateIn = contractDoc.getString(Constants.KEY_GUEST_DATE_IN);
                            String gender = contractDoc.getString(Constants.KEY_GUEST_GENDER);
                            Long totalMembers = contractDoc.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS);
                            String payDate = contractDoc.getString(Constants.KEY_CONTRACT_PAY_DATE);

                            db.collection(Constants.KEY_COLLECTION_ROOMS)
                                    .document(roomId)
                                    .get()
                                    .addOnCompleteListener(roomTask -> {
                                        if (roomTask.isSuccessful() && roomTask.getResult() != null) {
                                            DocumentSnapshot roomDoc = roomTask.getResult();
                                            String roomName = roomDoc.getString(Constants.KEY_NAME_ROOM);
                                            String roomPrice = roomDoc.getString(Constants.KEY_PRICE);

                                            db.collection(Constants.KEY_COLLECTION_USERS)
                                                    .document(userID)
                                                    .get()
                                                    .addOnCompleteListener(userTask -> {
                                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                                            DocumentSnapshot userDoc = userTask.getResult();
                                                            String userName = userDoc.getString(Constants.KEY_NAME);
                                                            String userPhoneNumber = userDoc.getString(Constants.KEY_PHONE_NUMBER);
                                                            String userAddress = userDoc.getString(Constants.KEY_ADDRESS);

                                                            db.collection(Constants.KEY_COLLECTION_HOMES)
                                                                    .document(homeId)
                                                                    .get()
                                                                    .addOnCompleteListener(homeTask -> {
                                                                        if (homeTask.isSuccessful() && homeTask.getResult() != null) {
                                                                            DocumentSnapshot homeDoc = homeTask.getResult();
                                                                            String homeName = homeDoc.getString(Constants.KEY_NAME_HOME);

                                                                            String fileName = "contract_" + roomId + ".pdf";
                                                                            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
                                                                            File pdfFile = new File(path);

                                                                            if (!pdfFile.exists()) {
                                                                                createPdf(path, fullName, cccdNumber, roomName, roomPrice, homeName, contractCreateDate, contractExpireDate, totalMembers, payDate, userName, userPhoneNumber, userAddress);
                                                                            }

                                                                            if (pdfFile.exists()) {
                                                                                uploadPdfToFirebaseStorage(pdfFile);
                                                                            } else {
                                                                                showToast("File PDF không tồn tại");
                                                                            }

                                                                            showProgressBar(false);
                                                                        } else {
                                                                            showToast("Failed to fetch home data");
                                                                            showProgressBar(false);
                                                                        }
                                                                    });
                                                        } else {
                                                            showToast("No user data found");
                                                            showProgressBar(false);
                                                        }
                                                    });
                                        } else {
                                            showToast("Failed to fetch room data");
                                            showProgressBar(false);
                                        }
                                    });
                        } else {
                            showToast("No contracts found for the room");
                            showProgressBar(false);
                        }
                    } else {
                        showToast("Failed to fetch contract data");
                        showProgressBar(false);
                    }
                });
    }



    private static void createPdf(String path, String guestName, String guestCccd, String roomName, String roomPrice,
                            String homeName, String contractCreateDate, String contractExpireDate, Long totalMembers, String payDate,
                            String lessorName, String lessorPhone, String lessorAddress) {
                        try {
                            PdfWriter writer = new PdfWriter(Files.newOutputStream(Paths.get(path)));
                            PdfDocument pdfDoc = new PdfDocument(writer);
                            Document document = new Document(pdfDoc);

                            // Add header
                            PdfFont font;
                            font = PdfFontFactory.createFont("assets/fonts/arial_unicode_bold.ttf", "Identity-H", true);


                            // Nếu số điện thoại hoặc địa chỉ là null, thay thế bằng "...."
                            lessorPhone = (lessorPhone == null) ? "...." : lessorPhone;
                            lessorAddress = (lessorAddress == null) ? "...." : lessorAddress;

                            // Add title
                            Text header = new Text(R.string.Header_hop_dong + "\n")
                                    .setFont(font)
                                    .setBold()
                                    .setFontSize(14);
                            document.add(new Paragraph(header).setTextAlignment(TextAlignment.CENTER));

                            Text header2 = new Text(R.string.Header_hop_dong_2 + "\n")
                                    .setFont(font)
                                    .setBold()
                                    .setFontSize(14);
                            document.add(new Paragraph(header2).setTextAlignment(TextAlignment.CENTER));

                            // Add contract title
                            Text contractTitle = new Text("\nHỢP ĐỒNG THUÊ PHÒNG TRỌ")
                                    .setFont(font)
                                    .setBold()
                                    .setFontSize(14);
                            document.add(new Paragraph(contractTitle).setTextAlignment(TextAlignment.CENTER));

                            // Add contract details
                            document.add(new Paragraph("\nHôm nay ngày " + contractCreateDate.substring(0, 2) +
                                    " tháng " + contractCreateDate.substring(3, 5) +
                                    " năm " + contractCreateDate.substring(6) +
                                    " tại căn nhà " + homeName +
                                    ". Chúng tôi ký tên dưới đây gồm có:")
                                    .setFont(font)
                                    .setFontSize(12));

                            // Add lessor info (Thông tin bên cho thuê)
                            document.add(new Paragraph("\nBÊN CHO THUÊ PHÒNG TRỌ (gọi tắt là Bên A):")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Ông/bà " + lessorName)
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Số điện thoại: " + lessorPhone)
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("CMND/CCCD số " + lessorPhone + " cấp ngày .......... nơi cấp ................................")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Thường trú tại: " + lessorAddress)
                                    .setFont(font)
                                    .setFontSize(12));

                            // Add tenant info (Thông tin bên thuê)
                            document.add(new Paragraph("\nBÊN THUÊ PHÒNG TRỌ (gọi tắt là Bên B):")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Ông/bà " + guestName)
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("CMND/CCCD số " + guestCccd + " cấp ngày .......... nơi cấp ................................")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Thường trú tại: ...............................................................................................")
                                    .setFont(font)
                                    .setFontSize(12));

                            // Add contract terms (Điều khoản hợp đồng)
                            document.add(new Paragraph("\n1. Nội dung thuê phòng trọ")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Bên A cho Bên B thuê 01 phòng trọ " + roomName +
                                    " tại căn nhà " + homeName + ". Với thời hạn là: " + contractExpireDate +
                                    " tháng giá thuê: " + roomPrice + " đồng. Chưa bao gồm chi phí: điện sinh hoạt nước.")
                                    .setFont(font)
                                    .setFontSize(12));

                            document.add(new Paragraph("\n2. Trách nhiệm Bên A")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Đảm bảo căn nhà cho thuê không có tranh chấp khiếu kiện.\nĐăng ký với chính quyền địa phương về thủ tục cho thuê phòng trọ.")
                                    .setFont(font)
                                    .setFontSize(12));

                            document.add(new Paragraph("\n3. Trách nhiệm Bên B")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Đặt cọc với số tiền là............................đồng (Bằng chữ ......................................) thanh toán tiền thuê phòng hàng tháng vào ngày " + payDate + " + tiền điện + nước.")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Đảm bảo các thiết bị và sửa chữa các hư hỏng trong phòng trong khi sử dụng. Nếu không sửa chữa thì khi trả phòng bên A sẽ trừ vào tiền đặt cọc giá trị cụ thể được tính theo giá thị trường.\nChỉ sử dụng phòng trọ vào mục đích ở với số lượng tối đa không quá " + totalMembers + " người (kể cả trẻ em); không chứa các thiết bị gây cháy nổ hàng cấm... cung cấp giấy tờ tùy thân để đăng ký tạm trú theo quy định giữ gìn an ninh trật tự nếp sống văn hóa đô thị; không tụ tập nhậu nhẹt cờ bạc và các hành vi vi phạm pháp luật khác.\nKhông được tự ý cải tạo kiếm trúc phòng hoặc trang trí ảnh hưởng tới tường cột nền... Nếu có nhu cầu trên phải trao đổi với bên A để được thống nhất.")
                                    .setFont(font)
                                    .setFontSize(12));

                            document.add(new Paragraph("\n4. Điều khoản thực hiện")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("Hai bên nghiêm túc thực hiện những quy định trên trong thời hạn cho thuê nếu bên A lấy phòng phải báo cho bên B ít nhất 01 tháng hoặc ngược lại.\nSau thời hạn cho thuê ..... tháng nếu bên B có nhu cầu hai bên tiếp tục thương lượng giá thuê để gia hạn hợp đồng bằng miệng hoặc thực hiện như sau.")
                                    .setFont(font)
                                    .setFontSize(12));

                            // Add signature fields (Phần chữ ký)
                            document.add(new Paragraph("\nSố lần gia hạn\tThời gian gia hạn (tháng)\tTừ ngày\tĐến ngày\tGiá thuê/ tháng (triệu đồng)")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("\n\t1\t\t\t\t\t\n\t2\t\t\t\t\t")
                                    .setFont(font)
                                    .setFontSize(12));

                            document.add(new Paragraph("\nBên B\t\t\t\t\t\t\t\t\t\t\t\tBên A")
                                    .setFont(font)
                                    .setFontSize(12));
                            document.add(new Paragraph("(Ký ghi rõ họ tên)\t\t\t\t\t\t\t\t\t\t(Ký ghi rõ họ tên)")
                                    .setFont(font)
                                    .setFontSize(12));

                            document.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


    private void uploadPdfToFirebaseStorage(File pdfFile) {
        Uri fileUri = Uri.fromFile(pdfFile);
        String storagePath = "contracts/" + pdfFile.getName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(storagePath);

        // Disable the "Print" button while the upload is in progress
        printContract.setEnabled(false);

        UploadTask uploadTask = storageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            pdfUrl = uri.toString(); // Save the URL
            showToast("PDF uploaded successfully");
            showPdfInWebView(pdfUrl);

            // Enable the "Print" button after the upload is complete
            printContract.setEnabled(true);
        })).addOnFailureListener(exception -> {
            showToast("Failed to upload PDF: " + exception.getMessage());

            // Enable the "Print" button even if the upload fails
            printContract.setEnabled(true);
        });
    }


    private void downloadPdf() {
        if (pdfUrl != null) {
            String fileName = Uri.parse(pdfUrl).getLastPathSegment();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle(fileName);
            request.setDescription("Downloading PDF contract");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
            } else {
                showToast("Failed to download PDF");
            }
        } else {
            showToast("No PDF to download");
        }
    }


    private void showPdfInWebView(String url) {
        try {
            String encodedUrl = URLEncoder.encode(url, "UTF-8");
            webView.loadUrl("https://docs.google.com/viewer?url=" + encodedUrl);
        } catch (UnsupportedEncodingException e) {
            showToast("Failed to load PDF in WebView: " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
