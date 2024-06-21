package edu.poly.nhtr.Adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.Class.ServiceUtils;
import edu.poly.nhtr.databinding.ItemServiceImageLibraryBinding;
import edu.poly.nhtr.listeners.ServiceListener;

public class LibraryImageAdapter extends RecyclerView.Adapter<LibraryImageAdapter.ViewHolder>{
    private final List<Bitmap> images;
    private final ServiceListener listener;

    public LibraryImageAdapter(List<Bitmap> images, ServiceListener listener) {
        this.images = images;
        this.listener = listener;
    }


    @NonNull
    @Override
    public LibraryImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceImageLibraryBinding binding = ItemServiceImageLibraryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryImageAdapter.ViewHolder holder, int position) {
        // thiết lập dữ liệu
        holder.setLibraryData(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public ItemServiceImageLibraryBinding binding;
        public ViewHolder(@NonNull ItemServiceImageLibraryBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }

        public void setLibraryData(Bitmap image){
            binding.imageService.setImageBitmap(image);

            //Thiết lập hành động nhấn vào

        }
    }
}
