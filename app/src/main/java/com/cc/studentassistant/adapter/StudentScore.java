package com.cc.studentassistant.adapter;

/**
 * Created by CC on 2016/8/20.
 */
public class StudentScore {
    private String subject;
    private String score;
    private String credit;
    private String type;

    // 科目，类型，学分，分数
    public StudentScore(String subject, String type, String credit, String score) {
        this.subject = subject;
        this.type = type;
        this.credit = credit + "学分";
        this.score = score;
    }

    public String getSubject(){
        return subject;
    }

    public String getType(){
        return type;
    }

    public String getCredit(){
        return credit;
    }
    public String getScore() {
        return score;
    }
}