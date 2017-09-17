package com.boxxit.boxxit.library.invite;

import android.app.Activity;
import android.content.Context;

import com.boxxit.boxxit.library.base.Request;

public class InviteRequest implements Request {

    private Activity context;

    public InviteRequest (Activity context) {
        this.context = context;
    }

    public String getInviteUrl () {
        return "https://fb.me/208626762984319";
    }

    public String getPreviewUrl () {
        return "https://boxxit-3231.nodechef.com/fbpreviewimg.png";
    }

    public Activity getContext () {
        return context;
    }
}
