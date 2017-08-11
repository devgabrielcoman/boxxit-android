package com.boxxit.boxxit.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boxxit.boxxit.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorView extends RelativeLayout {

    @BindView(R.id.ErrorText) public TextView errorText;
    @BindView(R.id.RetryButton) public Button retry;

    public ErrorView(Context context) {
        this(context, null, 0);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_error, this);
        ButterKnife.bind(this);
    }
}
