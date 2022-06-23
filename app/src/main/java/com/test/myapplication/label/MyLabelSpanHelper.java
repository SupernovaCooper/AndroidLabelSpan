package com.test.myapplication.label;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2021-2099
 *
 * @author Cooper
 * History:
 * author - date - version - desc
 * Cooper 2022/6/20 11:40 1  EditText中可拖动的span助手类
 */
public class MyLabelSpanHelper {
    private static final String TAG = MyLabelSpanHelper.class.getSimpleName();

    /**
     * set the main editText. Note, editText need inside frameLayout etc, otherwise it will relayout.
     * 设置主要的编辑框
     * 注意：编辑框需要用FrameLayout等包裹一下，否则动态插入view的时候可能乱跳。
     *
     * @param editText main edit text
     * @return this
     */
    public MyLabelSpanHelper setMainEditText(EditText editText) {
        mainEditText = editText;
        initMainEditText();
        return this;
    }

    private EditText mainEditText;

    private void initMainEditText() {
        if (mainEditText != null) {
            mainEditText.setSingleLine(false);

            disableEditTextClipBoard(mainEditText);

            setTouchToDragForEditText((ViewGroup) mainEditText.getParent(), mainEditText, mainEditText);

            mainEditText.setOnDragListener(mOnDragListener);
        }
    }

    //region data source , uneditable edittext, the label container.

    /**
     * set the edit to hold all the labels. Note, editText need inside frameLayout etc, otherwise it will relayout.
     * 设置存放全部标签的编辑框
     * 注意：编辑框需要用FrameLayout等包裹一下，否则动态插入view的时候可能乱跳。
     *
     * @param editText container
     * @return this
     */
    public MyLabelSpanHelper setLabelProviderEditText(EditText editText) {
        labelProviderEditText = editText;
        initLabelProviderEditText();
        return this;
    }

    private EditText labelProviderEditText;

    private void initLabelProviderEditText() {
        if (labelProviderEditText != null) {
            // forbid edit for the provider.
            labelProviderEditText.setInputType(InputType.TYPE_NULL);
            labelProviderEditText.setSingleLine(false);

            setLabels(labels);
            // same as the editor
            setTouchToDragForEditText((ViewGroup) labelProviderEditText.getParent(), labelProviderEditText, null);
        }
    }

    private List<MyLabelSpan> labels = new ArrayList<>();

    /**
     * set all the labels
     * 设置标签
     *
     * @param labels all the labels
     * @return this
     */
    public MyLabelSpanHelper setLabels(List<MyLabelSpan> labels) {
        this.labels = labels;
        if (labelProviderEditText != null) {
            labelProviderEditText.setText("");
            for (MyLabelSpan label : this.labels) {
                labelProviderEditText.append(label.getSpannableString());
                labelProviderEditText.append(" ");
            }
        }
        return this;
    }

    /**
     * get the content
     * 获取最终结果
     *
     * @param replaceByLabelValue replace the labels by the label value or title 使用value替换标签还是使用title替换标签
     * @return result string only. no span.
     */
    public String getFinalContentString(boolean replaceByLabelValue) {
        Editable editable = mainEditText.getText();

        SpannableStringBuilder builder = new SpannableStringBuilder(editable);
        int index = 0;
        while (index < builder.length()) {
            int next = builder.nextSpanTransition(index, builder.length(), MyLabelSpan.class);
            MyLabelSpan[] spans = builder.getSpans(index, next, MyLabelSpan.class);
            if (spans.length > 0) {
                MyLabelSpan span = spans[0];
                builder.delete(index, builder.getSpanEnd(span));
                int length;
                if (replaceByLabelValue) {
                    builder.insert(index, span.getLabelValue());
                    length = span.getLabelValue().length();
                } else {
                    builder.insert(index, span.getLabelTitle());
                    length = span.getLabelTitle().length();
                }
                index = index + length;
            } else {
                index++;
            }
        }

        return builder.toString();
    }

    /**
     * get the text with span. Replace the text by the labels.
     * 获取带span的文本，使用标签替换。
     *
     * @param text                target text
     * @param replaceByLabelValue replace the labels by the label value or title
     * @return result spannable string.
     */
    public SpannableStringBuilder getSpannableStringByReplaceLabels(String text, boolean replaceByLabelValue) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        String s = spannableStringBuilder.toString();

        for (MyLabelSpan label : labels) {
            String targetString;

            if (replaceByLabelValue) {
                targetString = label.getLabelValue();
            } else {
                targetString = label.getLabelTitle();
            }

            int index = 0;
            while (index < s.length()) {
                String sAfter = s.substring(index);
                if (sAfter.contains(targetString)) {
                    int start = sAfter.indexOf(targetString) + index;
                    int length = targetString.length();
                    int end = start + length;

                    spannableStringBuilder = spannableStringBuilder.delete(start, end);
                    // if use the same object, there will be problem when touch it!
                    spannableStringBuilder = spannableStringBuilder.insert(start, label.copy().getSpannableString());

                    s = spannableStringBuilder.toString();

                    // because the title maybe not same length as the value.
                    index = start + label.getLabelTitle().length();
                } else {
                    break;
                }
            }
        }
        return spannableStringBuilder;
    }

    /**
     * get the text with span. Replace the text by the labels.
     * 获取带span的文本，使用标签替换。
     *
     * @param replaceByLabelValue replace the labels by the label value or title
     * @return result spannable string.
     */
    public SpannableStringBuilder getSpannableStringByReplaceLabels(boolean replaceByLabelValue) {
        if (mainEditText != null) {
            return getSpannableStringByReplaceLabels(mainEditText.getText().toString(), replaceByLabelValue);
        }
        return new SpannableStringBuilder();
    }

    /**
     * replace labels and show final result. designed for set the text with scape character and convert into labels.
     * 替换标签并显示最终内容，设计用于把带转义字符的结果显示成可视化的标签。
     *
     * @param replaceByLabelValue replace the labels by the label value or title
     * @return this
     */
    public MyLabelSpanHelper showTextByReplaceLabels(boolean replaceByLabelValue) {
        if (mainEditText != null) {
            mainEditText.post(() -> mainEditText.setText(getSpannableStringByReplaceLabels(replaceByLabelValue)));
        }
        return this;
    }

    /**
     * replace labels and show final result. designed for set the text with scape character and convert into labels.
     * 替换标签并显示最终内容，设计用于把带转义字符的结果显示成可视化的标签。
     *
     * @param text                text to handle.
     * @param replaceByLabelValue replace the labels by the label value or title
     * @return this
     */
    public MyLabelSpanHelper showTextByReplaceLabels(String text, boolean replaceByLabelValue) {
        if (mainEditText != null) {
            mainEditText.post(() -> mainEditText.setText(getSpannableStringByReplaceLabels(text, replaceByLabelValue)));
        }
        return this;
    }
    //endregion

    //region touch to drag
    @SuppressLint("ClickableViewAccessibility")
    private static void setTouchToDragForEditText(ViewGroup viewGroup, EditText editText, EditText etForRemoveSpan) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Editable text = ((EditText) v).getText();
                if (text != null) {
                    MyLabelSpan[] spans = text.getSpans(0, text.length(), MyLabelSpan.class);
                    for (MyLabelSpan single : spans) {
                        if (single.getRectFByOffset(v.getPaddingLeft(), v.getPaddingTop()).contains(event.getX(), event.getY())) {
                            single.setSpanStart(text.getSpanStart(single));
                            single.setSpanEnd(text.getSpanEnd(single));
                            onTouchSpan(viewGroup, single, event.getX(), event.getY(), etForRemoveSpan);
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    //cache the span that we are dragging. used for restore it if nothing dropped.
    private static MyLabelSpan draggingSpan = null;

    /**
     * when we touch the span dynamic generation a textview just used for the drag shadow.
     *
     * @param viewGroup             to add a invisible textview, used for generate the shadow.
     * @param span                  the target span that we touched
     * @param x                     x
     * @param y                     y
     * @param editTextForRemoveSpan if it is the main edittext, then we need to remove the span if we really moved the finger (dragged the span) .
     */
    private static void onTouchSpan(ViewGroup viewGroup, MyLabelSpan span, float x, float y, @Nullable EditText editTextForRemoveSpan) {
        try {
            final TextView maskTextView = new TextView(viewGroup.getContext());
            maskTextView.setVisibility(View.INVISIBLE);
            viewGroup.addView(maskTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            maskTextView.setText(span.getLabelTitle());
            maskTextView.setTextColor(span.getTextColor());
            maskTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, span.getTextSize());
            maskTextView.post(() -> {
                try {
                    maskTextView.setX(x);
                    maskTextView.setY(y);
                    ViewCompat.startDragAndDrop(maskTextView, null, new View.DragShadowBuilder(maskTextView), span.copy(), 0);

                    // 3 steps to do: 1 cache the span; 2 delete the span if moved; 3 if dropped nothing, then restore the span;
                    if (editTextForRemoveSpan != null) {
                        draggingSpan = span;
                    } else {
                        draggingSpan = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        viewGroup.removeView(maskTextView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // listener for receive drop span and change cursor and delete span.
    private final View.OnDragListener mOnDragListener = new MyDragListener();

    // handle the drag event.
    private class MyDragListener implements View.OnDragListener {
        boolean isCanAcceptDropEvent = false;     //must greater than touch slop.
        boolean isAnySpanDroppedToInsert = false; //mark if inserted already.
        float dragStartX = 0;
        float dragStartY = 0;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (mainEditText != null) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        isCanAcceptDropEvent = false;
                        isAnySpanDroppedToInsert = false;
                        dragStartX = event.getX();
                        dragStartY = event.getY();

                        if (draggingSpan != null) {
                            draggingSpan.setSpanDeletedTemp(false);
                        }

                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        // handle the cursor
                        showCursorByPosition(mainEditText, event.getX(), event.getY());

                        // remove the span if really moved (equal or greater than touch slop).
                        // if we drug VERY FAST, then we can not receive this event and ACTION_DRAG_EXITED, so the delete won't work, that's the problem of the system.
                        int touchSlop = ViewConfiguration.get(mainEditText.getContext()).getScaledTouchSlop();
                        if (Math.abs(dragStartX - event.getX()) >= touchSlop
                                || Math.abs(dragStartY - event.getY()) >= touchSlop) {

                            // set the can accept mark
                            isCanAcceptDropEvent = true;

                            // set the deleted mark
                            if (draggingSpan != null && !draggingSpan.isSpanDeletedTemp()) {
                                draggingSpan.setSpanDeletedTemp(true);
                                mainEditText.getText().delete(draggingSpan.getSpanStart(), draggingSpan.getSpanEnd());
                            }
                        }
                        break;
                    case DragEvent.ACTION_DROP:
                        if (isCanAcceptDropEvent) {
                            if (event.getLocalState() instanceof MyLabelSpan) {
                                MyLabelSpan span = (MyLabelSpan) event.getLocalState();
                                if (!span.getLabelTitle().isEmpty()) {
                                    mainEditText.getText().insert(mainEditText.getSelectionStart(), span.getSpannableString());
                                    isAnySpanDroppedToInsert = true;
                                }
                            }
                        }
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!isAnySpanDroppedToInsert && draggingSpan != null && isCanAcceptDropEvent) {
                            mainEditText.getText().insert(draggingSpan.getSpanStart(), draggingSpan.getSpannableString());
                        }
                        draggingSpan = null;
                        isCanAcceptDropEvent = false;
                        isAnySpanDroppedToInsert = false;
                        dragStartX = 0;
                        dragStartY = 0;
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:

                        // set the deleted mark, actually it's useless, because if we drug VERY FAST, we can not receive this event.
                        if (draggingSpan != null && !draggingSpan.isSpanDeletedTemp()) {
                            draggingSpan.setSpanDeletedTemp(true);
                            mainEditText.getText().delete(draggingSpan.getSpanStart(), draggingSpan.getSpanEnd());
                        }
                        break;
                }
            }
            return true;
        }
    }
    //endregion

    //region 工具类方法 tools

    // forbid clipboard , because sometime it will mess up the span
    private static void disableEditTextClipBoard(TextView textView) {
        if (textView instanceof EditText) {
            setInsertionDisabled((EditText) textView);
        }
        textView.setLongClickable(false);
        textView.setTextIsSelectable(false);
        textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // call that method
            textView.setCustomInsertionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
    }

    // forbid clipboard 2
    @SuppressLint("DiscouragedPrivateApi")
    private static void setInsertionDisabled(EditText editText) {
        try {
            Field editorField = TextView.class.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            Object editorObject = editorField.get(editText);

            // if this view supports insertion handles
            @SuppressLint("PrivateApi") Class<?> editorClass = Class.forName("android.widget.Editor");
            Field mInsertionControllerEnabledField = editorClass.getDeclaredField("mInsertionControllerEnabled");
            mInsertionControllerEnabledField.setAccessible(true);
            mInsertionControllerEnabledField.set(editorObject, false);

            // if this view supports selection handles
            Field mSelectionControllerEnabledField = editorClass.getDeclaredField("mSelectionControllerEnabled");
            mSelectionControllerEnabledField.setAccessible(true);
            mSelectionControllerEnabledField.set(editorObject, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // last pos, used for trigger vibrate
    private static int lastPos = -1;

    /**
     * 根据触摸位置显示光标
     *
     * @param x x坐标，相对于控件自身
     * @param y y坐标，相对于控件自身
     */
    private static void showCursorByPosition(EditText editText, float x, float y) {
        int pos = editText.getOffsetForPosition(x, y);
        if (pos != lastPos) {
            lastPos = pos;
            editText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        editText.setSelection(pos); //cursor position
        editText.requestFocus();    //show the cursor
    }
    //endregion

}
