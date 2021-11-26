package com.example.weatherapp;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class PmData {
    String[] pmStrs = new String[2];
    boolean[] isUnitHidden = new boolean[2];
    int[] pmGrade = new int[2];

    void setFields(ArpltnResult.Item item, int pmIdx) {
        String[] pmValues;
        if (pmIdx == 0)
            pmValues = new String[] {item.pm10Value, item.pm10Grade1h, item.pm10Value24, item.pm10Grade, item.pm10Flag};
        else
            pmValues = new String[]{item.pm25Value, item.pm25Grade1h, item.pm25Value24, item.pm25Grade, item.pm25Flag};

        if (!pmValues[0].equals("-")) {
            this.pmStrs[pmIdx] = pmValues[0];
            this.pmGrade[pmIdx] = Integer.parseInt(pmValues[1]);
        } else if (!pmValues[2].equals("-")) {
            this.pmStrs[pmIdx] = pmValues[2];
            this.pmGrade[pmIdx] = Integer.parseInt(pmValues[3]);
        } else {
            this.pmStrs[pmIdx] = pmValues[4];
            this.isUnitHidden[pmIdx] = true;
        }
    }

    public void updateView(MainActivity mainActivity) {
        updatePMView(mainActivity, R.id.pm10_text_view, R.id.pm10_unit_view, 0);
        updatePMView(mainActivity, R.id.pm25_text_view, R.id.pm25_unit_view, 1);
    }
    private void updatePMView(MainActivity mainActivity, int pmTextViewId, int pmUnitViewId, int pmInfoIdx) {
        TextView pmTextView = mainActivity.findViewById(pmTextViewId);
        TextView pmUnitView = mainActivity.findViewById(pmUnitViewId);

        String pm10Text = this.pmStrs[pmInfoIdx];
        if (this.isUnitHidden[pmInfoIdx]) {
            pmUnitView.setVisibility(View.INVISIBLE);
        } else {
            int pmColor = R.color.purple_700;
            switch (this.pmGrade[pmInfoIdx]) {
                case 1: pmColor = Color.BLUE;break;
                case 2: pmColor = Color.GREEN;break;
                case 3: pmColor = Color.parseColor("#ff7300");break;
                case 4: pmColor = Color.RED;break;
            }
            pmTextView.setTextColor(pmColor);
            pmUnitView.setTextColor(pmColor);
        }
        pmTextView.setText(pm10Text);
    }
}
