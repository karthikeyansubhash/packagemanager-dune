package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.attestation.AttestationUpdateServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Attestation;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AttestationActivity extends CommonController {

    @FXML
    private Label textDescription;
    @FXML
    private TextField textFile;
    @FXML
    private TextField textUuid;
    @FXML
    private TextField textHost;
    @FXML
    private PasswordField textAdminPassword;
    @FXML
    private TextField textUserName;
    @FXML
    private TextField textLdbKey;
    @FXML
    private TextArea textClientCredentials;
    @FXML
    private TextField textCommandLocation;
    @FXML
    private Button btnUpdateAttestation;
    @FXML
    private ProgressBar progressBar;

    private File commandFile;


    private AttestationMode attestationMode;

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView(currentAction);
    }

    private void initView(ActionType actionType) {
        switch (actionType) {
            case ATTESTATION:
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_INSTALL_MIN_HEIGHT);
                textDescription.setText(Constants.MESSAGE.getString("title_attestation"));
                textUuid.setText(Constants.DEFAULT_UUID);
                textAdminPassword.setText(Constants.DEFAULT_USER_PASSWORD);
                textHost.setText(Constants.DEFAULT_HOST);
                currentFile = Constants.DEFAULT_HPK;
                if (currentFile != null) {
                    loadHpk();
                }

                JsonArray defaultClientsInfo = new JsonArray();
                JsonObject clientInfo = new JsonObject();
                clientInfo.addProperty("client_id", "");
                clientInfo.addProperty("client_secret", "");
                defaultClientsInfo.add(clientInfo);

                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create();
                textClientCredentials.setText(gson.toJson(defaultClientsInfo));
                break;
        }
    }

    private void loadHpk() {
        if (currentFile != null){
            try {
                if (!Utils.checkSupportFormat(currentFile).equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    throw new Exception(Constants.MESSAGE.getString("error_not_support_format"));
                }

                hpkFile = new HpkFile(currentFile);
                Connector connector = hpkFile.getConnector();
                textFile.setText(currentFile.getName());
                textUuid.setText(connector.getUuid().toString());
                Constants.DEFAULT_HPK = currentFile;
            } catch (Exception e) {
                showMsg(State.ERROR,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("menu_open_hpk_file"),
                        Constants.MESSAGE.getString("msg_failed_to_open") + currentFile.getAbsolutePath() + "\n" + e.getMessage());
            } finally {
                try {
                    if (hpkFile != null) {
                        hpkFile.close();
                    }
                } catch (IOException e) {}
            }
        }
    }

    @FXML
    protected void handleInstallerBrowseAction(ActionEvent actionEvent) {
        currentFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
        loadHpk();
    }

    @FXML
    protected void handleUpdate(ActionEvent actionEvent) {
        attestationMode = AttestationMode.UPDATE;
        updateAttestation();

    }

    @FXML
    protected void handleCommandLocationAction(ActionEvent actionEvent) {
        File selectedDirectory = selectDirectory(stage);

        try {
            if (selectedDirectory != null) {
                commandFile = setValues(selectedDirectory, textCommandLocation);
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_failed_to_open"),
                    e.getMessage());
        }
    }

    private File setValues(File selectedFile, TextField textField) {
        if (selectedFile != null) {
            try {
                textField.setText(selectedFile.getName());
                return selectedFile;
            } catch (Exception e) {
                showMsg(State.ERROR,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("msg_failed_to_open"),
                        e.getMessage());
            }
        }
        return null;
    }

    @FXML
    protected void handleCommandLocationClearAction(ActionEvent actionEvent) {
        commandFile = null;
        textCommandLocation.setText(null);
    }

    private void updateAttestation() {
        try {
            if (!textHost.getText().isEmpty()
                    && !textUuid.getText().isEmpty()
                    && !textUserName.getText().isEmpty()
                    && !textLdbKey.getText().isEmpty()
                    && !textClientCredentials.getText().isEmpty()) {

                if (!Utils.isValidUuid(textUuid.getText())) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_uuid_error"));
                }

                if (!Utils.isValidJSONArray(textClientCredentials.getText())) {
                    throw new JsonSyntaxException(Constants.MESSAGE.getString("msg_json_invalid_error") + "\n" + textClientCredentials.getText());
                }

                Attestation attestation = new Attestation();

                JsonParser parser = new JsonParser();
                JsonArray clientCredentials = parser.parse(textClientCredentials.getText()).getAsJsonArray();

                attestation.setHost(textHost.getText());
                attestation.setUuid(textUuid.getText());
                attestation.setUser(attesFormatter(textUserName.getText()));
                attestation.setKey(attesFormatter(textLdbKey.getText()));
                attestation.setData(clientCredentials);

                disableAttestationObject(true);
                Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();
                Constants.DEFAULT_HOST = textHost.getText();

                AttestationUpdateServiceGUI attestationUpdateServiceGUI = new AttestationUpdateServiceGUI(
                        textHost.getText(), UUID.fromString(textUuid.getText()), attestation, commandFile, taskInterface);
                attestationUpdateServiceGUI.start();
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

    private String attesFormatter(String attesStr) {
        if (attesStr.startsWith("'") && attesStr.endsWith("'")) {
            attesStr = attesStr.substring(1, attesStr.length() - 1);
        }

        return attesStr.replaceAll("'", "'\\\\''")
                .replaceAll("\"", "\\\\\"");
    }

    private void disableAttestationObject(final boolean enable){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnUpdateAttestation.setDisable(enable);
                progressBar.setVisible(enable);
            }
        });
    }

    private TaskInterface taskInterface = new TaskInterface() {
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            disableAttestationObject(false);

            if (attestationMode == AttestationMode.UPDATE) {
                showMsg(State.INFORMATION,
                        Constants.MESSAGE.getString("msg_success"),
                        Constants.MESSAGE.getString("msg_success_save"),
                        null);
            }
        }

        @Override
        public void onFailed(Exception e) {
            disableAttestationObject(false);

            String msg = e.getMessage();
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_input_error"),
                    msg);
        }
    };

    enum AttestationMode {
        UPDATE
    }
}
