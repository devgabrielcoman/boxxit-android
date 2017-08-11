package com.boxxit.boxxit.app.common;

public interface Presenter <T extends State> {

    T initialState();

}
