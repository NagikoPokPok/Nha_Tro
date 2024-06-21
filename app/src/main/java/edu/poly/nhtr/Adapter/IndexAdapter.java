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

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    Context context;
    List<Index> index_list;
    private boolean isNextClicked = false;
    private boolean isDeleteClicked = false;
    private boolean isCheckBoxClicked = false;

    private final IndexInterface indexInterface;

    public IndexAdapter(Context context, List<Index> index_list, IndexInterface indexInterface)
    {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_row_index, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IndexAdapter.ViewHolder holder, int position) {
        Index indexModel = index_list.get(position);
        holder.nameRoom.setText(indexModel.getNameRoom());

        if (!isNextClicked) {

            holder.electricityIndexOld.setVisibility(View.VISIBLE);
            holder.electricityIndexNew.setVisibility(View.VISIBLE);
            holder.waterIndexOld.setVisibility(View.GONE);
            holder.waterIndexNew.setVisibility(View.GONE);

            holder.electricityIndexOld.setText(indexModel.getElectricityIndexOld());
            holder.electricityIndexNew.setText(String.valueOf(indexModel.getElectricityIndexNew()));
        } else {
            holder.electricityIndexOld.setVisibility(View.GONE);
            holder.electricityIndexNew.setVisibility(View.GONE);
            holder.waterIndexOld.setVisibility(View.VISIBLE);
            holder.waterIndexNew.setVisibility(View.VISIBLE);

            holder.waterIndexOld.setText(String.valueOf(indexModel.getWaterIndexOld()));
            holder.waterIndexNew.setText(String.valueOf(indexModel.getWaterIndexNew()));
        }

        if(isDeleteClicked){
            holder.btn_delete.setVisibility(View.GONE);
            holder.cbx_delete.setVisibility(View.VISIBLE);
        }else{
            holder.btn_delete.setVisibility(View.VISIBLE);
            holder.cbx_delete.setVisibility(View.GONE);
        }

        if(isCheckBoxClicked){
            holder.cbx_delete.setChecked(true);
        }else{
            holder.cbx_delete.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return index_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew;
        ImageButton btn_delete;
        CheckBox cbx_delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameRoom = itemView.findViewById(R.id.txt_name_room_index);
            electricityIndexOld = itemView.findViewById(R.id.txt_electricity_index_old);
            electricityIndexNew = itemView.findViewById(R.id.txt_electricity_index_new);
            waterIndexOld = itemView.findViewById(R.id.txt_water_index_old);
            waterIndexNew = itemView.findViewById(R.id.txt_water_index_new);

            btn_delete = itemView.findViewById(R.id.btn_delete_index);
            cbx_delete = itemView.findViewById(R.id.checkBox);

        }
    }

    public void isDeleteClicked(boolean isClicked)
    {
        isDeleteClicked = isClicked;
        notifyDataSetChanged();
    }

    public void isCheckBoxClicked(boolean isClicked)
    {
        isCheckBoxClicked = isClicked;
        notifyDataSetChanged();
    }
}
