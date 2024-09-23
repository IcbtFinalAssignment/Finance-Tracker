package com.icbtcampus.budgettracker_assignment.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.icbtcampus.budgettracker_assignment.R;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {
    private PDFView pdfView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfView = findViewById(R.id.pdfView);

        // Get the file path from the intent
        Intent intent = getIntent();
        String pdfFilePath = intent.getStringExtra("pdfFilePath");

        if (pdfFilePath != null) {
            File file = new File(pdfFilePath);
            pdfView.fromFile(file)
                    .load();
        }
    }
}
