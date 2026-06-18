package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller;

public class DuneInstallStats {
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_FAIL = -1;
    public static final int STATE_PROGRESS = 1;

    private int state;
    private String message;

    public DuneInstallStats(int state, String message) {
        this.state = state;
        this.message = message;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

