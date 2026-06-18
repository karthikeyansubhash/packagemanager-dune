package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocalizationActivity extends CommonController {

    @FXML
    VBox localizationBox;

    private ArrayList<LocalizedString> values;
    private CommonLocalizationController buttonManagerWindow;
    private Type type;

    public enum Type {
        TITLE,
        DESCRIPTION
    }

    public void setValue(CommonLocalizationController window, ArrayList<LocalizedString> values, Type type) {
        createLocalizationLayout();
        createButtonLayout();
        this.values = values;
        this.buttonManagerWindow = window;
        this.type = type;
        setLocalizationText(values);
    }

    public void setResult() {
        saveLocalizationText();
        buttonManagerWindow.onLocalizationActivityResult(type, values);
    }

    private void setLocalizationText(ArrayList<LocalizedString> values) {
        if (values != null) {
            ObservableList<Node> hboxList = localizationBox.getChildren();
            for (Node hbox : hboxList) {
                for (LocalizedString value : values) {
                    for (Node component : ((HBox) hbox).getChildren()) {
                        if (component.getId() != null && component.getId().equals(value.getCode())) {
                            ((TextField) component).setText(value.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void saveLocalizationText() {
        values = new ArrayList<>();
        ObservableList<Node> hboxList = localizationBox.getChildren();
        for (Node hbox : hboxList) {
            for (Node component : ((HBox) hbox).getChildren()) {
                if (component.getId() != null) {
                    TextField textField = (TextField) component;
                    if (textField.getText() != null && !textField.getText().isEmpty()) {
                        LocalizedString localizedString = new LocalizedString(textField.getId(), textField.getText());
                        values.add(localizedString);
                    }
                }
            }
        }
    }

    private List<String> getLocalizations() {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> localizationList = new ArrayList<>();
        InputStream inputStream = getClass().getResourceAsStream("/localization.txt");

        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                localizationList = new ArrayList<>(Arrays.asList(stringBuilder.toString().split(",")));
                for(Iterator<String> iter = localizationList.listIterator(); iter.hasNext(); ){
                    if(iter.next().equals(Constants.EN_US)){
                        iter.remove();
                    }
                }
            } catch (IOException ex) {

            } finally {
                if (bufferedReader != null) {
                    try { bufferedReader.close(); } catch (IOException e) { }
                }
            }
        }
        return localizationList;
    }

    private void createButtonLayout() {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setAlignment(Pos.CENTER);
        VBox invisibleBox = new VBox();
        invisibleBox.setPadding(new Insets(10, 10, 10, 10));
        final Button okButton = new Button();
        final Button cancelButton = new Button();
        okButton.setPrefWidth(100.0);
        okButton.setText(Constants.MESSAGE.getString("btn_ok"));
        cancelButton.setPrefWidth(100.0);
        cancelButton.getStyleClass().add("buttonWhite");
        cancelButton.setText(Constants.MESSAGE.getString("btn_cancel"));
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setResult();
                close(okButton);
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close(cancelButton);
            }
        });
        hBox.getChildren().addAll(okButton, invisibleBox, cancelButton);
        localizationBox.getChildren().add(hBox);
    }

    private void createLocalizationLayout() {

        List<String> locales = getLocalizations();
        for (int i = 0; i < locales.size(); i += 2) {
            HBox hBox = new HBox();
            hBox.setPadding(new Insets(0, 10, 5, 10));

            Label firstLabel = getNewLabel();
            Label secondLabel = getNewLabel();
            TextField firstText = getNewTextField();
            TextField secondText = getNewTextField();

            firstLabel.setText(locales.get(i));
            firstText.setId(locales.get(i));

            if (locales.size() <= i + 1) {
                hBox.getChildren().addAll(firstLabel, firstText);
            } else {
                secondLabel.setText(locales.get(i + 1));
                secondText.setId(locales.get(i + 1));
                hBox.getChildren().addAll(firstLabel, firstText, secondLabel, secondText);
            }
            localizationBox.getChildren().add(hBox);
        }
    }

    private Label getNewLabel() {
        Label newLabel = new Label();
        newLabel.setPrefHeight(24.0);
        newLabel.setPrefWidth(80.0);
        newLabel.setTextAlignment(TextAlignment.RIGHT);
        newLabel.setAlignment(Pos.CENTER);
        return newLabel;
    }

    private TextField getNewTextField() {
        TextField newTextField = new TextField();
        newTextField.setPrefHeight(24.0);
        newTextField.setPrefWidth(220.0);
        return newTextField;
    }
}
