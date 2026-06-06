package com.example.techgram;

import com.judemanutd.katexview.KatexView;

public class MathRenderer {
    public static void render(KatexView katexView, String textWithLatex) {
        if (katexView == null || textWithLatex == null) return;

        katexView.setText(textWithLatex);
    }
}
