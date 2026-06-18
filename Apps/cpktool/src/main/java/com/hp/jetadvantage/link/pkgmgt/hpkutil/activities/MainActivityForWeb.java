package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.HpkFileHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.HPKVersion;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.restlet.engine.io.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.UUID;

public class MainActivityForWeb extends CommonController{

    @FXML
    private Label textDescription;
    @FXML
    private TextField textInstallFile;
    @FXML
    private TextField textUuid;
    @FXML
    private TextField textName;
    @FXML
    private TextField textVendor;
    @FXML
    private TextField dateCreation;
    @FXML
    private Label textWarningMsg;
    @FXML
    private Button btnCreateHpk;
    @FXML
    private ButtonManagerActivity buttonManagerController;

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView(currentAction);
    }

    private void initView(ActionType actionType) {
        switch (actionType) {
            case OPEN:
                setStyle();
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_MIN_HEIGHT);
                File selectedFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
                if (selectedFile != null) {
                    initOpenType(selectedFile);
                } else {
                    this.currentAction = ActionType.NEW_LINK_FOR_WEB;
                    initNewView(ActionType.NEW_LINK_FOR_WEB);
                }
                break;
            default:
                setStyle();
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_MIN_HEIGHT);
                initNewView(actionType);
                break;
        }
    }

    private void setStyle() {
        dateCreation.setStyle("-fx-opacity: 1.0;");
    }

    private void initOpenType(File currentFile) {
        try {
            if (!PlatformType.LinkForWeb.equals(Utils.checkSupportFormat(currentFile))) {
                throw new Exception(Constants.MESSAGE.getString("error_not_support_format"));
            }
            HpkFile hpkFile = new HpkFile(currentFile);
            Connector connector = hpkFile.getConnector();
            initNewView(ActionType.NEW_LINK_FOR_WEB);
            loadHPKFile(connector);
            btnCreateHpk.setText(Constants.MESSAGE.getString("btn_save_as"));
            this.currentFile = currentFile;
            this.hpkFile = hpkFile;

            Constants.DEFAULT_HPK = currentFile;

            currentInstallFile = File.createTempFile("installFile", "tmp");
            try (FileOutputStream fos = new FileOutputStream(currentInstallFile)) {
                IoUtils.copy(hpkFile.getInstallFile(), fos);
                buttonManagerController.setValues(getButtonInfo());
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("menu_open_hpk_file"),
                    Constants.MESSAGE.getString("msg_failed_to_open") + " " + currentFile.getAbsolutePath() + "\n" + e.getMessage());
            initNewView(ActionType.NEW_LINK_FOR_WEB);
        }
    }

    private void initNewView(ActionType type) {
        currentAction = type;

        textInstallFile.setPromptText(Constants.MESSAGE.getString("hint_select_xml"));
        textDescription.setText(Constants.MESSAGE.getString("title_new_web_hpk") );
        btnCreateHpk.setText(Constants.MESSAGE.getString("btn_create_hpk"));
        hpkFile = null;
        currentFile = null;
        currentInstallFile = null;
        Constants.DEFAULT_UUID = "";
        Constants.DEFAULT_HPK = null;
        clear();
    }

    private void openHpkFile() {
        File selectedFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
        if (selectedFile != null) {
            initOpenType(selectedFile);
        }
    }

    private void loadHPKFile(Connector connector) {
        currentAction = ActionType.NEW_LINK_FOR_WEB;

        textUuid.setText(connector.getUuid().toString());
        textName.setText(connector.getName());
        textVendor.setText(connector.getVendorName());
        textInstallFile.setText(connector.getInstallFile());
        dateCreation.setText(connector.getDate());
        textDescription.setText(Constants.MESSAGE.getString("title_modify_web_hpk"));
    }

    private boolean createHPKFile(Connector connector) {
        boolean deleteInstallFile = false;
        try {
            File savingFile = selectHPKFile(stage, Constants.MESSAGE.getString("msg_save"), FileChooserMode.SAVE);
            if (savingFile == null) return false;

            if (currentInstallFile == null && hpkFile != null) {
                currentInstallFile = File.createTempFile("installFile", "tmp");
                try (FileOutputStream fos = new FileOutputStream(currentInstallFile)) {
                    IoUtils.copy(hpkFile.getInstallFile(), fos);
                }
                deleteInstallFile = true;
            }

            if (HPK.equalsIgnoreCase(Utils.getExtension(savingFile))) {
                HpkFileHelper.createCpk(savingFile, connector, currentInstallFile, null);
            } else {
                savingFile = new File(savingFile + "." + HPK);
                HpkFileHelper.createCpk(savingFile, connector, currentInstallFile, null);
            }

            Constants.DEFAULT_UUID = textUuid.getText();
            Constants.DEFAULT_HPK = savingFile;

            showMsg(State.INFORMATION,
                    Constants.MESSAGE.getString("msg_information"),
                    Constants.MESSAGE.getString("msg_success_save"),
                    null);
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_fail_save"),
                    e.getMessage());
            return false;
        } finally {
            if (deleteInstallFile) {
                currentInstallFile.delete();
                currentInstallFile = null;
            }
        }
        return true;
    }

    private ButtonInfo getButtonInfo() {
        try {
            return Utils.getButtonInfo(currentInstallFile, getClass());
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_failed_to_open"),
                    e.getMessage());
        }
        return null;
    }

    private Connector getConnector() {
        try {
            Connector connector = new Connector();
            if (Utils.isValidUuid(textUuid.getText())) {
                connector.setUuid(UUID.fromString(textUuid.getText()));
            } else {
                throw new IllegalArgumentException("Invalid UUID string: " + textUuid.getText());
            }
            connector.setName(textName.getText());
            connector.setVendorName(textVendor.getText());
            if (Utils.isValidDate(dateCreation.getText())) {
                connector.setDate(dateCreation.getText());
            } else {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_date_error"));
            }
            connector.setPlatformType(PlatformType.LinkForWeb);
            if (hpkFile != null) {
                connector.setSchemaLocation(hpkFile.getConnector().getSchemaLocation());
            }
            connector.setInstallFile(textInstallFile.getText());

            Constants.setHPKVersion(HPKVersion.HPK_1_0);
            connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_0.toString() + " " + Constants.XSD);
            connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_0.toString());

            Utils.isVerifyFormat(connector, getClass().getResource(Constants.getHPKVersion().getXsdPath()));

            return connector;
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_fail_save"),
                    e.getMessage());
            return null;
        }
    }

    private void clear() {
        textInstallFile.clear();
        textUuid.clear();
        textName.clear();
        textVendor.clear();
        dateCreation.setText(Constants.DATE_FORMAT.format(new Date()));
        btnCreateHpk.setDisable(true);

        textInstallFile.textProperty().addListener(textChangeListener);
        textUuid.textProperty().addListener(textChangeListener);
        textName.textProperty().addListener(textChangeListener);
        textVendor.textProperty().addListener(textChangeListener);
        dateCreation.textProperty().addListener(textChangeListener);

        buttonManagerController.clear();
    }

    @FXML
    protected void handleNewWebMenuAction(ActionEvent actionEvent) {
        clear();
    }

    @FXML
    protected void handleOpenMenuAction(ActionEvent actionEvent) {
        openHpkFile();
    }

    @FXML
    protected void handleCreateHpkMenuAction(ActionEvent actionEvent) {

        buttonManagerController.setUUID(textUuid.getText());

        ButtonInfo buttonInfo = buttonManagerController.getButtonInfo();
        if (buttonInfo != null) {
            currentInstallFile = new File("button.xml");
            buttonManagerController.createBtnXmlFile(buttonInfo, currentInstallFile);
            textInstallFile.setText(currentInstallFile.getName());

            Connector connector = getConnector();
            if (connector != null) {
                if(createHPKFile(connector)) {
                    currentInstallFile.delete();
                    currentInstallFile = null;
                }
            }
        }
    }

    @FXML
    protected void handleUuidGenerateButtonAction() {
        textUuid.setText(UUID.randomUUID().toString());
    }

    ChangeListener textChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            if (textUuid.getText().isEmpty() ||
                    textName.getText().isEmpty() ||
                    textVendor.getText().isEmpty()) {
                btnCreateHpk.setDisable(true);
                textWarningMsg.setText(Constants.MESSAGE.getString("msg_empty_error"));
            } else {
                btnCreateHpk.setDisable(true);
                if (!Utils.isValidUuid(textUuid.getText())) {
                    textWarningMsg.setText(Constants.MESSAGE.getString("msg_uuid_error"));
                } else if (!Utils.isValidDate(dateCreation.getText())) {
                    textWarningMsg.setText(Constants.MESSAGE.getString("msg_date_error"));
                } else {
                    btnCreateHpk.setDisable(false);
                    textWarningMsg.setText("");
                }
            }
        }
    };
}