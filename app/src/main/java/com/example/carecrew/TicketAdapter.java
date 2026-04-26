package com.example.carecrew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(Complaint complaint);
    }

    private List<Complaint> ticketList;
    private OnTicketClickListener listener;

    public TicketAdapter(List<Complaint> ticketList) {
        this.ticketList = ticketList;
    }

    public TicketAdapter(List<Complaint> ticketList, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
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
        holder.category.setText(ticket.category);
        holder.status.setText(ticket.status);
        holder.location.setText(holder.itemView.getContext().getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.description.setText(ticket.description);
        holder.assigned.setText(holder.itemView.getContext().getString(R.string.ticket_assigned_format, 
            (ticket.assignedTo != null && !ticket.assignedTo.isEmpty()) ? ticket.assignedTo : "Not Assigned"));

        // Show "Submit Review" only for Completed tickets
        if ("Completed".equalsIgnoreCase(ticket.status) || "Resolved".equalsIgnoreCase(ticket.status)) {
            holder.btnSubmitReview.setVisibility(View.VISIBLE);
        } else {
            holder.btnSubmitReview.setVisibility(View.GONE);
        }

        holder.btnSubmitReview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTicketClick(ticket); // Use click listener to trigger review dialog in activity
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTicketClick(ticket);
            }
        });

        // Set status color
        if ("Pending".equalsIgnoreCase(ticket.status)) {
            holder.status.setBackgroundResource(R.drawable.status_pending_bg);
        } else if ("In Progress".equalsIgnoreCase(ticket.status)) {
            holder.status.setBackgroundResource(R.drawable.status_pending_bg); // Replace with appropriate drawable if exists
        } else if ("Completed".equalsIgnoreCase(ticket.status) || "Resolved".equalsIgnoreCase(ticket.status)) {
            holder.status.setBackgroundResource(R.drawable.tag_bg); // Using tag_bg for now
        }
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public void updateList(List<Complaint> newList) {
        this.ticketList = newList;
        notifyDataSetChanged();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView category, status, location, description, assigned;
        View btnSubmitReview;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.ticketCategory);
            status = itemView.findViewById(R.id.ticketStatus);
            location = itemView.findViewById(R.id.ticketLocation);
            description = itemView.findViewById(R.id.ticketDescription);
            assigned = itemView.findViewById(R.id.ticketAssigned);
            btnSubmitReview = itemView.findViewById(R.id.btnSubmitReview);
        }
    }
}