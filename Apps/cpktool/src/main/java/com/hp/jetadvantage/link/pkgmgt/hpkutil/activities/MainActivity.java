package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.HpkFileHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.*;
import com.hp.jetadvantage.link.pkgmgt.lib.LocalizedString;
import com.sun.prism.impl.Disposer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.restlet.engine.io.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class MainActivity extends CommonController {

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
    private TextField textDefaultConfig;
    @FXML
    private TextField textAuthAgent;
    @FXML
    private TextField textStatistics;
    @FXML
    private HBox minSubActivityBox;
    @FXML
    private Label textWarningMsg;
    @FXML
    private Button btnAddSubActivity;
    @FXML
    private Button btnGetDeviceVersionActivity;
    @FXML
    private Label textDeviceVersionMsg;
    @FXML
    private Button btnSaveAs;
    @FXML
    private TableView tableSubActivity;
    @FXML
    private TableColumn colPlatformId;
    @FXML
    private TableColumn colUuid;
    @FXML
    private TableColumn colDetail;
    @FXML
    private TableColumn colUninstall;
    @FXML
    private CheckBox checkSetAsHome;
    @FXML
    private CheckBox checkConfigOnInstall;
    @FXML
    private Button btnAddAccessory;
    @FXML
    private TableView tableAccessory;
    @FXML
    private TableColumn colAccessoryType;
    @FXML
    private TableColumn colVendorId;
    @FXML
    private TableColumn colProductId;
    @FXML
    private TableColumn colAccessoryDetail;
    @FXML
    private TableColumn colRemoveAccessory;
    @FXML
    private TextField textWebService;
    @FXML
    private Button btnAddWebServiceActivity;
    @FXML
    private Button btnAddWebServiceEndPoint;
    @FXML
    private TableView tableWebServiceEndPoint;
    @FXML
    private TableColumn colWebServiceEndPointMethod;
    @FXML
    private TableColumn colWebServiceEndPointCategory;
    @FXML
    private TableColumn colWebServiceEndPointAbsolutePath;
    @FXML
    private TableColumn colWebServiceEndPointAuthType;
    @FXML
    private TableColumn colWebServiceEndPointDetail;
    @FXML
    private TableColumn colWebServiceEndPointRemove;
    @FXML
    private ComboBox<LinkPlatformVersion> cbBoxPlatformVersion;

    //HBox component is a layout component. It's not to store data.
    @FXML
    private HBox hboxAccessory;
    @FXML
    private HBox hboxWebService;
    @FXML
    private HBox hboxHomeScreen;
    @FXML
    private HBox hboxAuthAgent;
    @FXML
    private HBox hboxStatisticsAgent;
    @FXML
    private HBox hboxPlatformVersion;

    private List<SubActivityInfo> subActivityList = new ArrayList<>();
    private List<AccessoryInfo> accessoryInfoList = new ArrayList<>();
    private WebServiceInfo webServiceInfo = null;
    private List<AuthAgentInfo> authAgentList = new ArrayList<>();
    private StatisticsAgentInfo statisticsAgentInfo = null;
    private HomeScreenMode homeScreenMode = new HomeScreenMode();

    private ObservableList observableSubAppList = FXCollections.observableArrayList();
    private ObservableList observableAccessoryList = FXCollections.observableArrayList();
    private ObservableList observableWebServiceEndPointList = FXCollections.observableArrayList();
    private ObservableList observablePlatformVersionList = FXCollections.observableArrayList();

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView(currentAction);
    }

    private void initView(ActionType actionType) {
        switch (actionType) {
            case NEW_LINK_FOR_DEVICE:
            case NEW_LINK_FOR_WEB:
                setStyle();
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_MIN_HEIGHT);
                initNewView(actionType);
                break;
            case OPEN:
                setStyle();
                setMinScreenSize(Constants.SCREEN_MIN_WIDTH, Constants.SCREEN_MIN_HEIGHT);
                File selectedFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
                if (selectedFile != null) {
                    initOpenType(selectedFile);
                } else {
                    this.currentAction = ActionType.NEW_LINK_FOR_DEVICE;
                    initNewView(ActionType.NEW_LINK_FOR_DEVICE);
                }
                break;
        }
    }


    private void setStyle() {
        dateCreation.setStyle("-fx-opacity: 1.0;");
    }

    @FXML
    protected void handleOpenMenuAction(ActionEvent actionEvent) {
        openHpkFile();
    }

    @FXML
    protected void handleUuidGenerateButtonAction(ActionEvent actionEvent) {
        textUuid.setText(UUID.randomUUID().toString());
        if (authAgentList != null) {
            for (AuthAgentInfo info : authAgentList) {
                info.setUuid(UUID.randomUUID());
            }
        }
    }

    @FXML
    protected void handleCbBoxPlatformVersionAction() {
        LinkPlatformVersion linkPlatformVersion = cbBoxPlatformVersion.getSelectionModel().getSelectedItem();
        HPKVersion hpkVersion = linkPlatformVersion.getHpkVersion();
        Constants.setHPKVersion(hpkVersion);
        Constants.setPlatformVersion(linkPlatformVersion);
        initHPKVersionView();
    }

    @FXML
    protected void handleAddSubActivity(ActionEvent actionEvent) {
        openSubActivityController(null);
    }

    @FXML
    protected void handleAddAccessory(ActionEvent actionEvent) {
        if (accessoryInfoList.size() < Constants.MAX_ACCESSORIES) {
            openAccessoryController(null);
        }
    }

    @FXML
    protected void handleWebServiceAction(ActionEvent actionEvent) {
        openWebServiceActivity(webServiceInfo);
    }

    @FXML
    protected void handleWebServiceClearAction(ActionEvent actionEvent) {
        webServiceInfo = null;
        refreshWebServiceView();
    }

    @FXML
    protected void handleAddWebServiceEndPoint(ActionEvent actionEvent) {
        openWebServiceEndPointController(null);
    }

    private void openSubActivityController(SubActivityInfo info) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sub_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("menu_add_icon_configuration"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddSubActivity.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((SubActivity) loader.getController()).setMainWindow(this);
            if (info != null) {
                ((SubActivity) loader.getController()).setValues(info);
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openAccessoryController(AccessoryInfo info) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/accessory_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("accessory"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddAccessory.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((ProviderForAccessoryActivity) loader.getController()).setMainWindow(this);
            if (info != null) {
                ((ProviderForAccessoryActivity) loader.getController()).setValues(info);
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openWebServiceEndPointController(WebServiceEndPoint webServiceEndPoint) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/webservice_endpoint_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("webservice"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddWebServiceEndPoint.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((WebServiceEndPointActivity) loader.getController()).setMainWindow(this);
            if (webServiceEndPoint != null) {
                ((WebServiceEndPointActivity) loader.getController()).setValues(webServiceEndPoint);
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void initNewView(ActionType type) {
        minSubActivityBox.setVisible(true);
        textInstallFile.setPromptText(Constants.MESSAGE.getString("hint_select_apk"));
        textDescription.setText(Constants.MESSAGE.getString("title_new_device_hpk"));
        Constants.DEFAULT_PLATFORM_TYPE = PlatformType.LinkForDevice;

        currentAction = type;
        btnSaveAs.setText(Constants.MESSAGE.getString("btn_create_hpk"));
        hpkFile = null;
        currentFile = null;
        currentInstallFile = null;
        defaultConfigFile = null;
        Constants.DEFAULT_UUID = "";
        Constants.DEFAULT_HPK = null;
        clear();
        initHPKVersionView();
    }

    private void initHPKVersionView() {
        HPKVersion hpkVersion = Constants.getHPKVersion();
        if (hpkVersion != null) {
            enableView(hboxStatisticsAgent);
            enableView(hboxAuthAgent);
            enableView(hboxHomeScreen);
            enableView(hboxAccessory);
            enableView(hboxWebService);
            enableView(hboxPlatformVersion);

            LinkPlatformVersion linkPlatformVersion = Constants.getPlatformVersion();
            if (linkPlatformVersion.checkPlatformVersion(hpkVersion) == false) {
                linkPlatformVersion = LinkPlatformVersion.getEnumByHPKVersion(hpkVersion);
            }
            String title = Constants.MESSAGE.getString("menu_toolname");

            if (hpkVersion.equals(HPKVersion.HPK_1_0)) {
                disableView(hboxStatisticsAgent);
                disableView(hboxAuthAgent);
                disableView(hboxHomeScreen);
                disableView(hboxAccessory);
                disableView(hboxWebService);
                disableView(hboxPlatformVersion);
                title = title + String.format(" (Version %s) - %s", TOOL_VERSION, MESSAGE.getString("hpk_version_1_0"));
            } else if (hpkVersion.equals(HPKVersion.HPK_1_1)) {
                disableView(hboxStatisticsAgent);
                disableView(hboxAccessory);
                disableView(hboxWebService);
                disableView(hboxPlatformVersion);
                title = title + String.format(" (Version %s) - %s", TOOL_VERSION, MESSAGE.getString("hpk_version_1_1"));
            } else if (hpkVersion.equals(HPKVersion.HPK_1_2)) {
                disableView(hboxStatisticsAgent);
                disableView(hboxWebService);
                disableView(hboxPlatformVersion);
                title = title + String.format(" (Version %s) - %s", TOOL_VERSION, MESSAGE.getString("hpk_version_1_2"));
            } else if (hpkVersion.equals(HPKVersion.HPK_1_3)) {
                disableView(hboxStatisticsAgent);
                disableView(hboxWebService);
                title = title + String.format(" (Version %s) - %s", TOOL_VERSION, MESSAGE.getString("hpk_version_1_3"));
            } else if (hpkVersion.equals(HPKVersion.HPK_1_4)) {
                title = title + String.format(" (Version %s) - %s", TOOL_VERSION, MESSAGE.getString("hpk_version_1_4"));
            }
            stage.setTitle(title);
            cbBoxPlatformVersion.getSelectionModel().select(linkPlatformVersion);
            stage.sizeToScene();
        }
    }

    private void enableView(Pane view) {
        view.setManaged(true);
        view.setVisible(true);
    }

    private void disableView(Pane view) {
        view.setManaged(false);
        view.setVisible(false);
    }

    private void initOpenType(File currentFile) {
        try {
            if (!Utils.checkSupportFormat(currentFile).equals(PlatformType.LinkForDevice)) {
                throw new Exception(Constants.MESSAGE.getString("error_not_support_format"));
            }

            HpkFile hpkFile = new HpkFile(currentFile);
            Connector connector = hpkFile.getConnector();
            PlatformType currentType = connector.getPlatformType();
            initNewView(Utils.getActionType(currentType));
            String schema = hpkFile.getConnector().getSchemaLocation();
            String version = schema.substring(schema.lastIndexOf("/") + 1, schema.lastIndexOf(" "));
            LinkPlatformVersion linkPlatformVersion = LinkPlatformVersion.getEnumByValue(hpkFile.getConnector().getPlatformVersion());
            HPKVersion hpkVersion = HPKVersion.getHPKVersion(version);
            if (linkPlatformVersion != null && linkPlatformVersion.getHpkVersion().getLevel() > hpkVersion.getLevel()) {
                hpkVersion = linkPlatformVersion.getHpkVersion();
            }
            Constants.setHPKVersion(hpkVersion);
            loadHPKFile(connector);
            btnSaveAs.setText(Constants.MESSAGE.getString("btn_save_as"));
            this.currentFile = currentFile;
            this.hpkFile = hpkFile;
            this.currentInstallFile = null;
            this.defaultConfigFile = null;
            Constants.DEFAULT_HPK = currentFile;
            initHPKVersionView();
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("menu_open_hpk_file"),
                    Constants.MESSAGE.getString("msg_failed_to_open") + " " + currentFile.getAbsolutePath() + "\n" + e.getMessage());
            initNewView(ActionType.NEW_LINK_FOR_DEVICE); //[OXPd block] Temporary
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

    private void openHpkFile() {
        File selectedFile = selectHPKFile(stage, null, FileChooserMode.OPEN);
        if (selectedFile != null) {
            closeHpkFile();
            initOpenType(selectedFile);
        }
    }

    private void loadHPKFile(Connector connector) {
        textUuid.setText(connector.getUuid().toString());
        textName.setText(connector.getName());
        textVendor.setText(connector.getVendorName());
        textInstallFile.setText(connector.getInstallFile());
        dateCreation.setText(connector.getDate());
        textDefaultConfig.setText(connector.getDefaultConfig());
        PlatformType currentType = connector.getPlatformType();

        String platformVersion = connector.getPlatformVersion();
        if (platformVersion != null) {
            LinkPlatformVersion linkPlatformVersion = LinkPlatformVersion.getEnumByValue(platformVersion);
            if (observablePlatformVersionList.contains(linkPlatformVersion)) {
                cbBoxPlatformVersion.getSelectionModel().select(linkPlatformVersion);
                Constants.setPlatformVersion(linkPlatformVersion);
            }
        }

        currentAction = ActionType.NEW_LINK_FOR_DEVICE;
        textDescription.setText(Constants.MESSAGE.getString("title_modify_device_hpk"));
        minSubActivityBox.setVisible(true);

        List<SubApp> subApps = connector.getSubAppList();
        subActivityList.clear();
        if (subApps != null) {
            for (SubApp subApp : subApps) {
                SubActivityInfo info = new SubActivityInfo();
                info.setUuid(subApp.getUuid());
                info.setPlatformId(subApp.getPlatformId());
                subActivityList.add(info);
            }
        }

        refreshSubActivityView();

        ArrayList<Provider> providers = connector.getProviders();
        if (providers != null && providers.size() > 0) {
            authAgentList = convertProviderToAuthInfo(providers);
            homeScreenMode = convertProviderToHomeScreenMode(providers);
            accessoryInfoList = convertProviderToAccessoryInfo(providers);
            webServiceInfo = convertProviderToWebServiceInfo(providers);
            statisticsAgentInfo = convertProviderToStatisticInfo(providers);
        }
        refreshAuthAgentView();
        refreshStatisticsView();
        refreshHomescreenView();
        refreshAccessoryView();
        refreshWebServiceView();
    }

    @FXML
    protected void handleBrowseAction(ActionEvent actionEvent) {
        File selectedFile = selectFile(stage, currentAction, FileChooserMode.OPEN);
        if (selectedFile != null) {
            currentInstallFile = setValues(selectedFile, textInstallFile);
        }
    }

    @FXML
    protected void handleDeviceVersionAction(ActionEvent actionEvent) {
        openConnectDeviceActivity();
    }

    @FXML
    protected void handleDefaultConfigAction(ActionEvent actionEvent) {
        File selectedFile = selectFile(stage, ActionType.CONFIGURATION, FileChooserMode.OPEN);

        try {
            if (selectedFile != null) {
                selectedFile = Utils.checkDefaultConfigValidation(selectedFile.getAbsolutePath());
                defaultConfigFile = setValues(selectedFile, textDefaultConfig);
            }
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_failed_to_open"),
                    e.getMessage());
        }
    }

    @FXML
    protected void handleDefaultConfigClearAction(ActionEvent actionEvent) {
        defaultConfigFile = null;
        textDefaultConfig.setText(null);
    }

    @FXML
    protected void handleCheckSetAsHome(ActionEvent actionEvent) {
        if (checkSetAsHome.isSelected()) {
            checkConfigOnInstall.setDisable(false);
        } else {
            checkConfigOnInstall.setDisable(true);
        }
    }

    @FXML
    protected void handleAuthAgentAction(ActionEvent actionEvent) {
        openAuthAgentActivity(authAgentList);
    }

    @FXML
    protected void handleAuthAgentClearAction(ActionEvent actionEvent) {
        authAgentList.clear();
        textAuthAgent.setText("");
    }

    @FXML
    protected void handleStatisticsAction(ActionEvent actionEvent) {
        openStatisticsActivity(statisticsAgentInfo);
    }

    @FXML
    protected void handleStatisticsClearAction(ActionEvent actionEvent) {
        statisticsAgentInfo = null;
        textStatistics.setText("");
    }

    private void openConnectDeviceActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/device_version_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("btn_device_version"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnGetDeviceVersionActivity.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((ProviderForDeviceVersionActivity) loader.getController()).setMainWindow(this);
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openAuthAgentActivity(List<AuthAgentInfo> infos) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth_agent_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("auth_agent"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddSubActivity.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((ProviderForAuthAgentActivity) loader.getController()).setMainWindow(this);
            if (infos != null && infos.size() > 0) {
                ((ProviderForAuthAgentActivity) loader.getController()).setValues(infos.get(0));
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openWebServiceActivity(WebServiceInfo info) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/webservice_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(LAUNGUAGE_RESOURCE, DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(MESSAGE.getString("webservice"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddWebServiceActivity.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((ProviderForWebServiceActivity) loader.getController()).setMainWindow(this);
            if (info != null) {
                ((ProviderForWebServiceActivity) loader.getController()).setValues(info);
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openStatisticsActivity(StatisticsAgentInfo info) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statistics_activity_controller.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("statistics"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAddSubActivity.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((ProviderForStatisticsActivity) loader.getController()).setMainWindow(this);
            if (info != null) {
                ((ProviderForStatisticsActivity) loader.getController()).setValues(info);
            }
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createHPKFile(Connector connector) {
        boolean deleteInstallFile = false;
        boolean deleteDefaultConfigFile = false;
        try {

            UUID uuid = connector.getUuid();
            ArrayList<SubApp> subApps = connector.getSubAppList();

            if (subApps != null) {
                for (SubApp subApp : subApps) {
                    if (uuid.equals(subApp.getUuid())) {
                        showMsg(State.ERROR,
                                Constants.MESSAGE.getString("msg_error"),
                                Constants.MESSAGE.getString("msg_uuid_error"),
                                Constants.MESSAGE.getString("msg_uuid_error_dup"));

                        return;
                    }
                }
            }

            File savingFile = selectHPKFile(stage, Constants.MESSAGE.getString("msg_save"), FileChooserMode.SAVE);
            if (savingFile == null) return;

            if (currentInstallFile == null && hpkFile != null) {
                currentInstallFile = File.createTempFile("installFile", "tmp");
                try (FileOutputStream fos = new FileOutputStream(currentInstallFile)) {
                    IoUtils.copy(hpkFile.getInstallFile(), fos);
                }
                deleteInstallFile = true;
            }

            if (defaultConfigFile == null && hpkFile != null) {
                if (textDefaultConfig.getText() != null &&
                        !textDefaultConfig.getText().isEmpty()) {
                    defaultConfigFile = File.createTempFile("defaultConfigFile", "tmp");
                    try (FileOutputStream fos = new FileOutputStream(defaultConfigFile)) {
                        IoUtils.copy(hpkFile.getDefaultConfigFile(), fos);
                    } catch (Exception e) {
                        defaultConfigFile = null;
                        throw new Exception(Constants.MESSAGE.getString("msg_fail_read_config"));
                    }
                    deleteDefaultConfigFile = true;
                }
            }

            if (Utils.getExtension(savingFile).equals(HPK)) {
                HpkFileHelper.createCpk(savingFile, connector, currentInstallFile, defaultConfigFile);
            } else {
                savingFile = new File(savingFile + "." + HPK);
                HpkFileHelper.createCpk(savingFile, connector, currentInstallFile, defaultConfigFile);
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
        } finally {
            if (deleteInstallFile) {
                currentInstallFile.delete();
                currentInstallFile = null;
            }
            if (deleteDefaultConfigFile) {
                defaultConfigFile.delete();
                defaultConfigFile = null;
            }
        }
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
            connector.setPlatformType(Constants.DEFAULT_PLATFORM_TYPE);

            if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_3.getLevel()) {
                if (cbBoxPlatformVersion.getValue() == null
                        || cbBoxPlatformVersion.getValue().checkPlatformVersion(Constants.getHPKVersion()) == false) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_platform_version_error"));
                } else {
                    connector.setPlatformVersion(cbBoxPlatformVersion.getValue().toString());
                }
            }

            if (subActivityList.size() > 0) {
                ArrayList<SubApp> subAppList = new ArrayList<>();
                for (SubActivityInfo info : subActivityList) {
                    SubApp subApp = new SubApp();
                    subApp.setUuid(info.getUuid());
                    subApp.setPlatformId(info.getPlatformId());
                    subAppList.add(subApp);
                }
                connector.setSubAppList(subAppList);
            }
            connector.setInstallFile(textInstallFile.getText());
            connector.setDefaultConfig(textDefaultConfig.getText());

            ArrayList<Provider> providers = new ArrayList<>();
            if (authAgentList != null) {
                if (subActivityList.size() > 0 && authAgentList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("sub_app_cannot_use_other_option"));
                }
                for (AuthAgentInfo info : authAgentList) {
                    Provider provider = new Provider();
                    provider.setType(Constants.PROVIDER_TYPE_AUTHN);
                    provider.setUuid(info.getUuid().toString());
                    provider.setTitle(convertLocalizationModel(info.getTitles()));
                    provider.setDescription(convertLocalizationModel(info.getDescriptions()));
                    provider.setAuthenticationUrl(info.getIntentUri());
                    provider.setEnablePrePromptCheck(String.valueOf(info.isEnablePrePromptCheck()));
                    providers.add(provider);
                }
            }

            if (statisticsAgentInfo != null && hboxStatisticsAgent.isVisible()) {
                if (subActivityList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("sub_app_cannot_use_statistics_agent_option"));
                }
                Provider provider = new Provider();
                provider.setType(Constants.PROVIDER_TYPE_STATISTICS);
                provider.setUuid(statisticsAgentInfo.getUuid().toString());
                provider.setTitle(convertStatisticsLocalizedStringModel(statisticsAgentInfo.getTitles()));
                provider.setDescription(convertStatisticsLocalizedStringModel(statisticsAgentInfo.getDescriptions()));
                provider.setAckRequiredForDelete(String.valueOf(statisticsAgentInfo.isAckRequiredForDelete()));
                providers.add(provider);
            }

            if (checkSetAsHome.isSelected()) {
                if (subActivityList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("sub_app_cannot_use_other_option"));
                }
                if (authAgentList != null && authAgentList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("home_screen_cannot_use_other_option"));
                }
                Provider provider = new Provider();
                provider.setType(Constants.PROVIDER_TYPE_HOME_SCREEN);
                provider.setEnableHomeScreenMode(String.valueOf(checkSetAsHome.isSelected()));
                provider.setConfigOnInstall(String.valueOf(checkConfigOnInstall.isSelected()));
                providers.add(provider);
            }

            if (accessoryInfoList.size() > 0) {
                if (subActivityList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("sub_app_cannot_use_accessory_option"));
                }
                /*if (checkSetAsHome.isSelected()) {
                    throw new Exception(Constants.MESSAGE.getString("home_screen_cannot_use_other_option"));
                }*/
                for (int i = 0; i < accessoryInfoList.size(); i++) {
                    Provider provider = new Provider();
                    provider.setType(Constants.PROVIDER_TYPE_ACCESSORIES);
                    provider.setRegistrationType(accessoryInfoList.get(i).getRegistrationType().name());
                    provider.setVendorId(accessoryInfoList.get(i).getVendorId());
                    provider.setProductId(accessoryInfoList.get(i).getProductId());
                    provider.setSerialNumber(accessoryInfoList.get(i).getSerialNumber());
                    providers.add(provider);
                }
            }

            if(webServiceInfo != null && hboxWebService.isVisible()) {
                if (webServiceInfo.getWebServiceEndPointList().isEmpty()) {
                    throw new Exception(MESSAGE.getString("webservice_no_endpoint"));
                }
                Provider provider = new Provider();
                provider.setType(PROVIDER_TYPE_WEBSERVICES);
                provider.setUuid(webServiceInfo.getUuid().toString());
                provider.setTitle(convertLocalizationModel(webServiceInfo.getTitles()));
                provider.setDescription(convertLocalizationModel(webServiceInfo.getDescriptions()));
                JsonArray webServiceEndPointArray = new JsonArray();
                for(WebServiceEndPoint webServiceEndPoint : webServiceInfo.getWebServiceEndPointList()) {
                    webServiceEndPointArray.add(webServiceEndPoint.toJsonObject());
                }
                provider.setEndPoints(webServiceEndPointArray.toString());
                providers.add(provider);
            }

            if (providers.size() > 0) {
                connector.setProviders(providers);
            }

            HPKVersion hpkVersion = Constants.getHPKVersion();
            if (hpkVersion != null) {
                if (hpkVersion.equals(HPKVersion.HPK_1_0)) {
                    connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_0.toString() + " " + Constants.XSD);
                    connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_0.toString());
                } else if (hpkVersion.equals(HPKVersion.HPK_1_1)) {
                    connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_1.toString() + " " + Constants.XSD);
                    connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_1.toString());
                } else if (hpkVersion.equals(HPKVersion.HPK_1_2)) {
                    connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_2.toString() + " " + Constants.XSD);
                    connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_2.toString());
                } else if (hpkVersion.equals(HPKVersion.HPK_1_3)) {
                    connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_3.toString() + " " + Constants.XSD);
                    connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_3.toString());
                } else if (hpkVersion.equals(HPKVersion.HPK_1_4)) {
                    connector.setSchemaLocation(Constants.NAMESPACE + HPKVersion.HPK_1_4.toString() + " " + Constants.XSD);
                    connector.setNamespace(Constants.NAMESPACE + HPKVersion.HPK_1_4.toString());
                }
            }

            try {
                Utils.isVerifyFormat(connector, getClass().getResource(Constants.getHPKVersion().getXsdPath()));
            } catch (Exception e) {
                throw new Exception(Constants.MESSAGE.getString("msg_input_error"));
            }
            return connector;
        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_fail_save"),
                    e.getMessage());
            return null;
        }
    }

    public List<SubActivityInfo> getSubActivities() {
        return subActivityList;
    }

    public List<AccessoryInfo> getAccessoryInfos() {
        return accessoryInfoList;
    }

    public void setSubActivity(SubActivityInfo info, boolean isNew) {
        if (isNew) {
            subActivityList.add(info);
        }
        refreshSubActivityView();
    }

    public void setAccessoryInfo(AccessoryInfo accessoryInfo, boolean isNew) {
        if (isNew) {
            if (accessoryInfoList.size() < Constants.MAX_ACCESSORIES) {
                accessoryInfoList.add(accessoryInfo);
            } else {
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("msg_fail_maximum_allowable_range_exceeded"),
                        null);
            }
        }
        refreshAccessoryView();
    }

    public void setWebServiceInfo(WebServiceInfo info) {
        webServiceInfo = info;
        refreshWebServiceView();
    }

    public List<WebServiceEndPoint> getWebServiceEndPointList() {
        if (webServiceInfo == null) {
            return new ArrayList<WebServiceEndPoint>();
        }
        return webServiceInfo.getWebServiceEndPointList();
    }

    public void setWebServiceEndPoint(WebServiceEndPoint webServiceEndPoint, boolean isNew) {
        if (isNew) {
            webServiceInfo.addWebServiceEndPoint(webServiceEndPoint);
        }
        refreshWebServiceView();
    }

    public List<AuthAgentInfo> getAuthAgentList() {
        return authAgentList;
    }

    public void setAuthAgentList(AuthAgentInfo info) {
        authAgentList.clear();
        authAgentList.add(info);
        refreshAuthAgentView();
    }

    public void setStatisticsAgentInfo(StatisticsAgentInfo info) {
        statisticsAgentInfo = info;
        refreshStatisticsView();
    }

    public void setDeviceVersion(String ip, String deviceVersion) {
        if (deviceVersion != null) {
            String msg = Constants.MESSAGE.getString("device_id") + ip + "\n" + Constants.MESSAGE.getString("current_device_version") + deviceVersion;
            textDeviceVersionMsg.setText(msg);
            LinkPlatformVersion linkPlatformVersion = LinkPlatformVersion.getEnumByValue(deviceVersion);
            if (observablePlatformVersionList.contains(linkPlatformVersion)) {
                cbBoxPlatformVersion.getSelectionModel().select(linkPlatformVersion);
                Constants.setPlatformVersion(linkPlatformVersion);
            }
        }
    }

    private void refreshSubActivityView() {
        observableSubAppList.setAll(subActivityList);
    }

    private void refreshAccessoryView() {
        observableAccessoryList.setAll(accessoryInfoList);
        if (accessoryInfoList.size() < Constants.MAX_ACCESSORIES) {
            btnAddAccessory.setDisable(false);
        } else {
            btnAddAccessory.setDisable(true);
        }
    }

    private void refreshWebServiceView() {
        observableWebServiceEndPointList.setAll(getWebServiceEndPointList());
        if (webServiceInfo != null) {
            String title = "";
            for(com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString localizedString : webServiceInfo.getTitles()) {
                if(localizedString.getCode().equals(EN_US)) {
                    title = localizedString.getValue();
                    break;
                }
            }
            textWebService.setText(title);
            btnAddWebServiceEndPoint.setDisable(false);
        } else {
            textWebService.setText(null);
            btnAddWebServiceEndPoint.setDisable(true);
        }
        refreshSaveButton();
    }

    private void refreshAuthAgentView() {
        if (authAgentList != null && authAgentList.size() > 0) {
            AuthAgentInfo info = authAgentList.get(0);
            String title = "";
            for (com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString localizedString : info.getTitles()) {
                if (localizedString.getCode().equals(Constants.EN_US)) {
                    title = localizedString.getValue();
                    break;
                }
            }
            textAuthAgent.setText(title);
        }
    }

    private void refreshStatisticsView() {
        if (statisticsAgentInfo != null) {
            String title = "";
            for (com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString localizedString : statisticsAgentInfo.getTitles()) {
                if (localizedString.getLanguageTag().equals(Constants.EN_US)) {
                    title = localizedString.getValue();
                    break;
                }
            }
            textStatistics.setText(title);
        }
    }

    private void refreshHomescreenView() {
        if (homeScreenMode != null) {
            checkSetAsHome.setSelected(homeScreenMode.isEnableHomeScreenMode());
            if (homeScreenMode.isEnableHomeScreenMode()) {
                checkConfigOnInstall.setDisable(false);
                checkConfigOnInstall.setSelected(homeScreenMode.isConfigOnInstall());
            } else {
                checkConfigOnInstall.setDisable(true);
            }
        }
    }

    public void removeSubActivity(SubActivityInfo info) {
        for (int i = 0; i < subActivityList.size(); i++) {
            if (subActivityList.get(i).getPlatformId().equals(info.getPlatformId())) {
                subActivityList.remove(i);
                observableSubAppList.setAll(subActivityList);
                break;
            }
        }
    }

    public void removeAccessory(AccessoryInfo info) {
        for (int i = 0; i < accessoryInfoList.size(); i++) {
            if (accessoryInfoList.get(i).getProductId().equals(info.getProductId())
                    && accessoryInfoList.get(i).getVendorId().equals(info.getVendorId())
                    && accessoryInfoList.get(i).getRegistrationType().equals(info.getRegistrationType())
                    && ((accessoryInfoList.get(i).getSerialNumber() == null && info.getSerialNumber() == null) || (accessoryInfoList.get(i).getSerialNumber().equals(info.getSerialNumber())))) {
                accessoryInfoList.remove(i);
                refreshAccessoryView();
                break;
            }
        }
    }

    public void removeWebServiceEndPoint(WebServiceEndPoint webServiceEndPoint) {
        for (int i = 0; i < getWebServiceEndPointList().size(); i++) {
            if (getWebServiceEndPointList().get(i).getMethodType().equals(webServiceEndPoint.getMethodType())
                    && getWebServiceEndPointList().get(i).getCategory().equals(webServiceEndPoint.getCategory())
                    && getWebServiceEndPointList().get(i).getAbsolutePath().equals(webServiceEndPoint.getAbsolutePath())
                    && getWebServiceEndPointList().get(i).getAuthType().equals(webServiceEndPoint.getAuthType())) {
                getWebServiceEndPointList().remove(i);
                refreshWebServiceView();
                break;
            }
        }
    }

    @FXML
    protected void handleSaveAsMenuAction(ActionEvent actionEvent) {
        Connector connector = getConnector();
        if (connector != null) {
            createHPKFile(connector);
        }
    }

    protected void clear() {
        textInstallFile.clear();
        textUuid.clear();
        textName.clear();
        textVendor.clear();
        dateCreation.setText(Constants.DATE_FORMAT.format(new Date()));
        observablePlatformVersionList.setAll(Arrays.asList(LinkPlatformVersion.values()));
        cbBoxPlatformVersion.setItems(observablePlatformVersionList);
        cbBoxPlatformVersion.getSelectionModel().select(Constants.LATEST_PLATFORM_VERSION);
        subActivityList.clear();
        accessoryInfoList.clear();
        observableSubAppList.clear();
        observableAccessoryList.clear();
        observableWebServiceEndPointList.clear();
        btnSaveAs.setDisable(true);

        textInstallFile.textProperty().addListener(textChangeListener);
        textUuid.textProperty().addListener(textChangeListener);
        textName.textProperty().addListener(textChangeListener);
        textVendor.textProperty().addListener(textChangeListener);
        dateCreation.textProperty().addListener(textChangeListener);

        observableSubAppList.setAll(subActivityList);
        observableAccessoryList.setAll(accessoryInfoList);

        authAgentList.clear();
        textAuthAgent.clear();
        textStatistics.clear();
        textWebService.clear();

        checkConfigOnInstall.setSelected(false);
        checkConfigOnInstall.setDisable(true);
        checkSetAsHome.setSelected(false);
        homeScreenMode = null;
        statisticsAgentInfo = null;
        btnAddWebServiceEndPoint.setDisable(true);
        webServiceInfo = null;

        initTable();
    }

    protected void refreshSaveButton() {
        if (textInstallFile.getText().isEmpty() ||
                textUuid.getText().isEmpty() ||
                textName.getText().isEmpty() ||
                textVendor.getText().isEmpty()) {
            btnSaveAs.setDisable(true);
            textWarningMsg.setText(Constants.MESSAGE.getString("msg_empty_error"));
        } else {
            btnSaveAs.setDisable(true);
            if (!Utils.isValidUuid(textUuid.getText())) {
                textWarningMsg.setText(Constants.MESSAGE.getString("msg_uuid_error"));
            } else if (!Utils.isValidDate(dateCreation.getText())) {
                textWarningMsg.setText(Constants.MESSAGE.getString("msg_date_error"));
            } else if (webServiceInfo != null && getWebServiceEndPointList().isEmpty()) {
                textWarningMsg.setText(Constants.MESSAGE.getString("msg_webservice_endpoint_error"));
            } else {
                btnSaveAs.setDisable(false);
                textWarningMsg.setText("");
            }
        }
    }

    ChangeListener textChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            refreshSaveButton();
        }
    };

    private class SubAppsButtonCell extends TableCell<Disposer.Record, Boolean> {
        Button cellButton;

        SubAppsButtonCell(ButtonType buttonType) {
            cellButton = new Button();

            if (buttonType.equals(ButtonType.UNINSTALL)) {
                cellButton.getStyleClass().add("buttonUninstall");
                cellButton.setId(ButtonType.UNINSTALL.toString());
            } else if (buttonType.equals(ButtonType.DETAIL)) {
                cellButton.getStyleClass().add("buttonInformation");
                cellButton.setId(ButtonType.DETAIL.toString());
            } else {
                cellButton = null;
            }

            if (cellButton != null) {
                cellButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent actionEvent) {
                        String buttonType = ((Button) actionEvent.getSource()).getId();

                        final SubActivityInfo info = (SubActivityInfo) SubAppsButtonCell.this.getTableView().getItems().get(SubAppsButtonCell.this.getIndex());

                        if (buttonType.equalsIgnoreCase(ButtonType.DETAIL.toString())) {
                            openSubActivityController(info);
                        } else {
                            ButtonClickListener deleteButtonClickListener = new ButtonClickListener() {
                                @Override
                                public void ok() {
                                    removeSubActivity(info);
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
                        }
                    }
                });
            }
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            if (observableSubAppList != null) {
                if (i < observableSubAppList.size()) {
                    setGraphic(cellButton);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    private class AccessoryButtonCell extends TableCell<Disposer.Record, Boolean> {
        Button cellButton;

        AccessoryButtonCell(ButtonType buttonType) {
            cellButton = new Button();

            if (buttonType.equals(ButtonType.REMOVE)) {
                cellButton.getStyleClass().add("buttonUninstall");
                cellButton.setId(ButtonType.REMOVE.toString());
            } else if (buttonType.equals(ButtonType.DETAIL)) {
                cellButton.getStyleClass().add("buttonInformation");
                cellButton.setId(ButtonType.DETAIL.toString());
            } else {
                cellButton = null;
            }

            if (cellButton != null) {
                cellButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent actionEvent) {
                        String buttonType = ((Button) actionEvent.getSource()).getId();

                        final AccessoryInfo info = (AccessoryInfo) AccessoryButtonCell.this.getTableView().getItems().get(AccessoryButtonCell.this.getIndex());

                        if (buttonType.equalsIgnoreCase(ButtonType.DETAIL.toString())) {
                            openAccessoryController(info);
                        } else if (buttonType.equalsIgnoreCase(ButtonType.REMOVE.toString())) {
                            ButtonClickListener deleteButtonClickListener = new ButtonClickListener() {
                                @Override
                                public void ok() {
                                    removeAccessory(info);
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
                        }
                    }
                });
            }
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            if (observableAccessoryList != null) {
                if (i < observableAccessoryList.size()) {
                    setGraphic(cellButton);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    private class WebServiceEndPointButtonCell extends TableCell<Disposer.Record, Boolean> {
        Button cellButton;

        WebServiceEndPointButtonCell(ButtonType buttonType) {
            cellButton = new Button();

            if (buttonType.equals(ButtonType.REMOVE)) {
                cellButton.getStyleClass().add("buttonUninstall");
                cellButton.setId(ButtonType.REMOVE.toString());
            } else if (buttonType.equals(ButtonType.DETAIL)) {
                cellButton.getStyleClass().add("buttonInformation");
                cellButton.setId(ButtonType.DETAIL.toString());
            } else {
                cellButton = null;
            }

            if (cellButton != null) {
                cellButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent actionEvent) {
                        String buttonType = ((Button) actionEvent.getSource()).getId();

                        final WebServiceEndPoint webServiceEndPoint = (WebServiceEndPoint) WebServiceEndPointButtonCell.this.getTableView().getItems().get(WebServiceEndPointButtonCell.this.getIndex());

                        if (buttonType.equalsIgnoreCase(ButtonType.DETAIL.toString())) {
                            openWebServiceEndPointController(webServiceEndPoint);
                        } else if (buttonType.equalsIgnoreCase(ButtonType.REMOVE.toString())) {
                            ButtonClickListener deleteButtonClickListener = new ButtonClickListener() {
                                @Override
                                public void ok() {
                                    removeWebServiceEndPoint(webServiceEndPoint);
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
                        }
                    }
                });
            }
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            if (observableWebServiceEndPointList != null) {
                if (i < observableWebServiceEndPointList.size()) {
                    setGraphic(cellButton);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    public void initTable() {
        // sub apps table initialize
        tableSubActivity.setSelectionModel(null);
        colUuid.setCellValueFactory(new PropertyValueFactory<SubActivityInfo, String>("uuid"));
        colPlatformId.setCellValueFactory(new PropertyValueFactory<SubActivityInfo, String>("platformId"));
        colDetail.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new SubAppsButtonCell(ButtonType.DETAIL);
                    }
                }
        );
        colUninstall.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new SubAppsButtonCell(ButtonType.UNINSTALL);
                    }
                }
        );
        tableSubActivity.setItems(observableSubAppList);

        // accessory table initialize
        tableAccessory.setSelectionModel(null);
        tableAccessory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAccessoryType.setCellValueFactory(new PropertyValueFactory<SubActivityInfo, String>("registrationType"));
        colVendorId.setCellValueFactory(new PropertyValueFactory<SubActivityInfo, String>("vendorId"));
        colProductId.setCellValueFactory(new PropertyValueFactory<SubActivityInfo, String>("productId"));
        colAccessoryDetail.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new AccessoryButtonCell(ButtonType.DETAIL);
                    }
                }
        );
        colRemoveAccessory.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new AccessoryButtonCell(ButtonType.REMOVE);
                    }
                }
        );
        tableAccessory.setItems(observableAccessoryList);

        // webservice table initialize
        tableWebServiceEndPoint.setSelectionModel(null);
        tableWebServiceEndPoint.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colWebServiceEndPointMethod.setCellValueFactory(new PropertyValueFactory<WebServiceEndPoint, String>("methodType"));
        colWebServiceEndPointCategory.setCellValueFactory(new PropertyValueFactory<WebServiceEndPoint, String>("category"));
        colWebServiceEndPointAbsolutePath.setCellValueFactory(new PropertyValueFactory<WebServiceEndPoint, String>("absolutePath"));
        colWebServiceEndPointAuthType.setCellValueFactory(new PropertyValueFactory<WebServiceEndPoint, String>("authType"));
        colWebServiceEndPointDetail.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new WebServiceEndPointButtonCell(ButtonType.DETAIL);
                    }
                }
        );
        colWebServiceEndPointRemove.setCellFactory(
                new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(TableColumn<Disposer.Record, Boolean> p) {
                        return new WebServiceEndPointButtonCell(ButtonType.REMOVE);
                    }
                }
        );
        tableWebServiceEndPoint.setItems(observableWebServiceEndPointList);
    }

    private ArrayList<LocalizedString> convertLocalizationModel(ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString> localizedStrings) {
        ArrayList<LocalizedString> newLocalizedStrings = new ArrayList<>();
        for (com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString localizedString : localizedStrings) {
            LocalizedString newLocalizedString = new LocalizedString();
            newLocalizedString.setCode(localizedString.getCode());
            newLocalizedString.setValue(localizedString.getValue());
            newLocalizedStrings.add(newLocalizedString);
        }
        return newLocalizedStrings;
    }

    private ArrayList<LocalizedString> convertStatisticsLocalizedStringModel(ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString> localizedStrings) {
        ArrayList<LocalizedString> newLocalizedStrings = new ArrayList<>();
        for (com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString localizedString : localizedStrings) {
            LocalizedString newLocalizedString = new LocalizedString();
            newLocalizedString.setLanguageTag(localizedString.getLanguageTag());
            newLocalizedString.setValue(localizedString.getValue());
            newLocalizedStrings.add(newLocalizedString);
        }
        return newLocalizedStrings;
    }

    private ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString> convertLocalizationObject(ArrayList<LocalizedString> localizedStrings) {
        ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString> newLocalizedStrings = new ArrayList<>();
        for (LocalizedString localizedString : localizedStrings) {
            com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString newLocalizedString = new com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString();
            newLocalizedString.setCode(localizedString.getCode());
            newLocalizedString.setValue(localizedString.getValue());
            newLocalizedStrings.add(newLocalizedString);
        }
        return newLocalizedStrings;
    }

    private ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString> convertExplicitLocalizationObject(ArrayList<LocalizedString> localizedStrings) {
        ArrayList<com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString> newLocalizedStrings = new ArrayList<>();
        for (LocalizedString localizedString : localizedStrings) {
            com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString newLocalizedString = new com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString();
            newLocalizedString.setLanguageTag(localizedString.getLanguageTag());
            newLocalizedString.setValue(localizedString.getValue());
            newLocalizedStrings.add(newLocalizedString);
        }
        return newLocalizedStrings;
    }

    private ArrayList<AuthAgentInfo> convertProviderToAuthInfo(List<Provider> providers) {
        ArrayList<AuthAgentInfo> authAgentInfos = new ArrayList<>();
        if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_1.getLevel()) {
            for (Provider provider : providers) {
                if (Constants.PROVIDER_TYPE_AUTHN.equals(provider.getType())) {
                    AuthAgentInfo info = new AuthAgentInfo();
                    info.setUuid(UUID.fromString(provider.getUuid()));
                    info.setTitles(convertLocalizationObject(provider.getTitle()));
                    info.setDescriptions(convertLocalizationObject(provider.getDescription()));
                    info.setIntentUri(provider.getAuthenticationUrl());
                    info.setEnablePrePromptCheck(Boolean.parseBoolean(provider.getEnablePrePromptCheck()));
                    authAgentInfos.add(info);
                }
            }
        }
        return authAgentInfos;
    }

    private StatisticsAgentInfo convertProviderToStatisticInfo(List<Provider> providers) {
        StatisticsAgentInfo statisticsAgentInfo = null;
        if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_4.getLevel()) {
            for (Provider provider : providers) {
                if (Constants.PROVIDER_TYPE_STATISTICS.equals(provider.getType())) {
                    statisticsAgentInfo = new StatisticsAgentInfo();
                    statisticsAgentInfo.setUuid(UUID.fromString(provider.getUuid()));
                    statisticsAgentInfo.setTitles(convertExplicitLocalizationObject(provider.getTitle()));
                    statisticsAgentInfo.setDescriptions(convertExplicitLocalizationObject(provider.getDescription()));
                    statisticsAgentInfo.setAckRequiredForDelete(Boolean.parseBoolean(provider.getAckRequiredForDelete()));
                }
            }
        }
        return statisticsAgentInfo;
    }

    private HomeScreenMode convertProviderToHomeScreenMode(List<Provider> providers) {
        HomeScreenMode homeScreenMode = new HomeScreenMode();
        if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_1.getLevel()) {
            for (Provider provider : providers) {
                if (Constants.PROVIDER_TYPE_HOME_SCREEN.equals(provider.getType())) {
                    homeScreenMode.setEnableHomeScreenMode(Boolean.parseBoolean(provider.getEnableHomeScreenMode()));
                    homeScreenMode.setConfigOnInstall(Boolean.parseBoolean(provider.getConfigOnInstall()));
                    return homeScreenMode;
                }
            }
        }
        return null;
    }

    private List<AccessoryInfo> convertProviderToAccessoryInfo(List<Provider> providers) {
        List<AccessoryInfo> accessoryInfoList = new ArrayList<>();
        if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_2.getLevel()) {
            for (Provider provider : providers) {
                if (Constants.PROVIDER_TYPE_ACCESSORIES.equals(provider.getType())) {
                    AccessoryInfo info = new AccessoryInfo();
                    info.setProductId(provider.getProductId());
                    info.setVendorId(provider.getVendorId());
                    info.setRegistrationType(provider.getRegistrationType());
                    info.setSerialNumber(provider.getSerialNumber() == null ? "" : provider.getSerialNumber());
                    accessoryInfoList.add(info);
                }
            }
        }
        return accessoryInfoList;
    }

    private WebServiceInfo convertProviderToWebServiceInfo(List<Provider> providers) {
        WebServiceInfo webServiceInfo = null;
        if (Constants.getHPKVersion().getLevel() >= HPKVersion.HPK_1_4.getLevel()) {
            for (Provider provider : providers) {
                if (PROVIDER_TYPE_WEBSERVICES.equals(provider.getType())) {
                    webServiceInfo = new WebServiceInfo();
                    webServiceInfo.setUuid(UUID.fromString(provider.getUuid()));
                    webServiceInfo.setTitles(convertLocalizationObject(provider.getTitle()));
                    webServiceInfo.setDescriptions(convertLocalizationObject(provider.getDescription()));
                    JsonParser parser = new JsonParser();
                    JsonArray endPointArray = parser.parse(provider.getEndPoints()).getAsJsonArray();
                    for (JsonElement element : endPointArray) {
                        WebServiceEndPoint endPoint = new WebServiceEndPoint();
                        try {
                            endPoint.setMethodType(element.getAsJsonObject().get(PROPERTY_WEBSERVICE_METHOD).getAsString());
                            endPoint.setCategory(element.getAsJsonObject().get(PROPERTY_WEBSERVICE_CATEGORY).getAsString());
                            endPoint.setAbsolutePath(element.getAsJsonObject().get(PROPERTY_WEBSERVICE_ABSOLUTEPATH).getAsString());
                            // JALPINF-1094 Process hpk files that do not have an authType in previous version.
                            endPoint.setAuthType(element.getAsJsonObject().get(PROPERTY_WEBSERVICE_AUTHTYPE) != null ?
                                    element.getAsJsonObject().get(PROPERTY_WEBSERVICE_AUTHTYPE).getAsString() : WebServiceEndPoint.AuthType.NONE.getName());
                            webServiceInfo.addWebServiceEndPoint(endPoint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return webServiceInfo;
    }
}
