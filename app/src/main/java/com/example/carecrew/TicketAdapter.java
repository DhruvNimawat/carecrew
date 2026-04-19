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
        holder.category.setText(ticket.category);
        holder.status.setText(ticket.status);
        holder.location.setText(holder.itemView.getContext().getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.description.setText(ticket.description);
        holder.assigned.setText(holder.itemView.getContext().getString(R.string.ticket_assigned_format, "Not Assigned"));

        // Set status color
        if ("Pending".equalsIgnoreCase(ticket.status)) {
            holder.status.setBackgroundResource(R.drawable.status_pending_bg);
        } else if ("In Progress".equalsIgnoreCase(ticket.status)) {
            // We can add more backgrounds later
            holder.status.setBackgroundResource(R.drawable.status_pending_bg);
        } else if ("Completed".equalsIgnoreCase(ticket.status)) {
            // We can add more backgrounds later
            holder.status.setBackgroundResource(R.drawable.status_pending_bg);
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

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.ticketCategory);
            status = itemView.findViewById(R.id.ticketStatus);
            location = itemView.findViewById(R.id.ticketLocation);
            description = itemView.findViewById(R.id.ticketDescription);
            assigned = itemView.findViewById(R.id.ticketAssigned);
        }
    }
}