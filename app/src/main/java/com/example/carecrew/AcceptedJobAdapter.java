package com.example.carecrew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AcceptedJobAdapter extends RecyclerView.Adapter<AcceptedJobAdapter.ViewHolder> {

    private Context context;
    private List<Complaint> acceptedList;

    public AcceptedJobAdapter(Context context, List<Complaint> acceptedList) {
        this.context = context;
        this.acceptedList = acceptedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_accepted_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint ticket = acceptedList.get(position);

        // Standardized Ticket ID with #SR prefix
        String displayId = ticket.id != null ? ticket.id : context.getString(R.string.not_available);
        if (displayId.length() > 6) {
            displayId = displayId.substring(displayId.length() - 6).toUpperCase();
        }
        holder.tvTicketId.setText(context.getString(R.string.ticket_id_prefix, displayId));

        holder.tvCategory.setText(ticket.category);
        String user = (ticket.userName != null ? ticket.userName : context.getString(R.string.not_available));
        holder.tvUser.setText(context.getString(R.string.ticket_user_format, user));
        holder.tvLocation.setText(context.getString(R.string.ticket_room_format, ticket.roomNumber));
        holder.tvDescription.setText(ticket.description);

        // Standardized Status Badge logic
        String status = ticket.status != null ? ticket.status : context.getString(R.string.status_accepted);
        holder.tvStatusBadge.setText(status);
        holder.tvStatusBadge.setTextColor(android.graphics.Color.WHITE);

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
            holder.tvStatusBadge.setTextColor(android.graphics.Color.BLACK);
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_accepted);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, JobUpdateStaffActivity.class);
            intent.putExtra("ticketId", ticket.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return acceptedList.size();
    }

    public void updateList(List<Complaint> newList) {
        this.acceptedList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvCategory, tvUser, tvStatusBadge, tvLocation, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}