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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.poly.nhtr.databinding.FragmentGuestPrintContractBinding;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class GuestPrintContractFragment extends Fragment {

    private FirebaseFirestore db;
    private Button printContract;
    private ProgressBar progressBar;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private Room room;
    private Home home;
    private FragmentGuestPrintContractBinding binding;
    private PreferenceManager preferenceManager;
    private WebView webView;

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
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        printContract = binding.btnPrintContract;
        progressBar = binding.progressBar;
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = binding.webviewPdf;

        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            if (room != null && home != null) {
                printContract.setOnClickListener(v -> {
                    if (checkPermissions()) {
                        fetchDataAndDisplayPdf(room.getRoomId(), home.getIdHome());
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
                showToast("Permission denied");
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchDataAndDisplayPdf(String roomId, String homeId) {
        showProgressBar(true);

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
                                                                createPdf(path, fullName, cccdNumber, roomName, roomPrice, homeName, contractCreateDate, contractExpireDate, dateIn, gender, totalMembers, payDate);
                                                            }

                                                            if (pdfFile.exists()) {
                                                                uploadPdfToFirebaseStorage(pdfFile);
                                                            } else {
                                                                showToast("PDF file does not exist");
                                                            }

                                                            showProgressBar(false);
                                                        } else {
                                                            showToast("Failed to fetch home data");
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
                }).addOnFailureListener(e -> {
                    showToast("Lỗi khi tải dữ liệu: " + e.getMessage());
                    showProgressBar(false);
                });
    }

    private void createPdf(String path, String fullName, String idNumber, String roomName, String roomPrice, String homeName, String contractCreateDate, String contractExpireDate, String dateIn, String gender, Long totalMembers, String payDate) {
        try {
            PdfWriter writer = new PdfWriter(Files.newOutputStream(Paths.get(path)));
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add header
            PdfFont font;
            try {
                font = PdfFontFactory.createFont("assets/fonts/arial.ttf", "Identity-H", true);
            } catch (Exception e) {
                font = PdfFontFactory.createFont();
            }

            Text header = new Text("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM\nĐộc lập - Tự do - Hạnh phúc")
                    .setFont(font)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14);
            document.add(new Paragraph(header).setTextAlignment(TextAlignment.CENTER));

            // Add tenant info
            document.add(new Paragraph("\nHọ và tên người thuê: " + fullName)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("CCCD: " + idNumber)
                    .setFont(font)
                    .setFontSize(12));

            // Add room info
            document.add(new Paragraph("\nTên phòng: " + roomName)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Giá phòng: " + roomPrice)
                    .setFont(font)
                    .setFontSize(12));

            // Add home info
            document.add(new Paragraph("\nTên nhà: " + homeName)
                    .setFont(font)
                    .setFontSize(12));

            // Add contract dates
            document.add(new Paragraph("\nNgày bắt đầu hợp đồng: " + contractCreateDate)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Ngày hết hạn hợp đồng: " + contractExpireDate)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Ngày vào ở: " + dateIn)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Giới tính: " + gender)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Tổng số thành viên: " + totalMembers)
                    .setFont(font)
                    .setFontSize(12));
            document.add(new Paragraph("Ngày trả tiền: " + payDate)
                    .setFont(font)
                    .setFontSize(12));

            document.close();
        } catch (Exception e) {
            Timber.tag("PDF Creation").e(e, "Error creating PDF");
        }
    }

    private void uploadPdfToFirebaseStorage(File pdfFile) {
        Uri fileUri = Uri.fromFile(pdfFile);
        String storagePath = "contracts/" + pdfFile.getName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(storagePath);

        UploadTask uploadTask = storageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            Timber.tag("PDF Upload").d("PDF uploaded successfully. Download URL: %s", downloadUrl);
            showToast("PDF uploaded successfully");
            showPdfInWebView(downloadUrl);
            downloadPdf(downloadUrl, pdfFile.getName());
        })).addOnFailureListener(exception -> {
            Timber.tag("PDF Upload").e(exception, "Failed to upload PDF");
            showToast("Failed to upload PDF: " + exception.getMessage());
        });
    }

    private void downloadPdf(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
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
    }


    private void showPdfInWebView(String url) {
        webView.loadUrl("https://docs.google.com/viewer?url=" + url);
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
