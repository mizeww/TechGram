package com.example.techgram;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmptyPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rv_tasks_feed);
        tvEmptyPlaceholder = findViewById(R.id.tv_empty_feed_placeholder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPref = getSharedPreferences("TechGramPrefs", Context.MODE_PRIVATE);
        String currentViewer = sharedPref.getString("CURRENT_USER", "Гость");

        String filterAuthor = getIntent().getStringExtra("FILTER_AUTHOR");
        List<Task> taskList;

        if (filterAuthor != null) {
            setTitle("Публикации: " + filterAuthor);

            taskList = dbHelper.getTasksByAuthor(filterAuthor);

            if (taskList.isEmpty()) {
                tvEmptyPlaceholder.setText("У вас еще нет опубликованных задач");
                tvEmptyPlaceholder.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyPlaceholder.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            setTitle("Лента актуальных задач");
            taskList = dbHelper.getAllTasks();

            if (taskList.isEmpty()) {
                dbHelper.addTask("Расчет балки", "Условие задачи по теоретической механике...", "Теормех", "10 мин назад", "Администратор", "Проверить знаки", null);
                taskList = dbHelper.getAllTasks();
            }


            tvEmptyPlaceholder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new TaskAdapter(taskList, currentViewer);
        recyclerView.setAdapter(adapter);
    }
}
