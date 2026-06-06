package com.example.techgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import java.util.List;

public class SolutionsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private List<Solution> solutionsList;
    private IncomingSolutionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solutions_list);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rv_incoming_solutions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String user = getIntent().getStringExtra("USER_NAME");
        solutionsList = dbHelper.getUnscoredSolutionsForAuthor(user);

        adapter = new IncomingSolutionsAdapter(solutionsList, dbHelper);
        recyclerView.setAdapter(adapter);
    }

    private class IncomingSolutionsAdapter extends RecyclerView.Adapter<IncomingSolutionsAdapter.ViewHolder> {
        private final List<Solution> list;
        private final DatabaseHelper db;

        public IncomingSolutionsAdapter(List<Solution> list, DatabaseHelper db) {
            this.list = list;
            this.db = db;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_incoming_solution, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            final Solution s = list.get(pos);

            h.tvTitle.setText(s.getTaskTitle() + " (" + s.getTimestamp() + ")");
            MathRenderer.render(h.tvText, s.getText());

            String senderStatus = db.getUserStatus(s.getSender());
            h.tvSender.setText("Отправитель: " + s.getSender() + " [" + senderStatus + "]");

            final Task originalTask = db.getTaskById(s.getTaskId());
            if (originalTask != null && originalTask.getCriteria() != null && !originalTask.getCriteria().isEmpty()) {
                h.tvSender.setText("Отправитель: " + s.getSender() + " [" + senderStatus + "]\nВаши критерии: " + originalTask.getCriteria());
            }

            android.widget.ImageView ivAttached = h.itemView.findViewById(R.id.sol_iv_attached_image);
            if (s.getImageBlob() != null && s.getImageBlob().length > 0) {
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(s.getImageBlob(), 0, s.getImageBlob().length);
                ivAttached.setImageBitmap(bitmap);
                ivAttached.setVisibility(View.VISIBLE);

                ivAttached.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        android.widget.ImageView fullscreenImageView = new android.widget.ImageView(SolutionsListActivity.this);
                        fullscreenImageView.setImageBitmap(bitmap);
                        fullscreenImageView.setAdjustViewBounds(true);
                        fullscreenImageView.setPadding(16, 16, 16, 16);

                        new androidx.appcompat.app.AlertDialog.Builder(SolutionsListActivity.this)
                                .setTitle("Чертеж решения")
                                .setView(fullscreenImageView)
                                .setPositiveButton("Закрыть", null)
                                .show();
                    }
                });
            } else {
                ivAttached.setVisibility(View.GONE);
            }

            h.tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (originalTask != null) {
                        android.widget.LinearLayout dialogLayout = new android.widget.LinearLayout(SolutionsListActivity.this);
                        dialogLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                        dialogLayout.setPadding(32, 24, 32, 24);

                        com.judemanutd.katexview.KatexView kvTaskCondition = new com.judemanutd.katexview.KatexView(SolutionsListActivity.this);

                        String conditionText = "Дисциплина: " + originalTask.getDiscipline() + "\nУсловие задачи:\n" + originalTask.getDescription();

                        kvTaskCondition.setText(conditionText);
                        dialogLayout.addView(kvTaskCondition);

                        if (originalTask.getImageBlob() != null && originalTask.getImageBlob().length > 0) {
                            final android.graphics.Bitmap taskBitmap = android.graphics.BitmapFactory.decodeByteArray(
                                    originalTask.getImageBlob(), 0, originalTask.getImageBlob().length);

                            android.widget.ImageView ivTaskDetail = new android.widget.ImageView(SolutionsListActivity.this);

                            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 400);
                            params.topMargin = 24;
                            ivTaskDetail.setLayoutParams(params);
                            ivTaskDetail.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                            ivTaskDetail.setImageBitmap(taskBitmap);

                            ivTaskDetail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    android.widget.ImageView fullscreenImageView = new android.widget.ImageView(SolutionsListActivity.this);
                                    fullscreenImageView.setImageBitmap(taskBitmap);
                                    fullscreenImageView.setAdjustViewBounds(true);
                                    fullscreenImageView.setPadding(16, 16, 16, 16);

                                    new androidx.appcompat.app.AlertDialog.Builder(SolutionsListActivity.this)
                                            .setTitle("Чертеж к условию задачи")
                                            .setView(fullscreenImageView)
                                            .setPositiveButton("Назад", null)
                                            .show();
                                }
                            });

                            dialogLayout.addView(ivTaskDetail);
                        }

                        android.widget.ScrollView scrollView = new android.widget.ScrollView(SolutionsListActivity.this);
                        scrollView.addView(dialogLayout);

                        new androidx.appcompat.app.AlertDialog.Builder(SolutionsListActivity.this)
                                .setTitle(originalTask.getTitle())
                                .setView(scrollView)
                                .setPositiveButton("Понятно", null)
                                .show();
                    } else {
                        Toast.makeText(SolutionsListActivity.this, "Исходное условие задачи недоступно", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            h.btnRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String scoreStr = h.etScore.getText().toString().trim();
                    if (scoreStr.isEmpty()) {
                        Toast.makeText(SolutionsListActivity.this, "Введите балл", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int score = Integer.parseInt(scoreStr);
                    if (score < 0 || score > 100) {
                        Toast.makeText(SolutionsListActivity.this, "Оценка должна быть от 0 до 100", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.rateSolution(s.getId(), score, s.getSender());
                    Toast.makeText(SolutionsListActivity.this, "Решение оценено на " + score + "/100", Toast.LENGTH_SHORT).show();

                    int currentPos = h.getAdapterPosition();
                    list.remove(currentPos);
                    notifyItemRemoved(currentPos);
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSender;
            TextInputEditText etScore;
            MaterialButton btnRate;
            com.judemanutd.katexview.KatexView tvText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.sol_tv_task_title);
                tvSender = itemView.findViewById(R.id.sol_tv_sender);
                etScore = itemView.findViewById(R.id.sol_et_score);
                btnRate = itemView.findViewById(R.id.sol_btn_rate);

                tvText = itemView.findViewById(R.id.sol_tv_text);
            }
        }
    }
}
