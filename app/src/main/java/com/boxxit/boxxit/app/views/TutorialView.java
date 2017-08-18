package com.boxxit.boxxit.app.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boxxit.boxxit.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TutorialView extends RelativeLayout {

    public @Nullable @BindView(R.id.ProfilePicture) ImageView profilePicture;
    public @BindView(R.id.TutorialText) TextView tutorialText;

    public TutorialView(Context context) {
        this(context, null, 0);
    }

    public TutorialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bind (int layout) {
        inflate(getContext(), layout, this);
        ButterKnife.bind(this);
    }
}