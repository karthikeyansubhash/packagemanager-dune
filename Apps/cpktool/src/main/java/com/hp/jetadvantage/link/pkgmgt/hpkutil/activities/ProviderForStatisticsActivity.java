package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ExplicitLocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.StatisticsAgentInfo;
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

public class ProviderForStatisticsActivity extends CommonLocalizationController {
    @FXML
    private TextField textName;
    @FXML
    private TextField textDescription;
    @FXML
    private CheckBox checkCriticalSolution;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    private MainActivity mainWindow;
    private StatisticsAgentInfo info;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init(){
        textName.textProperty().addListener(textChangeListener);
        textDescription.textProperty().addListener(textChangeListener);
        btnAdd.setDisable(true);
        if(info == null) info = new StatisticsAgentInfo();
    }

    public void setValues(StatisticsAgentInfo info) {
        btnAdd.setDisable(true);
        if (info != null) {
            this.info = info;
            textName.setText(getENUSValue(info.getTitles()));
            textDescription.setText(getENUSValue(info.getDescriptions()));
            checkCriticalSolution.setSelected(info.isAckRequiredForDelete());
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (textName.getText().isEmpty() ||
                textDescription.getText().isEmpty()){
            btnAdd.setDisable(true);
        } else {
            btnAdd.setDisable(false);
        }
    }

    @FXML
    protected void handleMoreNameAction(ActionEvent actionEvent){
        ArrayList<ExplicitLocalizedString> values = null;
        if (info != null) {
            values = info.getTitles();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_name"),
                convertExplicitToLocalized(values), LocalizationActivity.Type.TITLE);
    }

    @FXML
    protected void handleMoreDescriptionAction(ActionEvent actionEvent){
        ArrayList<ExplicitLocalizedString> values = null;
        if (info != null) {
            values = info.getDescriptions();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_description"),
                convertExplicitToLocalized(values), LocalizationActivity.Type.DESCRIPTION);
    }

    @FXML
    protected void handleAdd(ActionEvent actionEvent) {
        if(info.getUuid() == null) {
            info.setUuid(UUID.randomUUID());
        }
        info.addTitle(getRequiredTitle());
        info.addDescription(getRequiredDescription());
        info.setAckRequiredForDelete(checkCriticalSolution.isSelected());

        mainWindow.setStatisticsAgentInfo(info);
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

    private String getENUSValue(ArrayList<ExplicitLocalizedString> values) {
        try {
            for (ExplicitLocalizedString value : values) {
                if ("en-US".equalsIgnoreCase(value.getLanguageTag())) {
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

    private ArrayList<LocalizedString> convertExplicitToLocalized(ArrayList<ExplicitLocalizedString> values){
        if(values == null){
            return null;
        }
        ArrayList<LocalizedString> localizedStringArrayList = new ArrayList<LocalizedString>();
        for(ExplicitLocalizedString value : values){
            LocalizedString localizedString = new LocalizedString();
            localizedString.setCode(value.getLanguageTag());
            localizedString.setValue(value.getValue());
            localizedStringArrayList.add(localizedString);
        }
        return localizedStringArrayList;
    }

    private ArrayList<ExplicitLocalizedString> convertLocalizedToExplicit(ArrayList<LocalizedString> values){
        ArrayList<ExplicitLocalizedString> explicitLocalizedStringArrayList = new ArrayList<ExplicitLocalizedString>();
        for(LocalizedString value : values){
            ExplicitLocalizedString explicitLocalizedString = new ExplicitLocalizedString();
            explicitLocalizedString.setLanguageTag(value.getCode());
            explicitLocalizedString.setValue(value.getValue());
            explicitLocalizedStringArrayList.add(explicitLocalizedString);
        }
        return explicitLocalizedStringArrayList;
    }

    @Override
    public void onLocalizationActivityResult(LocalizationActivity.Type type, ArrayList<LocalizedString> values) {
        if (values != null) {
            if (type.equals(LocalizationActivity.Type.TITLE)) {
                info.setTitles(convertLocalizedToExplicit(values));
            } else if (type.equals(LocalizationActivity.Type.DESCRIPTION)) {
                info.setDescriptions(convertLocalizedToExplicit(values));
            }
        }
    }

    private ExplicitLocalizedString getRequiredTitle() {
        ExplicitLocalizedString title = new ExplicitLocalizedString();
        title.setLanguageTag(Constants.EN_US);
        title.setValue((textName.getText().isEmpty())? null : textName.getText());
        return title;
    }

    private ExplicitLocalizedString getRequiredDescription() {
        ExplicitLocalizedString description = new ExplicitLocalizedString();
        description.setLanguageTag(Constants.EN_US);
        description.setValue((textDescription.getText().isEmpty())? null : textDescription.getText());
        return description;
    }
}
