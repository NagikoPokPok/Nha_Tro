package edu.poly.nhtr.listeners;

import android.view.View;

import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemServiceBinding;
import edu.poly.nhtr.models.Service;

public interface ServiceListener {
    void onServiceClicked(Service service);
    void openPopup(View view, Service service, ItemServiceBinding binding);
    void onItemCLick(Service service);


}
