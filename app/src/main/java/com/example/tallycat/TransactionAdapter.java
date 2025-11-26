package com.example.tallycat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<Transaction> transactionList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Populate the views with data
        holder.tvItemName.setText(transaction.getName());

        // Build details string
        String details = transaction.getTransactionType().toUpperCase() + " by " + transaction.getEmail();
        if (transaction.getHolder() != null && !transaction.getHolder().isEmpty()) {
            details += "\nHolder: " + transaction.getHolder();
        }
        if (transaction.getDueDate() != null && !transaction.getDueDate().isEmpty()) {
            details += "\nDue: " + transaction.getDueDate();
        }
        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isEmpty()) {
            details += "\nTxn ID: " + transaction.getTransactionId().substring(0, 8) + "...";
        }

        holder.tvDetails.setText(details);

        // Format timestamp
        Timestamp timestamp = transaction.getTimestamp();
        if (timestamp != null) {
            holder.tvTimestamp.setText(dateFormat.format(timestamp.toDate()));
        } else {
            holder.tvTimestamp.setText("No date");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvDetails, tvTimestamp;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvTransactionItemName);
            tvDetails = itemView.findViewById(R.id.tvTransactionDetails);
            tvTimestamp = itemView.findViewById(R.id.tvTransactionTimestamp);
        }
    }
}