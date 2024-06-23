package edu.poly.nhtr.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.poly.nhtr.databinding.LayoutItemRowIndexBinding;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    Context context;
    List<Index> index_list;
    private boolean isNextClicked = false;
    private boolean isDeleteClicked = false;
    private boolean isCheckBoxClicked = false;

    private final IndexInterface indexInterface;

    public IndexAdapter(Context context, List<Index> index_list, IndexInterface indexInterface) {
        this.context = context;
        this.index_list = index_list;
        this.indexInterface = indexInterface;
    }

    public void setNextClicked(boolean nextClicked) {
        isNextClicked = nextClicked;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IndexAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutItemRowIndexBinding binding = LayoutItemRowIndexBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IndexAdapter.ViewHolder holder, int position) {
        Index indexModel = index_list.get(position);
        holder.binding.txtNameRoomIndex.setText(indexModel.getNameRoom());

        if (!isNextClicked) {
            holder.binding.txtElectricityIndexOld.setVisibility(View.VISIBLE);
            holder.binding.txtElectricityIndexNew.setVisibility(View.VISIBLE);
            holder.binding.txtWaterIndexOld.setVisibility(View.GONE);
            holder.binding.txtWaterIndexNew.setVisibility(View.GONE);

            holder.binding.txtElectricityIndexOld.setText(indexModel.getElectricityIndexOld());
            holder.binding.txtElectricityIndexNew.setText(String.valueOf(indexModel.getElectricityIndexNew()));
        } else {
            holder.binding.txtElectricityIndexOld.setVisibility(View.GONE);
            holder.binding.txtElectricityIndexNew.setVisibility(View.GONE);
            holder.binding.txtWaterIndexOld.setVisibility(View.VISIBLE);
            holder.binding.txtWaterIndexNew.setVisibility(View.VISIBLE);

            holder.binding.txtWaterIndexOld.setText(String.valueOf(indexModel.getWaterIndexOld()));
            holder.binding.txtWaterIndexNew.setText(String.valueOf(indexModel.getWaterIndexNew()));
        }

        if (isDeleteClicked) {
            holder.binding.btnDeleteIndex.setVisibility(View.GONE);
            holder.binding.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnDeleteIndex.setVisibility(View.VISIBLE);
            holder.binding.checkBox.setVisibility(View.GONE);
        }

        if (isCheckBoxClicked) {
            holder.binding.checkBox.setChecked(true);
        } else {
            holder.binding.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return index_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayoutItemRowIndexBinding binding;

        public ViewHolder(@NonNull LayoutItemRowIndexBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnEditIndex.setOnClickListener(v -> indexInterface.showDialogDetailedIndex());
        }
    }

    public void isDeleteClicked(boolean isClicked) {
        isDeleteClicked = isClicked;
        notifyDataSetChanged();
    }

    public void isCheckBoxClicked(boolean isClicked) {
        isCheckBoxClicked = isClicked;
        notifyDataSetChanged();
    }
}
