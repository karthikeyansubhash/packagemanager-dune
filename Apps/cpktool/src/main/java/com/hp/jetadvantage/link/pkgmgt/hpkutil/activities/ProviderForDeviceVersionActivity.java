package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.deviceconfig.DeviceConfigurationService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceConfiguration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class ProviderForDeviceVersionActivity extends CommonController {

    @FXML
    private TextField textHost;
    @FXML
    private PasswordField textAdminPassword;
    @FXML
    private Label textStatus;
    @FXML
    private Button btnOK;
    @FXML
    private ProgressBar progressBar;

    private MainActivity mainWindow;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init() {
        textHost.textProperty().addListener(textChangeListener);
        textHost.setText(Constants.DEFAULT_HOST);
        textAdminPassword.setText(Constants.DEFAULT_USER_PASSWORD);
        if (Constants.DEFAULT_HOST.isEmpty()) {
            btnOK.setDisable(true);
        }
    }

    private void updateEnableState() {
        if (textHost.getText().isEmpty()) {
            btnOK.setDisable(true);
        } else {
            btnOK.setDisable(false);
        }
    }

    @FXML
    protected void handleGetVersion(ActionEvent actionEvent) {
        if (!textHost.getText().isEmpty()
                && Utils.isValidIP(textHost.getText())) {
            disableButtonObject(true);
            textStatus.setText("");
            Constants.DEFAULT_HOST = textHost.getText();
            Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();

            DeviceConfigurationService deviceConfigurationService = new DeviceConfigurationService(taskInterface);
            deviceConfigurationService.setHost(textHost.getText());
            deviceConfigurationService.setAccount(Constants.DEFAULT_USER_NAME, textAdminPassword.getText());
            deviceConfigurationService.start();
        } else {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_install_failed"),
                    Constants.MESSAGE.getString("msg_input_error"));
        }
    }

    @FXML
    protected void handleCancel(ActionEvent actionEvent) {
        close(btnOK);
    }

    private void disableButtonObject(final boolean enable) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnOK.setDisable(enable);
                progressBar.setVisible(enable);
            }
        });
    }

    private void resultDeviceConfiguration(final DeviceConfiguration deviceConfiguration) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainWindow.setDeviceVersion(Constants.DEFAULT_HOST, deviceConfiguration.getLinkPlatformVersion());
                close(btnOK);
            }
        });
    }

    private void updateTextLabel(final String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (message != null) {
                    textStatus.setText(message);
                }
            }
        });
    }

    ChangeListener textChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            updateEnableState();
        }
    };

    private TaskInterface taskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            updateTextLabel(taskStatus.getCause());
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            disableButtonObject(false);
            resultDeviceConfiguration((DeviceConfiguration) obj);
        }

        @Override
        public void onFailed(Exception e) {
            disableButtonObject(false);
            textStatus.setText(e.getMessage());
        }
    };
}
