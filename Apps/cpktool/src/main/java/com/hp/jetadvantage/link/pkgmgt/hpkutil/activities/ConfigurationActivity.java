package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration.ConfigGetServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration.ConfigUpdateServiceGUI;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Configuration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.UUID;

public class ConfigurationActivity extends CommonController {

    @FXML
    private TextField textFile;
    @FXML
    private TextField textUuid;
    @FXML
    private TextField textHost;
    @FXML
    private PasswordField textAdminPassword;
    @FXML
    private Button btnGetConfiguration;
    @FXML
    private Button btnUpdateConfiguration;
    @FXML
    private TextArea textConfiguration;
    @FXML
    private ProgressBar progressBar;

    private ConfigMode configMode;
    private Configuration configuration;

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView(currentAction);
    }

    private void initView(ActionType actionType){
        switch (actionType) {
            case CONFIGURATION:
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_INSTALL_MIN_HEIGHT);
                textUuid.setText(Constants.DEFAULT_UUID);
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
        currentFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
        loadHpk();
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
    protected void handleGet(ActionEvent actionEvent) {
        configMode = ConfigMode.GET;
        getConfiguration();
    }

    @FXML
    protected void handleUpdate(ActionEvent actionEvent) {
        configMode = ConfigMode.UPDATE;
        updateConfiguration();
    }

    private void getConfiguration(){
        try{
            if(!textHost.getText().isEmpty()
                    && !textUuid.getText().isEmpty()){
                if(!Utils.isValidUuid(textUuid.getText())){
                    throw new IllegalArgumentException("Invalid UUID string: "+ textUuid.getText());
                }
                disableConfigurationObject(true);
                Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();
                Constants.DEFAULT_HOST = textHost.getText();
                ConfigGetServiceGUI configManager = new ConfigGetServiceGUI(textHost.getText(), UUID.fromString(textUuid.getText()), taskInterface);
                configManager.start();
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

    private void updateConfiguration() {
        try {
            if (!textHost.getText().isEmpty()
                    && !textUuid.getText().isEmpty()
                    && !textConfiguration.getText().isEmpty()) {
                if (!Utils.isValidUuid(textUuid.getText())) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_uuid_error"));
                }

                if (!Utils.isValidJSON(textConfiguration.getText())) {
                    throw new JsonSyntaxException(Constants.MESSAGE.getString("msg_json_invalid_error")  + textConfiguration.getText());
                }

                Configuration configuration = new Configuration();

                JsonParser parser = new JsonParser();
                JsonObject configurationData = parser.parse(textConfiguration.getText()).getAsJsonObject();
                configuration.setData(configurationData);
                configuration.setUuid(textUuid.getText());

                disableConfigurationObject(true);
                Constants.DEFAULT_USER_PASSWORD = textAdminPassword.getText();
                Constants.DEFAULT_HOST = textHost.getText();
                ConfigUpdateServiceGUI configUpdateServiceGUI = new ConfigUpdateServiceGUI(
                        textHost.getText(), UUID.fromString(textUuid.getText()), configuration, taskInterface);
                configUpdateServiceGUI.start();
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

    private void disableConfigurationObject(final boolean enable){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnGetConfiguration.setDisable(enable);
                btnUpdateConfiguration.setDisable(enable);
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
            configuration = (Configuration) obj;
            if(configuration != null) {
                showConfigure(configuration);
                if(configMode == ConfigMode.UPDATE) {
                    showMsg(State.INFORMATION,
                            Constants.MESSAGE.getString("msg_success"),
                            Constants.MESSAGE.getString("msg_success_save"),
                            null);
                }
            }
            disableConfigurationObject(false);
        }

        @Override
        public void onFailed(Exception e) {
            String msg = e.getMessage();
            if (e instanceof ResourceException) {
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                }
            }
            disableConfigurationObject(false);
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_input_error"),
                    msg);
        }
    };

    private void showConfigure(Configuration configuration) {
        try {
            if(configuration.getData() == null){
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_warning"),
                        Constants.MESSAGE.getString("msg_json_error"),
                        null);
            } else {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create();
                textConfiguration.setText(gson.toJson(configuration.getData()));
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_json_value_error"),
                    "Invalid Json data: " + configuration.getData());
        }
    }

    enum ConfigMode{
        GET, UPDATE
    }
}
