package com.example.carecrew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private final List<Announcement> announcementList;
    private final OnAnnouncementDeleteListener deleteListener;

    public interface OnAnnouncementDeleteListener {
        void onDelete(Announcement announcement);
    }

    public AnnouncementAdapter(List<Announcement> announcementList, OnAnnouncementDeleteListener deleteListener) {
        this.announcementList = announcementList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        holder.bind(announcement, deleteListener);
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp;
        com.google.android.material.button.MaterialButton btnDelete;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAnnTitle);
            tvMessage = itemView.findViewById(R.id.tvAnnMessage);
            tvTimestamp = itemView.findViewById(R.id.tvAnnTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteAnn);
        }

        public void bind(Announcement announcement, OnAnnouncementDeleteListener deleteListener) {
            tvTitle.setText(announcement.getTitle());
            tvMessage.setText(announcement.getMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String dateStr = sdf.format(new Date(announcement.getTimestamp()));
            tvTimestamp.setText(dateStr);

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(announcement);
                }
            });
        }
    }
}
