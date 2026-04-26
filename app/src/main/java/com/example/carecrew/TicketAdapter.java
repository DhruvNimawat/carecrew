package com.example.carecrew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Complaint> ticketList;

    public TicketAdapter(List<Complaint> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Complaint ticket = ticketList.get(position);

        // Standardized Ticket ID with #SR prefix
        String displayId = ticket.id != null ? ticket.id : holder.itemView.getContext().getString(R.string.not_available);
        if (displayId.length() > 6) {
            displayId = displayId.substring(displayId.length() - 6).toUpperCase();
        }
        holder.tvTicketId.setText(holder.itemView.getContext().getString(R.string.ticket_id_prefix, displayId));

        holder.category.setText(ticket.category);

        // Standardized Status Badge logic
        String status = ticket.status != null ? ticket.status : holder.itemView.getContext().getString(R.string.status_unknown);
        holder.status.setText(status);
        holder.status.setTextColor(android.graphics.Color.WHITE);

        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            holder.status.setText(holder.itemView.getContext().getString(R.string.status_completed));
            holder.status.setBackgroundResource(R.drawable.bg_status_completed_green);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.status.setText(holder.itemView.getContext().getString(R.string.status_cancelled));
            holder.status.setBackgroundResource(R.drawable.bg_status_cancelled_red);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            holder.status.setText(holder.itemView.getContext().getString(R.string.status_inprogress));
            holder.status.setBackgroundResource(R.drawable.bg_status_inprogress_blue);
        } else if ("Pending".equalsIgnoreCase(status)) {
            holder.status.setText(holder.itemView.getContext().getString(R.string.status_pending));
            holder.status.setBackgroundResource(R.drawable.bg_status_pending);
            holder.status.setTextColor(android.graphics.Color.BLACK);
        } else {
            holder.status.setBackgroundResource(R.drawable.bg_status_accepted);
        }

        holder.location.setText(holder.itemView.getContext().getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.description.setText(ticket.description);
        String assignedTo = (ticket.assignedTo != null ? ticket.assignedTo : holder.itemView.getContext().getString(R.string.not_assigned));
        holder.assigned.setText(holder.itemView.getContext().getString(R.string.ticket_assigned_format, assignedTo));
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public void updateList(List<Complaint> newList) {
        this.ticketList = newList;
        notifyDataSetChanged();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView category, status, location, description, assigned, tvTicketId;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            category = itemView.findViewById(R.id.tvCategory);
            status = itemView.findViewById(R.id.tvStatusBadge);
            location = itemView.findViewById(R.id.tvLocation);
            description = itemView.findViewById(R.id.tvDescription);
            assigned = itemView.findViewById(R.id.tvAssignedStatus);
        }
    }
}