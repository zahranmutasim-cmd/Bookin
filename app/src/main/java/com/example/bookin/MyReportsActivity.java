package com.example.bookin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateContainer;
    private List<Report> reportList;
    private ReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        initViews();
        loadReports();
    }

    private void initViews() {
        ImageButton backButton = findViewById(R.id.back_button);
        recyclerView = findViewById(R.id.reports_recycler_view);
        emptyStateContainer = findViewById(R.id.empty_state_container);

        backButton.setOnClickListener(v -> finish());

        reportList = new ArrayList<>();
        adapter = new ReportAdapter(reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadReports() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showEmptyState();
            return;
        }

        DatabaseReference reportsRef = FirebaseDatabase.getInstance()
                .getReference("userReports")
                .child(user.getUid());

        reportsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                    Report report = reportSnapshot.getValue(Report.class);
                    if (report != null) {
                        report.setReportId(reportSnapshot.getKey());
                        reportList.add(0, report); // Add to front for reverse order
                    }
                }

                if (reportList.isEmpty()) {
                    showEmptyState();
                } else {
                    showReportsList();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showReportsList() {
        emptyStateContainer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    // Adapter class
    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

        private final List<Report> reports;

        public ReportAdapter(List<Report> reports) {
            this.reports = reports;
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            Report report = reports.get(position);

            holder.bookTitle.setText(report.getBookTitle());
            holder.sellerName.setText("Dilaporkan: " + report.getSellerName());
            holder.reason.setText("Alasan: " + report.getReason());

            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            holder.date.setText(sdf.format(new Date(report.getTimestamp())));

            // Set status
            String status = report.getStatus();
            if (status == null)
                status = "pending";

            switch (status) {
                case "resolved":
                    holder.status.setText("Selesai");
                    holder.status.setBackgroundResource(R.drawable.badge_success_bg);
                    break;
                case "reviewed":
                    holder.status.setText("Ditinjau");
                    holder.status.setBackgroundResource(R.drawable.badge_pending_bg);
                    break;
                default:
                    holder.status.setText("Diproses");
                    holder.status.setBackgroundResource(R.drawable.badge_pending_bg);
                    break;
            }

            // Load book image
            if (report.getBookImage() != null && !report.getBookImage().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(report.getBookImage())
                        .placeholder(R.drawable.default_book)
                        .into(holder.bookImage);
            }
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            ImageView bookImage;
            TextView bookTitle, sellerName, reason, date, status;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                bookImage = itemView.findViewById(R.id.report_book_image);
                bookTitle = itemView.findViewById(R.id.report_book_title);
                sellerName = itemView.findViewById(R.id.report_seller_name);
                reason = itemView.findViewById(R.id.report_reason);
                date = itemView.findViewById(R.id.report_date);
                status = itemView.findViewById(R.id.report_status);
            }
        }
    }
}
