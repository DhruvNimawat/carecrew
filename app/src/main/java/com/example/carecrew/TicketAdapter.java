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
        
        // Ticket ID formatting
        String displayId = ticket.id != null ? ticket.id : "N/A";
        if (displayId.length() > 6) {
            displayId = displayId.substring(displayId.length() - 6).toUpperCase();
        }
        holder.ticketId.setText(holder.itemView.getContext().getString(R.string.ticket_id_prefix, displayId));

        holder.category.setText(ticket.category);
        holder.status.setText(ticket.status);
        holder.location.setText(holder.itemView.getContext().getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.description.setText(ticket.description);
        
        String assignedStaff = (ticket.assignedTo != null && !ticket.assignedTo.isEmpty()) ? ticket.assignedTo : "Not Assigned";
        holder.assigned.setText(holder.itemView.getContext().getString(R.string.ticket_assigned_format, assignedStaff));

        // Entrance animation for items
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(100f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(position * 50L)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

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

        // Standardized status styling
        String status = ticket.status != null ? ticket.status : "Pending";
        holder.status.setTextColor(android.graphics.Color.WHITE);

        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            holder.status.setBackgroundResource(R.drawable.bg_status_completed_green);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.status.setBackgroundResource(R.drawable.bg_status_cancelled_red);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            holder.status.setBackgroundResource(R.drawable.bg_status_inprogress_blue);
        } else if ("Pending".equalsIgnoreCase(status)) {
            holder.status.setBackgroundResource(R.drawable.bg_status_pending);
            holder.status.setTextColor(android.graphics.Color.BLACK);
        } else {
            holder.status.setBackgroundResource(R.drawable.bg_status_accepted);
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
        TextView category, status, location, description, assigned, ticketId;
        View btnSubmitReview;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.tvCategory);
            status = itemView.findViewById(R.id.tvStatusBadge);
            location = itemView.findViewById(R.id.tvLocation);
            description = itemView.findViewById(R.id.tvDescription);
            assigned = itemView.findViewById(R.id.tvAssignedStatus);
            ticketId = itemView.findViewById(R.id.tvTicketId);
            btnSubmitReview = itemView.findViewById(R.id.btnSubmitReview);
        }
    }
}