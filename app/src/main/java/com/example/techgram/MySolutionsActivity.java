package com.example.techgram;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MySolutionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setPadding(32, 32, 32, 32);
        rv.setClipToPadding(false);

        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("У вас еще нет отправленных решений");
        tvEmpty.setTextSize(16);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.gravity = Gravity.CENTER;
        tvEmpty.setLayoutParams(textParams);
        tvEmpty.setVisibility(View.GONE);

        rootLayout.addView(rv);
        rootLayout.addView(tvEmpty);
        setContentView(rootLayout);
        setTitle("Мои отправленные решения");

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String username = getIntent().getStringExtra("USER_NAME");

        if (username == null) {
            username = "Гость";
        }

        List<Solution> list = dbHelper.getSolutionsBySender(username);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new MySolutionsAdapter(list));
        }
    }

    private class MySolutionsAdapter extends RecyclerView.Adapter<MySolutionsAdapter.ViewHolder> {
        private final List<Solution> list;
        public MySolutionsAdapter(List<Solution> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_my_solution, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Solution s = list.get(pos);
            h.title.setText(s.getTaskTitle());
            MathRenderer.render(h.text, s.getText());

            final android.widget.ImageView ivMyPhoto = h.itemView.findViewById(R.id.my_sol_iv_image);
            if (s.getImageBlob() != null && s.getImageBlob().length > 0) {
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(s.getImageBlob(), 0, s.getImageBlob().length);
                ivMyPhoto.setImageBitmap(bitmap);
                ivMyPhoto.setVisibility(android.view.View.VISIBLE);

                ivMyPhoto.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        android.widget.ImageView fullscreenImageView = new android.widget.ImageView(MySolutionsActivity.this);
                        fullscreenImageView.setImageBitmap(bitmap);
                        fullscreenImageView.setAdjustViewBounds(true);
                        fullscreenImageView.setPadding(16, 16, 16, 16);

                        new androidx.appcompat.app.AlertDialog.Builder(MySolutionsActivity.this)
                                .setTitle("Ваш отправленный чертеж")
                                .setView(fullscreenImageView)
                                .setPositiveButton("Закрыть", null)
                                .show();
                    }
                });
            } else {
                ivMyPhoto.setVisibility(android.view.View.GONE);
            }



            if (s.getScore() == -1) {
                h.status.setText("На проверке у автора");
                h.status.setTextColor(0xFF856404);
            } else {
                h.status.setText("Оценено на: " + s.getScore() + " / 100 баллов");
                h.status.setTextColor(0xFF155724);
            }
        }


        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, status;
            com.judemanutd.katexview.KatexView text;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.my_sol_tv_title);
                status = itemView.findViewById(R.id.my_sol_tv_status);

                text = itemView.findViewById(R.id.my_sol_tv_text);
            }
        }

    }
}
