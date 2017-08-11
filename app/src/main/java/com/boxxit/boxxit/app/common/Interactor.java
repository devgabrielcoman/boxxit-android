package com.boxxit.boxxit.app.common;

import com.boxxit.boxxit.app.events.UIEvent;

import rx.Observable;

public interface Interactor {

    Observable<UIEvent> getUIEvents ();

}
