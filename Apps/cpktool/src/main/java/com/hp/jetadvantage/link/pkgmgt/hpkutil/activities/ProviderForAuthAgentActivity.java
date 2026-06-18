package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.AuthAgentInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class ProviderForAuthAgentActivity extends CommonLocalizationController {
    @FXML
    private TextField textName;
    @FXML
    private TextField textDescription;
    @FXML
    private TextField textAuthAgentActivity;
    @FXML
    private CheckBox checkBoxPrePrompt;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    private MainActivity mainWindow;
    private AuthAgentInfo info;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init(){
        textName.textProperty().addListener(textChangeListener);
        textDescription.textProperty().addListener(textChangeListener);
        textAuthAgentActivity.textProperty().addListener(textChangeListener);
        btnAdd.setDisable(true);
        if(info == null) info = new AuthAgentInfo();
    }

    public void setValues(AuthAgentInfo info) {
        btnAdd.setDisable(true);
        if (info != null) {
            this.info = info;
            textName.setText(getENUSValue(info.getTitles()));
            textDescription.setText(getENUSValue(info.getDescriptions()));
            textAuthAgentActivity.setText(info.getIntentUri());
            checkBoxPrePrompt.setSelected(info.isEnablePrePromptCheck());
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (textName.getText().isEmpty() ||
                textDescription.getText().isEmpty() ||
                textAuthAgentActivity.getText().isEmpty()){
            btnAdd.setDisable(true);
        } else {
            btnAdd.setDisable(false);
        }
    }

    @FXML
    protected void handleMoreNameAction(ActionEvent actionEvent){
        ArrayList<LocalizedString> values = null;
        if (info != null) {
            values = info.getTitles();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_name"),
                values, LocalizationActivity.Type.TITLE);
    }

    @FXML
    protected void handleMoreDescriptionAction(ActionEvent actionEvent){
        ArrayList<LocalizedString> values = null;
        if (info != null) {
            values = info.getDescriptions();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_description"),
                values, LocalizationActivity.Type.DESCRIPTION);
    }

    @FXML
    protected void handleCheckBoxAction(ActionEvent actionEvent){
    }

    @FXML
    protected void handleAdd(ActionEvent actionEvent) {
        if(info.getUuid() == null) {
            info.setUuid(UUID.randomUUID());
        }
        info.addTitle(getRequiredTitle());
        info.addDescription(getRequiredDescription());
        info.setIntentUri(textAuthAgentActivity.getText());
        info.setEnablePrePromptCheck(checkBoxPrePrompt.isSelected());

        mainWindow.setAuthAgentList(info);
        close(btnAdd);
    }

    @FXML
    protected void handleCancel(ActionEvent actionEvent) {
        close(btnCancel);
    }

    ChangeListener textChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            updateEnableState();
        }
    };

    private String getENUSValue(ArrayList<LocalizedString> values) {
        try {
            for (LocalizedString value : values) {
                if ("en-US".equalsIgnoreCase(value.getCode())) {
                    return value.getValue();
                }
            }
        } catch (Exception e) {}
        return "";
    }

    private void showLocalizationActivity(String title, ArrayList<LocalizedString> values, LocalizationActivity.Type type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/localization_activity.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.NONE);
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((LocalizationActivity) loader.getController()).setValue(this, values, type);
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onLocalizationActivityResult(LocalizationActivity.Type type, ArrayList<LocalizedString> values) {
        if (values != null) {
            if (type.equals(LocalizationActivity.Type.TITLE)) {
                info.setTitles(values);
            } else if (type.equals(LocalizationActivity.Type.DESCRIPTION)) {
                info.setDescriptions(values);
            }
        }
    }

    private LocalizedString getRequiredTitle() {
        LocalizedString title = new LocalizedString();
        title.setCode(Constants.EN_US);
        title.setValue((textName.getText().isEmpty())? null : textName.getText());
        return title;
    }

    private LocalizedString getRequiredDescription() {
        LocalizedString description = new LocalizedString();
        description.setCode(Constants.EN_US);
        description.setValue((textDescription.getText().isEmpty())? null : textDescription.getText());
        return description;
    }
}
