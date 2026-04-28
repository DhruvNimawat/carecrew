package com.example.carecrew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.UpdateViewHolder> {

    private List<Complaint> updates;
    private OnUpdateClickListener listener;

    public interface OnUpdateClickListener {
        void onUpdateClick(Complaint complaint);
    }

    public UpdateAdapter(List<Complaint> updates, OnUpdateClickListener listener) {
        this.updates = updates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_update, parent, false);
        return new UpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        Complaint complaint = updates.get(position);
        holder.tvUpdateCategory.setText(complaint.category);
        holder.tvUpdateDescription.setText(complaint.description);
        holder.tvUpdateStatus.setText(complaint.status);
        holder.tvUpdateTimestamp.setText(complaint.getTimestampString());

        // Color status tag safely
        int color = 0xFF888888; // Default Grey
        if ("Pending".equalsIgnoreCase(complaint.status)) color = 0xFFF44336; // Red
        else if ("In Progress".equalsIgnoreCase(complaint.status)) color = 0xFF2196F3; // Blue
        else if ("Resolved".equalsIgnoreCase(complaint.status)) color = 0xFF4CAF50; // Green
        
        if (holder.tvUpdateStatus.getBackground() != null) {
            holder.tvUpdateStatus.getBackground().mutate().setTint(color);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateClick(complaint);
            }
        });
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    static class UpdateViewHolder extends RecyclerView.ViewHolder {
        TextView tvUpdateCategory, tvUpdateDescription, tvUpdateStatus, tvUpdateTimestamp;

        public UpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUpdateCategory = itemView.findViewById(R.id.tvUpdateCategory);
            tvUpdateDescription = itemView.findViewById(R.id.tvUpdateDescription);
            tvUpdateStatus = itemView.findViewById(R.id.tvUpdateStatus);
            tvUpdateTimestamp = itemView.findViewById(R.id.tvUpdateTimestamp);
        }
    }
}