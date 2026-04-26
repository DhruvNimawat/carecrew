package com.example.carecrew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentUpdatesAdapter extends RecyclerView.Adapter<RecentUpdatesAdapter.UpdateViewHolder> {

    private List<UpdateItem> updates;

    public RecentUpdatesAdapter(List<UpdateItem> updates) {
        this.updates = updates;
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_update, parent, false);
        return new UpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        UpdateItem item = updates.get(position);
        holder.tvMessage.setText(item.getMessage());
        holder.tvTime.setText(item.getTimestamp());

        switch (item.getType()) {
            case RESOLVED:
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_save);
                holder.ivIcon.getBackground().setTint(0xFFE8F5E9); // Soft Green
                break;
            case IN_PROGRESS:
                holder.ivIcon.setImageResource(android.R.drawable.ic_popup_sync);
                holder.ivIcon.getBackground().setTint(0xFFE3F2FD); // Soft Blue
                break;
            case URGENT:
                holder.ivIcon.setImageResource(android.R.drawable.stat_notify_error);
                holder.ivIcon.getBackground().setTint(0xFFFFEBEE); // Soft Red
                break;
            case ANNOUNCEMENT:
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivIcon.getBackground().setTint(0xFFFFF3E0); // Soft Orange
                break;
        }
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    public static class UpdateViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvMessage, tvTime;

        public UpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivUpdateIcon);
            tvMessage = itemView.findViewById(R.id.tvUpdateMessage);
            tvTime = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}