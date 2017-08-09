package com.boxxit.boxxit.library.parse.models.facebook;

public class Paging {
    public Cursor cursors = new Cursor();
    public String next;
    public String previous;

    public String offsetAfter () {
        return next != null ? cursors.after : null;
    }

    public String offsetBefore () {
        return previous != null ? cursors.before : null;
    }
}
