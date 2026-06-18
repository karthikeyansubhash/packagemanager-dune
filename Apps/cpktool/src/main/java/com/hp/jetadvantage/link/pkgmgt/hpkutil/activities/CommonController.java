package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.UtilsFX;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;

import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class CommonController extends Controller {

    @FXML
    protected MenuItem menuNewWeb;
    @FXML
    protected MenuItem menuNewDevice;
    @FXML
    protected MenuItem menuOpen;
    @FXML
    protected MenuItem menuInstall;
    @FXML
    protected MenuItem menuConfiguration;
    @FXML
    protected MenuItem menuIconList;
    @FXML
    protected MenuItem menuSettings;
    @FXML
    protected MenuItem menuAttestation;

    protected Stage stage;
    protected File currentFile;
    protected File currentInstallFile;
    protected File defaultConfigFile;
    protected HpkFile hpkFile;
    protected ActionType currentAction = ActionType.NEW_LINK_FOR_DEVICE;

    @FXML
    protected void handleNewWebMenuAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleNewDeviceMenuAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleOpenMenuAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleOpenInstallAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleConfigurationAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleGetIconListAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleAttestationAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    @FXML
    protected void handleExitMenuAction(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    protected void handleAboutMenuAction(ActionEvent actionEvent) {
        if (PlatformType.LinkForDevice.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            showMsg(State.INFORMATION,
                    Constants.MESSAGE.getString("msg_about_tool"),
                    Constants.MESSAGE.getString("msg_about_toolversion"),
                    null);
        } else {
            showMsg(State.INFORMATION,
                    Constants.MESSAGE.getString("msg_about_tool"),
                    Constants.MESSAGE.getString("msg_about_toolversion_web"),
                    null);
        }
    }

    @FXML
    protected void handleVersionMenuAction(ActionEvent actionEvent) {
        changeScreen(actionEvent);
    }

    protected void setMinScreenSize(int width, int height) {
        //stage.setMinWidth(width);
        //stage.setMinHeight(height);
        //stage.setWidth(width);
        //stage.setHeight(height);
    }

    protected void changeScreen(ActionEvent event) {
        ActionType actionType = null;
        if (event.getSource() == menuInstall) {
            actionType = ActionType.INSTALL;
        } else if (event.getSource() == menuConfiguration) {
            actionType = ActionType.CONFIGURATION;
        } else if (event.getSource() == menuNewDevice) {
            if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForDevice)) {
                actionType = ActionType.NEW_LINK_FOR_DEVICE;
            } else if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
                actionType = ActionType.NEW_LINK_FOR_WEB;
            }
        } else if (event.getSource() == menuOpen) {
            actionType = ActionType.OPEN;
        } else if (event.getSource() == menuIconList) {
            actionType = ActionType.ICONLIST;
        } else if (event.getSource() == menuSettings) {
            actionType = ActionType.SETTINGS;
        } else if (event.getSource() == menuAttestation) {
            actionType = ActionType.ATTESTATION;
        }
        changeScreen(actionType);
    }

    protected void changeScreen(ActionType actionType) {
        try {
            closeHpkFile();
            if (actionType != null) {
                UtilsFX.changeScreen(this.stage, getClass(), actionType);
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_installer_open"),
                    Constants.MESSAGE.getString("msg_open_error") + "\n" + e.getMessage());
        }
    }

    protected void closeHpkFile() {
        if (hpkFile != null) {
            try {
                hpkFile.close();
            } catch (IOException e) {
            }
        }
    }

    public void setStageForMenu(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
            menuConfiguration.setVisible(false);
            menuSettings.setVisible(false);
            menuAttestation.setVisible(false);
        }
    }
}
