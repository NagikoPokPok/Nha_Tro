package edu.poly.nhtr.listeners;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.models.Service;

public interface ServiceListener {
    void onServiceClicked(Service service);
    void openPopup(View view, Service service, ItemServiceBinding binding);
    void onServiceItemCLick(Service service);
    void customPosition(RecyclerView recyclerView, int spanCount);

    void ShowToast(String message);

    void CloseDialog();
}
