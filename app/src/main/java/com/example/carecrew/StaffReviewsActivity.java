package com.example.carecrew;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StaffReviewsActivity extends AppCompatActivity {

    private RecyclerView rvStaffReviews;
    private EditText etSearchStaff;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private DatabaseReference mDatabase;
    private StaffAdapter adapter;
    private List<StaffModel> staffList = new ArrayList<>();
    private List<StaffModel> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_reviews);

        rvStaffReviews = findViewById(R.id.rvStaffReviews);
        etSearchStaff = findViewById(R.id.etSearchStaff);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        setupRecyclerView();
        fetchStaffData();
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new StaffAdapter(filteredList);
        rvStaffReviews.setLayoutManager(new LinearLayoutManager(this));
        rvStaffReviews.setAdapter(adapter);
    }

    private void fetchStaffData() {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("Users").orderByChild("role").equalTo("Staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                staffList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StaffModel staff = ds.getValue(StaffModel.class);
                    if (staff != null) {
                        // Assuming reviews might be stored under each staff or a separate Reviews node
                        // For now, setting dummy values or fetching from a child node if it exists
                        float rating = ds.hasChild("avgRating") ? ds.child("avgRating").getValue(Float.class) : 0f;
                        int reviewCount = ds.hasChild("reviewCount") ? ds.child("reviewCount").getValue(Integer.class) : 0;
                        staff.setAvgRating(rating);
                        staff.setReviewCount(reviewCount);
                        staffList.add(staff);
                    }
                }
                updateList(staffList);
                progressBar.setVisibility(View.GONE);
                tvNoData.setVisibility(staffList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StaffReviewsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearchStaff.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<StaffModel> temp = new ArrayList<>();
        for (StaffModel s : staffList) {
            if (s.getName().toLowerCase().contains(text.toLowerCase()) || 
                s.getCategory().toLowerCase().contains(text.toLowerCase())) {
                temp.add(s);
            }
        }
        updateList(temp);
    }

    private void updateList(List<StaffModel> list) {
        filteredList.clear();
        filteredList.addAll(list);
        adapter.notifyDataSetChanged();
        tvNoData.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Inner Model Class
    public static class StaffModel {
        private String name, email, category, role;
        private float avgRating;
        private int reviewCount;

        public StaffModel() {} // Required for Firebase

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getCategory() { return category != null ? category : "General"; }
        public float getAvgRating() { return avgRating; }
        public int getReviewCount() { return reviewCount; }
        
        public void setAvgRating(float r) { this.avgRating = r; }
        public void setReviewCount(int c) { this.reviewCount = c; }
    }

    // Inner Adapter Class
    private class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
        private List<StaffModel> list;

        public StaffAdapter(List<StaffModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_review, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StaffModel staff = list.get(position);
            holder.tvName.setText(staff.getName());
            holder.tvEmail.setText(staff.getEmail());
            holder.tvCategory.setText(staff.getCategory());
            holder.ratingBar.setRating(staff.getAvgRating());
            holder.tvRatingText.setText(String.format("%.1f/5.0", staff.getAvgRating()));
            holder.tvReviews.setText("(" + staff.getReviewCount() + " reviews)");

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(StaffReviewsActivity.this, "No reviews available for " + staff.getName() + " yet.", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvCategory, tvRatingText, tvReviews;
            RatingBar ratingBar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvStaffName);
                tvEmail = itemView.findViewById(R.id.tvStaffEmail);
                tvCategory = itemView.findViewById(R.id.tvStaffCategory);
                tvRatingText = itemView.findViewById(R.id.tvRatingText);
                tvReviews = itemView.findViewById(R.id.tvTotalReviews);
                ratingBar = itemView.findViewById(R.id.staffRatingBar);
            }
        }
    }
}