package edu.poly.nhtr.listeners;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import edu.poly.nhtr.Adapter.ServiceAdapter;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.models.Service;

public interface RoomServiceListener {
    void ShowToast(String message);
    void onServiceItemCLick(Service service, RecyclerView recyclerView, int position);
    void customPosition(RecyclerView recyclerView, int spanCount);

    void onServiceClicked(Service service);

    void openPopup(View view, Service service, ItemServiceBinding binding);

    void deleteSuccessfully(Service service);

    void updateSuccessfully(int quantity, String roomServiceId);

    void onChooseServiceClicked(Service service, int position);

    void updateDataBeforeAddSuccessfully(ServiceAdapter adapter);
    void showLoading();
    void hideLoading();
}
