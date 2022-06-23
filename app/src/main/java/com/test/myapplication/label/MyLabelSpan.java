package com.test.myapplication.label;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

/**
 * Copyright (C), 2021-2099
 *
 * @author Cooper
 * History:
 * author - date - version - desc
 * Cooper 2022/6/15 09:08 1  可拖动的span对象，支持圆角、边框、margin、padding
 */
public class MyLabelSpan extends ReplacementSpan implements Parcelable {

    private static final String TAG = "MyLabelSpan tag";

    private final String labelTitle;       //显示文本，不可修改。 the title for show, not allow to edit.
    private final String labelValue;       //文本内容，不可见的内容。 the value that the tag carries.
    private final int borderRadius;
    private final int borderWidth;
    private final int borderColor;
    private final int borderMargin; //only for left and right
    private final int borderPadding; //only for left and right
    private final int textColor;

    private float textSize = 0;

    private int mSize;
    private final RectF mRectF = new RectF();

    private int spanStart = 0; //拖动时用来记录位置的
    private int spanEnd = 0;   //拖动时用来记录位置的
    private boolean spanDeletedTemp = false; //临时删除了

    public MyLabelSpan(
            String labelShowTitle, //显示文本，不可修改。 the title for show, not allow to edit.
            String labelValue,     //文本内容，不可见的内容。 the value that the tag carries.
            int borderRadius,
            int borderWidth,
            int borderColor,
            int borderMargin,
            int borderPadding,
            int textColor) {
        super();
        this.labelTitle = labelShowTitle;
        this.labelValue = labelValue;
        this.borderRadius = borderRadius;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.borderMargin = borderMargin;
        this.borderPadding = borderPadding;
        this.textColor = textColor;
    }

    public SpannableString getSpannableString() {
        SpannableString ss = new SpannableString(labelTitle);
        ss.setSpan(this, 0, labelTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public String getLabelTitle() {
        return labelTitle;
    }

    public String getLabelValue() {
        return labelValue;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public int getSpanStart() {
        return spanStart;
    }

    public void setSpanStart(int spanStart) {
        this.spanStart = spanStart;
    }

    public int getSpanEnd() {
        return spanEnd;
    }

    public void setSpanEnd(int spanEnd) {
        this.spanEnd = spanEnd;
    }

    public boolean isSpanDeletedTemp() {
        return spanDeletedTemp;
    }

    public void setSpanDeletedTemp(boolean spanDeletedTemp) {
        this.spanDeletedTemp = spanDeletedTemp;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

    public MyLabelSpan copy() {
        return new MyLabelSpan(
                labelTitle,
                labelValue,
                borderRadius,
                borderWidth,
                borderColor,
                borderMargin,
                borderPadding,
                textColor
        );
    }

    public RectF getRectF() {
        return mRectF;
    }

    public RectF getRectFByOffset(int paddingLeft, int paddingTop) {
        RectF result = new RectF(mRectF);
        result.offset(paddingLeft, paddingTop);
        return result;
    }

    @Override
    public int getSize(@NonNull Paint paint,
                       CharSequence text,
                       int start,
                       int end,
                       Paint.FontMetricsInt fm) {

        CharSequence before = text.subSequence(0, start);
        CharSequence after = text.subSequence(end, text.length());
        CharSequence content = before + labelTitle + after;

        mSize = (int) (paint.measureText(content, start, start + labelTitle.length()));
        return mSize + borderPadding * 2 + borderMargin * 2;
    }

    @Override
    public void draw(@NonNull Canvas canvas,
                     CharSequence text,
                     int start,
                     int end,
                     float x,
                     int top,
                     int y, // baseline
                     int bottom,
                     @NonNull Paint paint) {
        int defaultColor = paint.getColor();
        float defaultStrokeWidth = paint.getStrokeWidth();
        textSize = paint.getTextSize();

        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);

//        if (BuildConfig.DEBUG) {
//            //random bg for debug
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//            Random random = new Random();
//            int ranColor = 0xff000000 | random.nextInt(0x00ffffff);
//            paint.setColor(ranColor);
//        }

        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);
        float borderStartX = x + borderMargin;
        //seems not right, but it's really in the center.
        mRectF.set(borderStartX, y + paint.ascent(), borderStartX + mSize + borderPadding * 2, y + paint.descent());
        canvas.drawRoundRect(mRectF, borderRadius, borderRadius, paint);

        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(defaultStrokeWidth);

        CharSequence before = text.subSequence(0, start);
        CharSequence after = text.subSequence(end, text.length());
        CharSequence content = before + labelTitle + after;

        canvas.drawText(content, start, start + labelTitle.length(), borderStartX + borderPadding, y, paint);

        paint.setColor(defaultColor);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(labelTitle);
        dest.writeString(labelValue);
        dest.writeInt(borderRadius);
        dest.writeInt(borderWidth);
        dest.writeInt(borderColor);
        dest.writeInt(borderMargin);
        dest.writeInt(borderPadding);
        dest.writeInt(textColor);
    }

    MyLabelSpan(Parcel source) {
        labelTitle = source.readString();
        labelValue = source.readString();
        borderRadius = source.readInt();
        borderWidth = source.readInt();
        borderColor = source.readInt();
        borderMargin = source.readInt();
        borderPadding = source.readInt();
        textColor = source.readInt();
    }

    private static final Creator<MyLabelSpan> CREATOR = new Creator<MyLabelSpan>() {
        @Override
        public MyLabelSpan createFromParcel(Parcel source) {
            return new MyLabelSpan(source);
        }

        @Override
        public MyLabelSpan[] newArray(int size) {
            return new MyLabelSpan[0];
        }
    };
}