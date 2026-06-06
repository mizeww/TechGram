package com.example.techgram;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;

    private final String currentViewer;

    public TaskAdapter(List<Task> taskList, String currentViewer) {
        this.taskList = taskList;
        this.currentViewer = currentViewer;
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvDiscipline.setText(task.getDiscipline());
        holder.tvTimestamp.setText(task.getTimestamp());
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TaskDetailActivity.class);
                intent.putExtra("TASK_ID", task.getId());
                intent.putExtra("TASK_TITLE", task.getTitle());
                intent.putExtra("TASK_DESC", task.getDescription());
                intent.putExtra("TASK_DISCIPLINE", task.getDiscipline());
                intent.putExtra("TASK_TIME", task.getTimestamp());
                intent.putExtra("TASK_AUTHOR", task.getAuthor());
                intent.putExtra("TASK_CRITERIA", task.getCriteria());
                intent.putExtra("CURRENT_VIEWER", currentViewer);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiscipline, tvTimestamp, tvTitle, tvDescription;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscipline = itemView.findViewById(R.id.item_tv_discipline);
            tvTimestamp = itemView.findViewById(R.id.item_tv_timestamp);
            tvTitle = itemView.findViewById(R.id.item_tv_title);
            tvDescription = itemView.findViewById(R.id.item_tv_description);
        }
    }
}
