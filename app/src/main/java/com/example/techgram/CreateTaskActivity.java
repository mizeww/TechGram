package com.example.techgram;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;

public class CreateTaskActivity extends AppCompatActivity {

    private TextInputEditText etDiscipline, etTitle, etDesc, etCriteria;
    private MaterialButton btnSaveTask, btnAttach;
    private ImageView ivPreview;
    private DatabaseHelper dbHelper;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        dbHelper = new DatabaseHelper(this);

        etDiscipline = findViewById(R.id.et_task_discipline);
        etTitle = findViewById(R.id.et_task_title);
        etDesc = findViewById(R.id.et_task_desc);
        etCriteria = findViewById(R.id.et_task_criteria);
        btnSaveTask = findViewById(R.id.btn_save_task);
        btnAttach = findViewById(R.id.btn_task_attach_photo);
        ivPreview = findViewById(R.id.iv_task_create_preview);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedImageUri = uri;
                            ivPreview.setImageURI(uri);
                            ivPreview.setVisibility(View.VISIBLE);
                            btnAttach.setText("Чертеж условия добавлен");

                            ivPreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImageView fullscreenImageView = new ImageView(CreateTaskActivity.this);
                                    fullscreenImageView.setImageURI(selectedImageUri);
                                    fullscreenImageView.setAdjustViewBounds(true);
                                    fullscreenImageView.setPadding(16, 16, 16, 16);

                                    new androidx.appcompat.app.AlertDialog.Builder(CreateTaskActivity.this)
                                            .setTitle("Просмотр схемы условия")
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

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String discipline = etDiscipline.getText().toString().trim();
                String title = etTitle.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                String criteria = etCriteria.getText().toString().trim();

                if (discipline.isEmpty() || title.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(CreateTaskActivity.this, "Заполните основные поля задачи", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentAuthor = getIntent().getStringExtra("USER_NAME");
                if (currentAuthor == null) { currentAuthor = "Гость"; }

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

                boolean success = dbHelper.addTask(title, desc, discipline, currentDateTime, currentAuthor, criteria, compressedImageBytes);

                if (success) {
                    Toast.makeText(CreateTaskActivity.this, "Задача успешно опубликована!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateTaskActivity.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
