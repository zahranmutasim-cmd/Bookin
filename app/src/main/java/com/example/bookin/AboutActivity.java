package com.example.bookin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutActivity extends BaseActivity {

    private CircleImageView profileImage;
    private TextView userName;
    private TextView greetingText;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupBottomNavigationBar();

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        greetingText = findViewById(R.id.greeting_text);
        searchEditText = findViewById(R.id.search_edit_text);
        ImageView settingsIcon = findViewById(R.id.settings_icon);

        // Load user profile
        loadUserProfile();

        // Settings icon click (optional - can navigate to settings)
        settingsIcon.setOnClickListener(v -> {
            // Could open settings activity if needed
        });

        // Setup FAQ item click listeners
        setupFaqClickListeners();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set user name
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                userName.setText(displayName);
                
                // Set greeting text with user name in uppercase
                String greeting = "Hallo " + displayName.toUpperCase() + ",\nTemukan semua informasi yang kamu butuhkan disini.";
                greetingText.setText(greeting);
            }

            // Load profile image
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_profile)
                        .into(profileImage);
            }
        }
    }

    private void setupFaqClickListeners() {
        // FAQ Item 1 - Apa itu Aplikasi BOOKin?
        findViewById(R.id.faq_item_1).setOnClickListener(v -> {
            // Can open detail page or show dialog with answer
            showFaqAnswer("Apa itu Aplikasi BOOKin?", 
                "BOOKin adalah aplikasi marketplace buku yang memudahkan pengguna untuk menjual dan membeli buku bekas maupun baru. Aplikasi ini menghubungkan penjual dan pembeli buku di seluruh Indonesia.");
        });

        // FAQ Item 2 - Bagaimana cara menjual buku?
        findViewById(R.id.faq_item_2).setOnClickListener(v -> {
            showFaqAnswer("Bagaimana cara menjual buku?", 
                "Untuk menjual buku:\n1. Klik tombol '+' di navigasi bawah\n2. Pilih kategori buku\n3. Upload foto buku (depan dan belakang)\n4. Isi detail seperti tipe, kondisi, dan lokasi\n5. Isi judul, deskripsi, dan harga\n6. Klik 'Pasang Iklan'");
        });

        // FAQ Item 3 - Bagaimana cara membeli buku?
        findViewById(R.id.faq_item_3).setOnClickListener(v -> {
            showFaqAnswer("Bagaimana cara membeli buku?", 
                "Untuk membeli buku:\n1. Cari buku yang diinginkan di halaman Home\n2. Klik buku untuk melihat detail\n3. Hubungi penjual melalui fitur chat\n4. Diskusikan harga dan metode pembayaran\n5. Lakukan transaksi dengan penjual");
        });

        // FAQ Item 4 - Apa itu Rating Akun?
        findViewById(R.id.faq_item_4).setOnClickListener(v -> {
            showFaqAnswer("Apa itu Rating Akun?", 
                "Rating Akun adalah penilaian yang diberikan oleh pengguna lain berdasarkan pengalaman transaksi. Rating membantu pengguna lain mengetahui reputasi penjual atau pembeli.");
        });

        // FAQ Item 5 - Apa itu Menu Wishlist?
        findViewById(R.id.faq_item_5).setOnClickListener(v -> {
            showFaqAnswer("Apa itu Menu Wishlist?", 
                "Wishlist adalah fitur untuk menyimpan buku-buku yang ingin Anda beli nanti. Anda dapat menambahkan buku ke wishlist dengan menekan ikon hati pada halaman detail buku.");
        });

        // FAQ Item 6 - Bagaimana cara komunikasi?
        findViewById(R.id.faq_item_6).setOnClickListener(v -> {
            showFaqAnswer("Bagaimana cara komunikasi antara penjual dan pembeli?", 
                "Penjual dan pembeli dapat berkomunikasi melalui fitur Chat yang tersedia di aplikasi. Klik tombol 'Kirim Pesan' pada halaman detail buku untuk memulai percakapan dengan penjual.");
        });

        // FAQ Item 7 - Apa bedanya penjual dan pembeli?
        findViewById(R.id.faq_item_7).setOnClickListener(v -> {
            showFaqAnswer("Apa bedanya penjual dan pembeli?", 
                "Di BOOKin, setiap pengguna dapat menjadi penjual dan pembeli sekaligus. Sebagai penjual, Anda dapat memasang iklan buku. Sebagai pembeli, Anda dapat mencari dan membeli buku dari penjual lain.");
        });

        // FAQ Item 8 - Buku apa saja yang dijual?
        findViewById(R.id.faq_item_8).setOnClickListener(v -> {
            showFaqAnswer("Buku apa saja yang di jual di Aplikasi BOOKIn?", 
                "BOOKin menyediakan berbagai kategori buku:\n• Buku Gratis\n• Buku Pelajaran\n• Novel\n• Komik\n• Buku Cerita\n• Kamus Bahasa\n• Buku Anak\n• Majalah\n• Buku Keuangan\n• Self Improvement");
        });

        // FAQ Item 9 - Cara melaporkan penipuan
        findViewById(R.id.faq_item_9).setOnClickListener(v -> {
            showFaqAnswer("Bagaimana cara melaporkan penipuan?", 
                "Jika Anda mengalami atau mencurigai penipuan:\n1. Buka halaman Profil\n2. Pilih 'Pusat Bantuan'\n3. Pilih 'Laporkan Masalah'\n4. Jelaskan detail kejadian\n5. Sertakan bukti seperti screenshot chat atau bukti transfer");
        });
    }

    private void showFaqAnswer(String question, String answer) {
        // Create and show a dialog with the FAQ answer
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(question);
        builder.setMessage(answer);
        builder.setPositiveButton("Mengerti", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
