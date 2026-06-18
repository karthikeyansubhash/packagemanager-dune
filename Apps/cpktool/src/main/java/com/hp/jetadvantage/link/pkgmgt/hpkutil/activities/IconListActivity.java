package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.application.AppInfoGetServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.UninstallServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.sun.prism.impl.Disposer.Record;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class IconListActivity extends CommonController {

    @FXML
    private TextField textHost;
    @FXML
    private PasswordField textAdminPassword;
    @FXML
    private TableView tableAppInfo;
    @FXML
    private TableColumn colName;
    @FXML
    private TableColumn colUuid;
    @FXML
    private TableColumn colDetail;
    @FXML
    private TableColumn colUninstall;
    @FXML
    private Button btnLoad;
    @FXML
    private ProgressBar progressBar;

    private List<AppInfo> appInfoList;

    private AppInfo currentSelected;

    private AppInfo detailAppInfo;

    private ObservableList<AppInfo> data = FXCollections.observableArrayList();

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView();
    }

    private void initView() {
        setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_INSTALL_MIN_HEIGHT);
        textHost.setText(Constants.DEFAULT_HOST);
        textAdminPassword.setText(Constants.DEFAULT_USER_PASSWORD);
    }

    public void initTable() {
        tableAppInfo.setSelectionModel(null);

        colName.setCellValueFactory(new PropertyValueFactory<AppInfo, String>("name"));
        colUuid.setCellValueFactory(new PropertyValueFactory<AppInfo, String>("uuid"));

        colDetail.setCellFactory(
                new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

                    @Override
                    public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
                        return new ButtonCell(ButtonType.DETAIL);
                    }
                }
        );

        colUninstall.setCellFactory(
                new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

                    @Override
                    public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
                        return new ButtonCell(ButtonType.UNINSTALL);
                    }
                }
        );

        tableAppInfo.setItems(data);
    }

    private void setProgressBar(final boolean enable) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisible(enable);
                btnLoad.setDisable(enable);

            }
        });
    }

    private class ButtonCell extends TableCell<Record, Boolean> {
        Button cellButton;

        ButtonCell(ButtonType buttonType) {
            cellButton = new Button();

            if (buttonType.equals(ButtonType.UNINSTALL)) {
                cellButton.getStyleClass().add("buttonUninstall");
                cellButton.setId(ButtonType.UNINSTALL.toString());
            } else if (buttonType.equals(ButtonType.DETAIL)) {
                cellButton.getStyleClass().add("buttonInformation");
                cellButton.setId(ButtonType.DETAIL.toString());
                if (DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)) {
                    cellButton.setVisible(false);
                }
            } else {
                cellButton = null;
            }

            if (cellButton != null) {
                cellButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent actionEvent) {
                        String buttonType = ((Button) actionEvent.getSource()).getId();

                        currentSelected = (AppInfo) ButtonCell.this.getTableView().getItems().get(ButtonCell.this.getIndex());

                        if (buttonType.equalsIgnoreCase(ButtonType.DETAIL.toString())) {
                            if (!progressBar.isVisible()) {
                                AppInfoGetServiceGUI appInfoGetServiceGUI = new AppInfoGetServiceGUI(textHost.getText(), UUID.fromString(currentSelected.getUuid()), getDetailTaskInterface);
                                appInfoGetServiceGUI.start();
                                setProgressBar(true);
                            }
                        } else {
                            if (!textHost.getText().isEmpty() && Utils.isValidUuid(currentSelected.getUuid())) {
                                ButtonClickListener deleteButtonClickListener = new ButtonClickListener() {
                                    @Override
                                    public void ok() {
                                        setProgressBar(true);
                                        currentAction = ActionType.UNINSTALL;

                                        UninstallServiceGUI uninstallServiceGUI = new UninstallServiceGUI(uninstallTaskInterface);
                                        uninstallServiceGUI.setHost(textHost.getText());
                                        uninstallServiceGUI.setUuid(UUID.fromString(currentSelected.getUuid()));
                                        uninstallServiceGUI.setAccount(Constants.DEFAULT_USER_NAME, textAdminPassword.getText());
                                        uninstallServiceGUI.start();
                                    }

                                    @Override
                                    public void cancel() {
                                        actionEvent.consume();
                                    }
                                };

                                showYesOrNoDialog(State.WARNING,
                                        Constants.MESSAGE.getString("msg_confirmation"),
                                        Constants.MESSAGE.getString("msg_remove_confirm"),
                                        null,
                                        deleteButtonClickListener);
                            } else {
                                showMsg(State.ERROR,
                                        Constants.MESSAGE.getString("msg_error"),
                                        Constants.MESSAGE.getString("msg_uninstall_failed"),
                                        Constants.MESSAGE.getString("msg_input_error"));
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            if (data != null) {
                if (i < data.size()) {
                    setGraphic(cellButton);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    private void setUninstallLog(final TaskStatus taskStatus) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                if (taskStatus != null) {
                    if (taskStatus.getState().equals(PackageInstallerState.psCompleted)) {
                        data.remove(currentSelected);
                        msg = Constants.MESSAGE.getString("msg_uninstall_success");
                        showMsg(State.INFORMATION,
                                Constants.MESSAGE.getString("msg_success"),
                                msg,
                                null);
                    } else if (taskStatus.getState().equals(PackageInstallerState.psFailed)) {
                        msg = Constants.MESSAGE.getString("msg_uninstall_failed");
                        showMsg(State.ERROR,
                                Constants.MESSAGE.getString("msg_failed"),
                                msg,
                                taskStatus.getCause());
                    }
                }
            }
        });
    }

    private TaskInterface uninstallTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            setUninstallLog(taskStatus);
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            setProgressBar(false);
        }

        @Override
        public void onFailed(Exception e) {
            setProgressBar(false);
        }
    };

    private TaskInterface getDetailTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            setProgressBar(false);
            detailAppInfo = (AppInfo) obj;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (detailAppInfo != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/icon_detail_activity.fxml"));
                            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
                            VBox newWindow = (VBox) loader.load();
                            Stage stage = new Stage();
                            stage.setTitle(Constants.MESSAGE.getString("detail"));
                            stage.initModality(Modality.NONE);
                            Scene scene = new Scene(newWindow);
                            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
                            stage.setResizable(false);
                            stage.setScene(scene);
                            stage.sizeToScene();
                            ((IconDetailActivity) loader.getController()).setValue(detailAppInfo);
                            stage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onFailed(Exception e) {
            setProgressBar(false);
            String msg = e.getMessage();
            if (e instanceof ResourceException) {
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                    msg = Constants.MESSAGE.getString("msg_install_failed");
                }
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                }
            }
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_input_error"),
                    msg);
        }
    };

    private TaskInterface getListTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            setProgressBar(false);
            appInfoList = (List<AppInfo>) obj;
            if (appInfoList != null) {
                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.getPlatformType().equalsIgnoreCase(Constants.DEFAULT_PLATFORM_TYPE.toString())) {
                        data.add(appInfo);
                    }
                }
                initTable();
            }
        }

        @Override
        public void onFailed(Exception e) {
            setProgressBar(false);
            String msg = e.getMessage();
            if (e instanceof ResourceException) {
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                }
            }
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_input_error"),
                    msg);
        }
    };

    @FXML
    protected void handleLoadButtonAction() {
        try {
            if (!textHost.getText().isEmpty()) {
                setProgressBar(true);
                tableAppInfo.getItems().clear();
                Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();
                Constants.DEFAULT_HOST = textHost.getText();
                AppInfoGetServiceGUI appInfoGetServiceGUI = new AppInfoGetServiceGUI(textHost.getText(), getListTaskInterface);
                appInfoGetServiceGUI.start();
            } else {
                throw new Exception("");
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_input_error"),
                    e.getMessage());
        }
    }
}
