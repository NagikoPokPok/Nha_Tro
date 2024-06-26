package edu.poly.nhtr.listeners;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.models.Service;

public interface ServiceListener {
    void onServiceClicked(Service service);
    void openPopup(View view, Service service, ItemServiceBinding binding);
    void onServiceItemCLick(Service service, RecyclerView recyclerView, int position);
    void onImageItemClick(ImageView imageView, Bitmap bitmap);
    void customPosition(RecyclerView recyclerView, int spanCount);
    void deleteService(Service service);
    void addServiceSuccess(Service service);

    void ShowToast(String message);

    void CloseDialog();

    void showResultUpdateStatusApply(Service service);

    void showResultUpdateService(Service service, RecyclerView recyclerView, int position);
}
