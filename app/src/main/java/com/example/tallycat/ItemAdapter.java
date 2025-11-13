package com.example.tallycat;

import android.content.Context;
import android.content.Intent;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<Item> itemList;
    private final Context context;

    public ItemAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvItemCategory.setText("Category: " + item.getCategory());
        holder.tvItemDescription.setText(item.getDescription());
        holder.tvItemStatus.setText("Status: " + item.getStatus());

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemProfileActivity.class);
            // Put the entire Item object into the intent extras
            intent.putExtra("ITEM_EXTRA", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    // Helper methods
    public void updateData(List<Item> newItems) {
        this.itemList.clear();
        this.itemList.addAll(newItems);
        notifyDataSetChanged();
    }

    public void clearData() {
        this.itemList.clear();
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemCategory, tvItemDescription, tvItemStatus;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
        }
    }
}
