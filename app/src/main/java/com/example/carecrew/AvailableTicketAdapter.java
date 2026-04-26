package com.example.carecrew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AvailableTicketAdapter extends RecyclerView.Adapter<AvailableTicketAdapter.ViewHolder> {

    private Context context;
    private List<Complaint> ticketList;
    private OnTicketActionListener listener;

    public interface OnTicketActionListener {
        void onAccept(Complaint ticket);
        void onView(Complaint ticket);
    }

    public AvailableTicketAdapter(Context context, List<Complaint> ticketList, OnTicketActionListener listener) {
        this.context = context;
        this.ticketList = ticketList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_available_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint ticket = ticketList.get(position);

        // Standardized Ticket ID with #SR prefix
        String displayId = ticket.id != null ? ticket.id : context.getString(R.string.not_available);
        if (displayId.length() > 6) {
            displayId = displayId.substring(displayId.length() - 6).toUpperCase();
        }
        holder.tvTicketId.setText(context.getString(R.string.ticket_id_prefix, displayId));

        holder.tvCategory.setText(ticket.category);
        holder.tvLocation.setText(context.getString(R.string.ticket_room_format, (ticket.roomNumber != null ? ticket.roomNumber : context.getString(R.string.not_available))));
        holder.tvDescription.setText(ticket.description);

        // Standardized Status Badge logic (using status for display, but colored by priority in this specific adapter)
        String status = ticket.status != null ? ticket.status : context.getString(R.string.status_pending);
        holder.tvStatusBadge.setText(status);
        holder.tvStatusBadge.setTextColor(android.graphics.Color.WHITE);

        String assignedTo = (ticket.assignedTo != null && !ticket.assignedTo.isEmpty() ? ticket.assignedTo : context.getString(R.string.unassigned));
        holder.tvAssignedStatus.setText(context.getString(R.string.ticket_assigned_format, assignedTo));
        
        // Visual Styling based on priority
        if ("Urgent".equalsIgnoreCase(ticket.priority) || "High".equalsIgnoreCase(ticket.priority)) {
            holder.tvStatusBadge.setText(context.getString(R.string.status_urgent));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_urgent);
        } else if ("Medium".equalsIgnoreCase(ticket.priority)) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inprogress_blue);
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatusBadge.setTextColor(android.graphics.Color.BLACK);
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(ticket));
        holder.btnView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, TicketDetailsStaffActivity.class);
            intent.putExtra("ticketId", ticket.id);
            intent.putExtra("category", ticket.category);
            intent.putExtra("location", context.getString(R.string.ticket_room_format, ticket.roomNumber));
            intent.putExtra("priority", ticket.priority);
            intent.putExtra("description", ticket.description);
            intent.putExtra("imageUrl", ticket.imageUrl);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public void updateList(List<Complaint> newList) {
        this.ticketList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvStatusBadge, tvLocation, tvDescription, tvAssignedStatus, tvTicketId;
        MaterialButton btnAccept, btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAssignedStatus = itemView.findViewById(R.id.tvAssignedStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}