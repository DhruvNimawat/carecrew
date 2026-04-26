package com.example.carecrew;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryTicketAdapter extends RecyclerView.Adapter<HistoryTicketAdapter.ViewHolder> {

    private Context context;
    private List<Complaint> historyList;

    public HistoryTicketAdapter(Context context, List<Complaint> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint ticket = historyList.get(position);

        // Standardized Ticket ID with #SR prefix
        String displayId = ticket.id != null ? ticket.id : context.getString(R.string.not_available);
        if (displayId.length() > 6) {
            displayId = displayId.substring(displayId.length() - 6).toUpperCase();
        }
        holder.tvTicketId.setText(context.getString(R.string.ticket_id_prefix, displayId));

        holder.tvCategory.setText(ticket.category);
        holder.tvLocation.setText(context.getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.tvDescription.setText(ticket.description);

        // Standardized Status Badge logic
        String status = ticket.status != null ? ticket.status : context.getString(R.string.status_unknown);
        holder.tvStatusBadge.setText(status);
        holder.tvStatusBadge.setTextColor(Color.WHITE);

        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText(context.getString(R.string.status_completed));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_completed_green);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText(context.getString(R.string.status_cancelled));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled_red);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText(context.getString(R.string.status_inprogress));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inprogress_blue);
        } else if ("Pending".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText(context.getString(R.string.status_pending));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatusBadge.setTextColor(Color.BLACK);
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_accepted);
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public void updateList(List<Complaint> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvCategory, tvStatusBadge, tvLocation, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
