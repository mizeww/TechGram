package com.example.techgram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvDiscipline, tvTitle, tvTime, tvAuthor, tvCriteria;
    private LinearLayout authorCriteriaBlock, solverSendBlock;
    private TextInputEditText etSolutionInput;
    private MaterialButton btnSendSolution, btnAttach;
    private ImageView ivPreview;
    private com.judemanutd.katexview.KatexView tvDesc;


    private Uri selectedImageUri = null;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        dbHelper = new DatabaseHelper(this);

        tvDiscipline = findViewById(R.id.detail_tv_discipline);
        tvTitle = findViewById(R.id.detail_tv_title);
        tvTime = findViewById(R.id.detail_tv_time);
        tvAuthor = findViewById(R.id.detail_tv_author);
        tvDesc = findViewById(R.id.detail_tv_desc);


        authorCriteriaBlock = findViewById(R.id.author_criteria_block);
        tvCriteria = findViewById(R.id.detail_tv_criteria);

        solverSendBlock = findViewById(R.id.solver_send_block);
        etSolutionInput = findViewById(R.id.et_solution_input);
        btnSendSolution = findViewById(R.id.btn_send_solution);
        btnAttach = findViewById(R.id.btn_attach_photo);
        ivPreview = findViewById(R.id.iv_solution_preview);

        final String taskId = getIntent().getStringExtra("TASK_ID") != null ? getIntent().getStringExtra("TASK_ID") : "1";
        final String taskTitle = getIntent().getStringExtra("TASK_TITLE");
        final String taskDesc = getIntent().getStringExtra("TASK_DESC");
        final String taskDiscipline = getIntent().getStringExtra("TASK_DISCIPLINE");
        final String taskTime = getIntent().getStringExtra("TASK_TIME");
        final String authorName = getIntent().getStringExtra("TASK_AUTHOR");
        final String criteriaText = getIntent().getStringExtra("TASK_CRITERIA");

        tvTitle.setText(taskTitle);
        MathRenderer.render(tvDesc, taskDesc);
        tvDiscipline.setText(taskDiscipline);
        tvTime.setText(taskTime);
        tvAuthor.setText("Автор: " + authorName);

        SharedPreferences sharedPref = getSharedPreferences("TechGramPrefs", Context.MODE_PRIVATE);
        final String currentViewer = sharedPref.getString("CURRENT_USER", "Гость");

        if (authorName != null && authorName.equals(currentViewer) && criteriaText != null && !criteriaText.isEmpty()) {
            authorCriteriaBlock.setVisibility(View.VISIBLE);
            tvCriteria.setText(criteriaText);
        } else {
            authorCriteriaBlock.setVisibility(View.GONE);
        }

        if (authorName != null && authorName.equals(currentViewer)) {
            solverSendBlock.setVisibility(View.GONE);
        } else {
            solverSendBlock.setVisibility(View.VISIBLE);
        }

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedImageUri = uri;
                            ivPreview.setImageURI(uri);
                            ivPreview.setVisibility(View.VISIBLE);
                            btnAttach.setText("Чертеж успешно прикреплен");

                            ivPreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImageView fullscreenImageView = new ImageView(TaskDetailActivity.this);
                                    fullscreenImageView.setImageURI(selectedImageUri);
                                    fullscreenImageView.setAdjustViewBounds(true);
                                    fullscreenImageView.setPadding(16, 16, 16, 16);

                                    new androidx.appcompat.app.AlertDialog.Builder(TaskDetailActivity.this)
                                            .setTitle("Просмотр вложения")
                                            .setView(fullscreenImageView)
                                            .setPositiveButton("Назад", null)
                                            .show();
                                }
                            });
                        }
                    }
                });


        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPickerLauncher.launch("image/*");
            }
        });

        btnSendSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String solutionText = etSolutionInput.getText().toString().trim();
                if (solutionText.isEmpty()) {
                    Toast.makeText(TaskDetailActivity.this, "Введите текст решения", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] compressedImageBytes = null;

                if (selectedImageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        if (bitmap.getWidth() > 800) {
                            int nh = (int) (bitmap.getHeight() * (800.0 / bitmap.getWidth()));
                            bitmap = Bitmap.createScaledBitmap(bitmap, 800, nh, true);
                        }

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        compressedImageBytes = stream.toByteArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String currentDateTime = new java.text.SimpleDateFormat("dd.MM.yyyy 'в' HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

                boolean success = dbHelper.addSolution(taskId, taskTitle, currentViewer, authorName, solutionText, currentDateTime, compressedImageBytes);

                if (success) {
                    Toast.makeText(TaskDetailActivity.this, "Решение успешно отправлено!", Toast.LENGTH_SHORT).show();
                    etSolutionInput.setText("");
                    finish();
                } else {
                    Toast.makeText(TaskDetailActivity.this, "Ошибка при сохранении решения", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, MainActivity.class);
                intent.putExtra("USER_NAME", authorName);
                intent.putExtra("IS_VIEW_ONLY", true);
                startActivity(intent);
            }
        });

        ImageView ivTaskImage = findViewById(R.id.detail_iv_task_image);

        final Task currentDbTask = dbHelper.getTaskById(taskId);

        if (currentDbTask != null && currentDbTask.getImageBlob() != null && currentDbTask.getImageBlob().length > 0) {
            final Bitmap taskBitmap = BitmapFactory.decodeByteArray(currentDbTask.getImageBlob(), 0, currentDbTask.getImageBlob().length);
            ivTaskImage.setImageBitmap(taskBitmap);
            ivTaskImage.setVisibility(View.VISIBLE);

            ivTaskImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView fullscreenImageView = new ImageView(TaskDetailActivity.this);
                    fullscreenImageView.setImageBitmap(taskBitmap);
                    fullscreenImageView.setAdjustViewBounds(true);
                    fullscreenImageView.setPadding(16, 16, 16, 16);

                    new androidx.appcompat.app.AlertDialog.Builder(TaskDetailActivity.this)
                            .setTitle("Чертеж к условию задачи")
                            .setView(fullscreenImageView)
                            .setPositiveButton("Закрыть", null)
                            .show();
                }
            });
        } else {
            ivTaskImage.setVisibility(View.GONE);
        }

    }
}
