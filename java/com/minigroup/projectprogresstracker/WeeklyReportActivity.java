package com.minigroup.projectprogresstracker;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeeklyReportActivity extends AppCompatActivity {

    private TextInputEditText etReportContent, etChallenges;
    private TextView txtCurrentDateTime, txtSubmitterEmail;
    private LinearLayout layoutPreviousReports;
    private MaterialButton btnSubmit;

    // Logic IDs
    private String rawGroupId;     // Internal DB Key
    private String groupName;      // Project Title/Name
    private String displayGroupId; // Alphanumeric Code
    private User user;

    // PDF Variables
    private PdfDocument document;
    private PdfDocument.Page currentPage;
    private Canvas canvas;
    private Paint paint;
    private int currentY;
    private int pageNumber = 1;
    private final int PAGE_WIDTH = 595;
    private final int PAGE_HEIGHT = 842;
    private final int MARGIN = 50;
    private final int LINE_HEIGHT = 22;
    private final int BOTTOM_LIMIT = 780;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        user = SessionManager.getUser(this);
        rawGroupId = getIntent().getStringExtra("GROUP_ID");

        // Try to get title from intent first
        String projectTitle = getIntent().getStringExtra("PROJECT_TITLE");

        if (rawGroupId == null || user == null) {
            Toast.makeText(this, "Error: Session or Group missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- FETCH GROUP DETAILS ---
        Group group = GroupStorage.getGroupById(this, rawGroupId);
        if (group != null) {
            displayGroupId = group.getGroupId(); // 6-char code
            groupName = group.getProjectTitle();  // Group Name/Project Title
        } else {
            displayGroupId = rawGroupId;
            groupName = (projectTitle != null) ? projectTitle : "Unknown Project";
        }

        initViews();
        setupToolbar(groupName); // Show Group Name in Toolbar
        displaySessionDetails();
        loadHistory();

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void initViews() {
        etReportContent = findViewById(R.id.etReportContent);
        etChallenges = findViewById(R.id.etChallenges);
        btnSubmit = findViewById(R.id.btnSubmitFinalReport);
        txtCurrentDateTime = findViewById(R.id.txtCurrentDateTime);
        txtSubmitterEmail = findViewById(R.id.txtSubmitterEmail);
        layoutPreviousReports = findViewById(R.id.layoutPreviousReports);
    }

    private void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weekly Report");
            if (title != null) toolbar.setSubtitle(title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displaySessionDetails() {
        String currentDateTime = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).format(new Date());
        txtCurrentDateTime.setText("Date: " + currentDateTime);
        txtSubmitterEmail.setText("Submitter: " + user.getEmail());
    }

    private void loadHistory() {
        if (layoutPreviousReports == null) return;
        layoutPreviousReports.removeAllViews();

        ArrayList<WeeklyReport> reports = ReportStorage.getReportsByGroupId(this, rawGroupId);

        if (reports == null || reports.isEmpty()) {
            TextView emptyTxt = new TextView(this);
            emptyTxt.setText("No previous reports submitted.");
            emptyTxt.setPadding(20, 40, 20, 20);
            emptyTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layoutPreviousReports.addView(emptyTxt);
        } else {
            for (WeeklyReport report : reports) {
                addPreviousReportItem(report);
            }
        }
    }

    private void addPreviousReportItem(WeeklyReport report) {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        String dateStr = (report.getDate() != null) ? report.getDate() : "Unknown Date";
        btn.setText("View Report: " + dateStr);
        btn.setAllCaps(false);
        btn.setIconResource(R.drawable.ic_attach);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> openPdfFile(report.getPdfUri()));
        layoutPreviousReports.addView(btn);
    }

    private void submitReport() {
        String content = etReportContent.getText().toString().trim();
        String challenges = etChallenges.getText().toString().trim();

        if (content.isEmpty()) {
            etReportContent.setError("Report details are required");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String dateOnly = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        String timeOnly = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        // File name now uses Group Name (cleaned of spaces)
        String cleanGroupName = groupName.replaceAll("\\s+", "_");
        String fileName = "Report_" + cleanGroupName + "_" + timestamp + ".pdf";

        File pdfFile = createPdfFile(fileName, content, challenges);

        if (pdfFile != null) {
            WeeklyReport report = new WeeklyReport(
                    rawGroupId,
                    displayGroupId, // Still keep the 6-char code for data model
                    user.getEmail(),
                    content,
                    dateOnly,
                    timeOnly,
                    pdfFile.getAbsolutePath()
            );

            if (ReportStorage.saveReport(this, report)) {
                Toast.makeText(this, "✅ Report Submitted Successfully!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void addNewPage() {
        if (currentPage != null) {
            document.finishPage(currentPage);
        }
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        currentPage = document.startPage(pageInfo);
        canvas = currentPage.getCanvas();
        currentY = MARGIN + 20;
        pageNumber++;
    }

    private File createPdfFile(String fileName, String content, String challenges) {
        document = new PdfDocument();
        paint = new Paint();
        pageNumber = 1;

        addNewPage();

        // PDF Header
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText("WEEKLY PROGRESS REPORT", MARGIN, currentY, paint);
        currentY += 40;

        paint.setFakeBoldText(false);
        paint.setTextSize(12f);
        canvas.drawText("Student: " + user.getEmail(), MARGIN, currentY, paint);
        currentY += 20;

        // --- SHOW GROUP NAME HERE ---
        paint.setFakeBoldText(true);
        canvas.drawText("Project Title: " + groupName, MARGIN, currentY, paint);
        paint.setFakeBoldText(false);

        currentY += 20;
        canvas.drawText("Generated: " + txtCurrentDateTime.getText().toString(), MARGIN, currentY, paint);
        currentY += 40;

        paint.setFakeBoldText(true);
        paint.setTextSize(14f);
        canvas.drawText("Summary of Work:", MARGIN, currentY, paint);
        currentY += 25;
        paint.setFakeBoldText(false);
        paint.setTextSize(12f);

        drawWrappedText(content);

        if (!challenges.isEmpty()) {
            currentY += 30;
            if (currentY > BOTTOM_LIMIT - 60) addNewPage();

            paint.setFakeBoldText(true);
            paint.setTextSize(14f);
            canvas.drawText("Challenges Faced:", MARGIN, currentY, paint);
            currentY += 25;
            paint.setFakeBoldText(false);
            paint.setTextSize(12f);

            drawWrappedText(challenges);
        }

        document.finishPage(currentPage);

        File file = new File(getExternalFilesDir(null), fileName);
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (IOException e) {
            Log.e("PDF_GEN", "Error saving PDF: " + e.getMessage());
            return null;
        }
    }

    private void drawWrappedText(String text) {
        int maxWidth = PAGE_WIDTH - (MARGIN * 2);
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line + word + " ";
            float lineWidth = paint.measureText(testLine);

            if (lineWidth > maxWidth) {
                canvas.drawText(line.toString(), MARGIN, currentY, paint);
                line = new StringBuilder(word + " ");
                currentY += LINE_HEIGHT;

                if (currentY > BOTTOM_LIMIT) {
                    addNewPage();
                }
            } else {
                line.append(word).append(" ");
            }
        }
        canvas.drawText(line.toString(), MARGIN, currentY, paint);
        currentY += LINE_HEIGHT;
    }

    private void openPdfFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Uri path = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}