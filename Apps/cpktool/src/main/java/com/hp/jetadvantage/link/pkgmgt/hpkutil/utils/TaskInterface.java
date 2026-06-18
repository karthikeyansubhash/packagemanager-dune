package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

public interface TaskInterface {
    String updateMessage(TaskStatus msg);
    void onSucceed(Object obj);
    void onFailed(Exception e);
}
