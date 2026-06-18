package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.InstallServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.UninstallServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.HPKVersion;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class InstallActivity extends CommonController {

    @FXML
    private TextField textInstallerFile;
    @FXML
    private TextField textInstallerUuid;
    @FXML
    private TextField textHost;
    @FXML
    private PasswordField textAdminPassword;
    @FXML
    private Button btnInstall;
    @FXML
    private CheckBox checkAsInstallForce;
    @FXML
    private Button btnUninstall;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label textStatus;

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView(currentAction);
    }

    private void initView(ActionType actionType) {
        switch (actionType) {
            case INSTALL:
            case UNINSTALL:
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_INSTALL_MIN_HEIGHT);
                textInstallerUuid.setText(Constants.DEFAULT_UUID);
                textHost.setText(Constants.DEFAULT_HOST);
                textAdminPassword.setText(Constants.DEFAULT_USER_PASSWORD);
                textHost.setText(Constants.DEFAULT_HOST);
                currentFile = Constants.DEFAULT_HPK;
                if (currentFile != null) {
                    loadHpk();
                }
                break;
        }
    }

    @FXML
    protected void handleInstallerBrowseAction(ActionEvent actionEvent) {
        File selectedHpkFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
        if (selectedHpkFile != null) {
            if (isValidHPKFile(selectedHpkFile)) {
                currentFile = selectedHpkFile;
            }
        }
        loadHpk();
    }

    private void loadHpk() {
        if (currentFile != null) {
            try {
                if (!Utils.checkSupportFormat(currentFile).equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    throw new Exception(Constants.MESSAGE.getString("error_not_support_format"));
                }

                hpkFile = new HpkFile(currentFile);

                if (PlatformType.LinkForWeb.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    Utils.getButtonInfo(hpkFile.getInstallFile(), getClass());
                }

                Connector connector = hpkFile.getConnector();
                textInstallerFile.setText(currentFile.getName());
                textInstallerUuid.setText(connector.getUuid().toString());
                Constants.DEFAULT_HPK = currentFile;
            } catch (Exception e) {
                showMsg(State.ERROR,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("menu_open_hpk_file"),
                        Constants.MESSAGE.getString("msg_failed_to_open") + " " + currentFile.getAbsolutePath() + "\n" + e.getMessage());

                currentFile = null;
                textInstallerFile.setText(null);
                textInstallerUuid.setText(null);
            } finally {
                try {
                    if (hpkFile != null) {
                        hpkFile.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    @FXML
    protected void handleInstall(ActionEvent actionEvent) {
        Constants.DEFAULT_DEVICE_MODE = DeviceMode.LINKFORWEB;
        executeInstall();
    }

    @FXML
    protected void handleUninstall(ActionEvent actionEvent) {
        Constants.DEFAULT_DEVICE_MODE = DeviceMode.LINKFORWEB;
        executeUninstall();
    }

    private void executeInstall() {
        if (currentFile != null
                && !textHost.getText().isEmpty()
                && Utils.isValidIP(textHost.getText())
                && !textInstallerUuid.getText().isEmpty()
                && Utils.isValidUuid(textInstallerUuid.getText())) {
            disableInstallObject(true);
            textStatus.setText("");
            currentAction = ActionType.INSTALL;
            Constants.DEFAULT_UUID = textInstallerUuid.getText();
            Constants.DEFAULT_HOST = textHost.getText();
            Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();

            // if current device is not support package manager, we try to install HPKOXPdInstaller
            InstallServiceGUI installServiceGUI = new InstallServiceGUI(taskInterface);
            installServiceGUI.setCurrentFile(currentFile);
            installServiceGUI.setHost(textHost.getText());
            installServiceGUI.setAccount(Constants.DEFAULT_USER_NAME, textAdminPassword.getText());
            installServiceGUI.setOptions(checkAsInstallForce.isSelected());
            installServiceGUI.start();
        } else {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_install_failed"),
                    Constants.MESSAGE.getString("msg_input_error"));
        }
    }

    private void executeUninstall() {
        if (!textHost.getText().isEmpty()
                && !textInstallerUuid.getText().isEmpty()
                && Utils.isValidUuid(textInstallerUuid.getText())) {
            disableInstallObject(true);
            textStatus.setText("");
            currentAction = ActionType.UNINSTALL;
            Constants.DEFAULT_HOST = textHost.getText();
            Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();
            Constants.DEFAULT_HOST = textHost.getText();

            // if current device is not support package manager, we try to uninstall HPKOXPdUninstaller
            UninstallServiceGUI uninstallServiceGUI = new UninstallServiceGUI(taskInterface);
            uninstallServiceGUI.setHost(textHost.getText());
            uninstallServiceGUI.setUuid(UUID.fromString(textInstallerUuid.getText()));
            uninstallServiceGUI.setAccount(Constants.DEFAULT_USER_NAME, textAdminPassword.getText());
            uninstallServiceGUI.start();
        } else {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_install_failed"),
                    Constants.MESSAGE.getString("msg_input_error"));
        }
    }

    private void setInstallLog(final TaskStatus taskStatus) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                if (taskStatus != null) {
                    if (taskStatus.getState().equals(PackageInstallerState.psCompleted)) {
                        if (currentAction.equals(ActionType.INSTALL)) {
                            msg = Constants.MESSAGE.getString("msg_install_success");
                        } else if (currentAction.equals(ActionType.UNINSTALL)) {
                            msg = Constants.MESSAGE.getString("msg_uninstall_success");
                        }
                        textStatus.setText(msg);
                        showMsg(State.INFORMATION,
                                Constants.MESSAGE.getString("msg_success"),
                                msg,
                                null);
                    } else if (taskStatus.getState().equals(PackageInstallerState.psFailed)) {
                        if (currentAction.equals(ActionType.INSTALL)) {
                            msg = Constants.MESSAGE.getString("msg_install_failed");
                        } else if (currentAction.equals(ActionType.UNINSTALL)) {
                            msg = Constants.MESSAGE.getString("msg_uninstall_failed");
                        }
                        // if current mode is LinkForWeb and not found request url case
                        // keep going to install/uninstall using HPKOXPdInstaller/HPKOXPdUninstaller
                        if (PlatformType.LinkForWeb.equals(Constants.DEFAULT_PLATFORM_TYPE)
                                && DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)
                                && Constants.MESSAGE.getString("link_platform_is_not_enabled").equals(taskStatus.getCause())) {
                            showYesOrNoDialog(State.WARNING,
                                    Constants.MESSAGE.getString("msg_warning"),
                                    Constants.MESSAGE.getString("link_platform_is_not_enabled"),
                                    Constants.MESSAGE.getString("continue_unsecured_channel"),
                                    keepGoingClickListener
                            );
                        } else {
                            textStatus.setText(msg);
                            showMsg(State.ERROR,
                                    Constants.MESSAGE.getString("msg_failed"),
                                    msg,
                                    taskStatus.getCause());
                        }
                    } else if (taskStatus.getState().equals(PackageInstallerState.psInProgress)) {
                        if (currentAction == ActionType.INSTALL) {
                            textStatus.setText(Constants.MESSAGE.getString("msg_installing"));
                        } else {
                            textStatus.setText(Constants.MESSAGE.getString("msg_uninstalling"));
                        }
                    } else if (taskStatus.getState().equals(PackageInstallerState.psSending)) {
                        String percent = taskStatus.getCause() == null ? "" : " (" + taskStatus.getCause() + ")";
                        textStatus.setText(Constants.MESSAGE.getString("msg_sending") + percent);
                    }
                }
            }
        });
    }

    ButtonClickListener keepGoingClickListener = new ButtonClickListener() {
        @Override
        public void ok() {
            if (currentAction == ActionType.INSTALL) {
                executeInstall();
            } else {
                executeUninstall();
            }
        }

        @Override
        public void cancel() {
            textStatus.setText(Constants.MESSAGE.getString("msg_canceled"));
        }
    };

    private TaskInterface taskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            setInstallLog(taskStatus);
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            disableInstallObject(false);
        }

        @Override
        public void onFailed(Exception e) {
            disableInstallObject(false);
        }
    };

    private void disableInstallObject(final boolean enable) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnInstall.setDisable(enable);
                btnUninstall.setDisable(enable);
                progressBar.setVisible(enable);
            }
        });
    }

    private boolean isValidHPKFile(File file) {
        HpkFile hpkFile = null;
        try {
            hpkFile = new HpkFile(file);
            String schema = hpkFile.getConnector().getSchemaLocation();
            String version = schema.substring(schema.lastIndexOf("/") + 1, schema.lastIndexOf(" "));
            java.net.URL xsdFile;
            if (HPKVersion.HPK_1_0.toString().equals(version)) {
                xsdFile = getClass().getResource(HPKVersion.HPK_1_0.getXsdPath());
            } else if (HPKVersion.HPK_1_1.toString().equals(version)) {
                xsdFile = getClass().getResource(HPKVersion.HPK_1_1.getXsdPath());
            } else if (HPKVersion.HPK_1_2.toString().equals(version)) {
                xsdFile = getClass().getResource(HPKVersion.HPK_1_2.getXsdPath());
            } else if (HPKVersion.HPK_1_3.toString().equals(version)) {
                xsdFile = getClass().getResource(HPKVersion.HPK_1_3.getXsdPath());
            } else if (HPKVersion.HPK_1_4.toString().equals(version)) {
                xsdFile = getClass().getResource(HPKVersion.HPK_1_4.getXsdPath());
            } else {
                throw new Exception("HPK version is invalid");
            }

            String data = Utils.convertStreamToString(hpkFile.getXmlStream());
            Utils.isVerifyFormat(data, xsdFile);
            return true;
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("menu_open_hpk_file"),
                    Constants.MESSAGE.getString("msg_failed_to_open") + " " + file.getAbsolutePath() + "\n" + e.getMessage());
            return false;
        } finally {
            try {
                if (hpkFile != null) {
                    hpkFile.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
