package com.example.tallycat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Binds a list of Transaction objects to the RecyclerView in DashboardActivity.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    // Convert timestamps to string.
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

        // Populate the views with data from the transaction object
        holder.tvItemName.setText(transaction.getName());

        String details = transaction.getTransactionType() + " by " + transaction.getEmail();
        holder.tvDetails.setText(details);

        // Safely format the timestamp
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

    /**
     * ViewHolder that holds the UI components for a single row.
     */
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
