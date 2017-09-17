package com.boxxit.boxxit.library.invite;

import com.boxxit.boxxit.library.base.Task;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import rx.Single;

public class InviteTask implements Task <InviteRequest, Void, Single<Void>> {


    @Override
    public Single<Void> execute(InviteRequest request) {

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(request.getInviteUrl())
                    .setPreviewImageUrl(request.getPreviewUrl())
                    .build();
            AppInviteDialog.show(request.getContext(), content);
            return Single.just(null);
        } else {
            return Single.error(new Throwable());
        }
    }
}
