package com.boxxit.boxxit.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.boxxit.boxxit.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InviteView extends RelativeLayout {

    @BindView(R.id.InviteBtn) public Button invite;

    public InviteView(Context context) {
        this(context, null, 0);
    }

    public InviteView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InviteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_invite, this);
        ButterKnife.bind(this);
    }
}
