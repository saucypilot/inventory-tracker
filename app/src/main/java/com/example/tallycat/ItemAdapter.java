package com.example.tallycat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<Item> itemList;
    private final Context context;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClicked(Item item);
    }

    // Default constructor used by SearchActivity, InventoryActivity, etc.
    public ItemAdapter(List<Item> itemList, Context context) {
        this(itemList, context, null);
    }

    // Constructor with custom click listener (used by ManualCheckoutReturnActivity).
    public ItemAdapter(List<Item> itemList, Context context, OnItemClickListener listener) {
        this.itemList = itemList;
        this.context = context;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvItemCategory.setText("Category: " + safe(item.getCategory()));
        holder.tvItemDescription.setText(safe(item.getDescription()));
        holder.tvItemStatus.setText("Status: " + safe(item.getStatus()));

        String holderText = item.getHolder();
        if (holderText == null || holderText.isEmpty()) {
            holderText = "\u2014";
        }
        holder.tvItemHolder.setText("Holder: " + holderText);

        String dueDateText = item.getDueDate();
        if (dueDateText == null || dueDateText.isEmpty()) {
            dueDateText = "\u2014";
        }
        holder.tvItemDueDate.setText("Due: " + dueDateText);

        // Default click behavior: open the item profile screen,
        // or delegate to a custom listener if one was provided.
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClicked(item);
            } else {
                Intent intent = new Intent(context, ItemProfileActivity.class);
                intent.putExtra("ITEM_EXTRA", item);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    // Helpers to manage list contents
    public void updateData(List<Item> newItems) {
        this.itemList.clear();
        this.itemList.addAll(newItems);
        notifyDataSetChanged();
    }

    public void clearData() {
        this.itemList.clear();
        notifyDataSetChanged();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        TextView tvItemCategory;
        TextView tvItemDescription;
        TextView tvItemStatus;
        TextView tvItemHolder;
        TextView tvItemDueDate;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
            tvItemHolder = itemView.findViewById(R.id.tvItemHolder);
            tvItemDueDate = itemView.findViewById(R.id.tvItemDueDate);
        }
    }
}
