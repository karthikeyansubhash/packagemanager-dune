package com.hp.jetadvantage.link.pkgmgt.hpkutil;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.UtilsFX;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFxApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
            UtilsFX.changeScreen(primaryStage, getClass(), ActionType.NEW_LINK_FOR_WEB);
        } else {
            Constants.setHPKVersion(Constants.HPK_LATEST_VERSION);
            Constants.setPlatformVersion(Constants.LATEST_PLATFORM_VERSION);
            UtilsFX.changeScreen(primaryStage, getClass(), ActionType.NEW_LINK_FOR_DEVICE);
        }
    }

}
