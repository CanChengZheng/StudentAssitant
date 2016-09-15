package com.cc.studentassistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cc.studentassistant.R;



import java.util.List;

/**
 * Created by CC on 2016/8/20.
 */
public class ScoreAdapter extends ArrayAdapter<StudentScore> {
    private int resourceId;

    public ScoreAdapter(Context context, int textViewResourceId, List<StudentScore> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StudentScore studentScore = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView tv_subject = (TextView) view.findViewById(R.id.tv_scoreItem_subject);
        TextView tv_type = (TextView) view.findViewById(R.id.tv_scoreItem_type);
        TextView tv_credit = (TextView) view.findViewById(R.id.tv_scoreItem_credit);
        TextView tv_score = (TextView) view.findViewById(R.id.tv_scoreItem_score);

        tv_subject.setText(studentScore.getSubject());
        tv_type.setText(studentScore.getType());
        tv_credit.setText(studentScore.getCredit());
        tv_score.setText(studentScore.getScore());

        return view;
    }
}



