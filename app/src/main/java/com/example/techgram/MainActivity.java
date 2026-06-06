package com.example.techgram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername, tvUserStatus, tvRating, tvPublishedCount, tvSolvedCount;
    private MaterialButton btnCreateTask, btnSolveTasks, btnAuthGate;
    private DatabaseHelper dbHelper;
    private String currentUsername = null;
    private MaterialButton btnMyTasks, btnMySolutions, btnViewSolutions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.content.SharedPreferences sharedPref = getSharedPreferences("TechGramPrefs", MODE_PRIVATE);
        currentUsername = sharedPref.getString("CURRENT_USER", null);



        dbHelper = new DatabaseHelper(this);

        tvUsername = findViewById(R.id.tv_username);
        tvUserStatus = findViewById(R.id.tv_user_status);
        tvRating = findViewById(R.id.tv_rating);
        tvPublishedCount = findViewById(R.id.tv_published_count);
        tvSolvedCount = findViewById(R.id.tv_solved_count);
        btnCreateTask = findViewById(R.id.btn_create_task);
        btnSolveTasks = findViewById(R.id.btn_solve_tasks);
        btnAuthGate = findViewById(R.id.btn_auth_gate);
        btnViewSolutions = findViewById(R.id.btn_view_solutions);
        btnMyTasks = findViewById(R.id.btn_my_tasks);
        btnMySolutions = findViewById(R.id.btn_my_solutions);
        btnViewSolutions = findViewById(R.id.btn_view_solutions);


        currentUsername = getIntent().getStringExtra("USER_NAME");
        updateProfileUI();

        btnMyTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                intent.putExtra("FILTER_AUTHOR", currentUsername != null ? currentUsername : "Гость");
                startActivity(intent);
            }
        });

        btnMySolutions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MySolutionsActivity.class);
                intent.putExtra("USER_NAME", currentUsername != null ? currentUsername : "Гость");
                startActivity(intent);
            }
        });



        btnViewSolutions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SolutionsListActivity.class);
                intent.putExtra("USER_NAME", currentUsername != null ? currentUsername : "Гость");
                startActivity(intent);
            }
        });


        btnAuthGate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUsername == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    android.content.SharedPreferences sharedPref = getSharedPreferences("TechGramPrefs", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove("CURRENT_USER");
                    editor.apply();

                    currentUsername = null;
                    updateProfileUI();
                }
            }
        });

        btnSolveTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                intent.putExtra("USER_NAME", currentUsername != null ? currentUsername : "Гость");
                startActivity(intent);
            }
        });


        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                intent.putExtra("USER_NAME", currentUsername);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileUI();
    }

    private void updateProfileUI() {
        boolean isViewOnly = getIntent().getBooleanExtra("IS_VIEW_ONLY", false);

        if (currentUsername != null) {
            tvUsername.setText(currentUsername);
            tvUserStatus.setText(dbHelper.getUserStatus(currentUsername));

            tvRating.setText(String.valueOf(dbHelper.getUserRating(currentUsername)));
            tvPublishedCount.setText(String.valueOf(dbHelper.getPublishedCount(currentUsername)));
            tvSolvedCount.setText("0");

            if (isViewOnly) {
                btnAuthGate.setVisibility(View.GONE);
                btnSolveTasks.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 2.0f;
                btnCreateTask.setLayoutParams(params);

                btnCreateTask.setText("Задачи пользователя");
                btnCreateTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                        intent.putExtra("FILTER_AUTHOR", currentUsername);
                        startActivity(intent);
                    }
                });

                findViewById(R.id.personal_history_block).setVisibility(View.GONE);

            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1.0f;
                params.setMarginEnd(6);
                btnCreateTask.setLayoutParams(params);

                btnAuthGate.setVisibility(View.VISIBLE);
                btnAuthGate.setText("Выйти");
                btnSolveTasks.setVisibility(View.VISIBLE);

                btnCreateTask.setText("Опубликовать\nзадачу");
                btnCreateTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                        intent.putExtra("USER_NAME", currentUsername);
                        startActivity(intent);
                    }
                });

                findViewById(R.id.personal_history_block).setVisibility(View.VISIBLE);
            }

        } else {
            tvUsername.setText("Гость");
            tvUserStatus.setText("Авторизуйтесь в системе");
            btnAuthGate.setText("Войти");
            btnAuthGate.setVisibility(View.VISIBLE);
            btnSolveTasks.setVisibility(View.VISIBLE);
            tvRating.setText("0");
            tvPublishedCount.setText("0");
            tvSolvedCount.setText("0");
        }

        int incomingSolutions = dbHelper.getIncomingSolutionsCount(currentUsername);
        if (!isViewOnly && incomingSolutions > 0) {
            btnViewSolutions.setVisibility(View.VISIBLE);
            btnViewSolutions.setText("Проверить присланные решения (" + incomingSolutions + ")");
        } else {
            btnViewSolutions.setVisibility(View.GONE);
        }

        if (isViewOnly) {
            findViewById(R.id.personal_history_block).setVisibility(View.GONE);
            btnSolveTasks.setVisibility(View.GONE);
        } else {
            findViewById(R.id.personal_history_block).setVisibility(View.VISIBLE);
            btnSolveTasks.setVisibility(View.VISIBLE);
        }



    }


}
