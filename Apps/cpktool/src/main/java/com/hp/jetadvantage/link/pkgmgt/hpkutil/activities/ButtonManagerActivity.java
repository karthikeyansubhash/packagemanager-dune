package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.BrowserTarget;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LookAndFeelSpecificIcon;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.NetworkCredentials;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.WebApplication;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.UtilsFX;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class ButtonManagerActivity extends CommonLocalizationController {
    final static int IMAGE_MARGIN = 2;

    @FXML
    private TextField textUuid;
    @FXML
    private TextField textName;
    @FXML
    private TextField textDescription;
    @FXML
    private TextField textUri;
    @FXML
    private TextField textDomain;
    @FXML
    private TextField textUserName;
    @FXML
    private TextField textPassword;
    @FXML
    private Button btnWJ2dot7Icon;
    @FXML
    private Button btnWJ1dot5Icon;
    @FXML
    private Button btnOmni90Icon;
    @FXML
    private Button btnOmni140Icon;
    @FXML
    private Button btnOmni179Icon;
    @FXML
    private ImageView btnWJ2dot7IconRemove;
    @FXML
    private ImageView btnWJ1dot5IconRemove;
    @FXML
    private ImageView btnOmni90IconRemove;
    @FXML
    private ImageView btnOmni140IconRemove;
    @FXML
    private ImageView btnOmni179IconRemove;
    @FXML
    private TextField textInitReqQueryStr;
    @FXML
    private Button btnInitReqQueryStr;
    @FXML
    private TextField textRequestedPosition;

    private ButtonInfo buttonInfo;

    public ButtonManagerActivity() { buttonInfo = new ButtonInfo(); }

    private void setIcons(Button btnIcon, File selectedFile) {
        if (selectedFile != null) {
            try {
                BufferedImage image = ImageIO.read(selectedFile);

                if (checkIconValidity(btnIcon, image)) {
                    setIcon(btnIcon, new ImageView(selectedFile.toURI().toString()));
                }

            } catch (Exception e) {
                showMsg(State.ERROR,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("msg_failed_to_open"),
                        e.getMessage());
            }
        }
    }

    private boolean checkIconValidity(Button btnIcon, BufferedImage image) {
        boolean result = true;

        int width = image.getWidth();
        int height = image.getHeight();

        if (btnIcon.getId().equalsIgnoreCase(Constants.BTN_WJ27)) {
            if (!(width <= Constants.ICON_LEN_WJ27 && height <= Constants.ICON_LEN_WJ27)) {
                result = false;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("failed_to_check_size_46"), "");
            }
        } else if (btnIcon.getId().equalsIgnoreCase(Constants.BTN_WJ15)) {
            if (!(width <= Constants.ICON_LEN_WJ15 && height <= Constants.ICON_LEN_WJ15)) {
                result = false;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("failed_to_check_size_66"), "");
            }
        } else if (btnIcon.getId().equalsIgnoreCase(Constants.BTN_OMNI90)) {
            if (!(width <= Constants.ICON_LEN_OMNI90 && height <= Constants.ICON_LEN_OMNI90)) {
                result = false;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("failed_to_check_size_90"), "");
            }
        } else if (btnIcon.getId().equalsIgnoreCase(Constants.BTN_OMNI140)) {
            if (!(width <= Constants.ICON_LEN_OMNI140 && height <= Constants.ICON_LEN_OMNI140)) {
                result = false;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("failed_to_check_size_140"), "");
            }
        } else if (btnIcon.getId().equalsIgnoreCase(Constants.BTN_OMNI179)) {
            if (!(width <= Constants.ICON_LEN_OMNI179 && height <= Constants.ICON_LEN_OMNI179)) {
                result = false;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_error"),
                        Constants.MESSAGE.getString("failed_to_check_size_179"), "");
            }
        }

        return result;
    }

    private void setIcon(Button btnIcon, ImageView icon) {
        icon.setFitHeight(btnIcon.getPrefHeight() - IMAGE_MARGIN);
        icon.setFitWidth(btnIcon.getPrefWidth() - IMAGE_MARGIN);

        btnIcon.setGraphic(icon);
        btnIcon.setText(null);

        String id = btnIcon.getId();
        if (id.contains("btnWJ1dot5Icon")) {
            btnWJ1dot5IconRemove.setVisible(true);
        } else if (id.contains("btnWJ2dot7Icon")) {
            btnWJ2dot7IconRemove.setVisible(true);
        } else if (id.contains("btnOmni90Icon")) {
            btnOmni90IconRemove.setVisible(true);
        } else if (id.contains("btnOmni140Icon")) {
            btnOmni140IconRemove.setVisible(true);
        } else if (id.contains("btnOmni179Icon")) {
            btnOmni179IconRemove.setVisible(true);
        }
    }

    public void clear() {
        buttonInfo = new ButtonInfo();
        textUuid.setText("");
        textName.setText("");
        textDescription.setText("");
        textUri.setText("");
        textDomain.setText("");
        textUserName.setText("");
        textPassword.setText("");
        textInitReqQueryStr.setText("");
        textRequestedPosition.setText("1");

        btnWJ2dot7Icon.setText("+");
        btnWJ1dot5Icon.setText("+");
        btnOmni90Icon.setText("+");
        btnOmni140Icon.setText("+");
        btnOmni179Icon.setText("+");

        btnWJ2dot7Icon.setGraphic(null);
        btnWJ1dot5Icon.setGraphic(null);
        btnOmni90Icon.setGraphic(null);
        btnOmni140Icon.setGraphic(null);
        btnOmni179Icon.setGraphic(null);

        btnWJ1dot5IconRemove.setVisible(false);
        btnWJ2dot7IconRemove.setVisible(false);
        btnOmni90IconRemove.setVisible(false);
        btnOmni140IconRemove.setVisible(false);
        btnOmni179IconRemove.setVisible(false);
    }

    public void setValues(ButtonInfo buttonInfo) {
        clear();
        if(buttonInfo == null) return;

        this.buttonInfo = buttonInfo;
        textUuid.setText(buttonInfo.getUuid().toString());
        textName.setText(buttonInfo.getName());
        String description = "";
        for (LocalizedString locale : buttonInfo.getDescription()) {
            if (Constants.EN_US.equals(locale.getCode())) {
                description = locale.getValue();
                break;
            }
        }
        textDescription.setText(description);
        textRequestedPosition.setText(buttonInfo.getRequestedPosition());
        textUri.setText(buttonInfo.getBrowserTarget().getWebApplication().getUri());

        ArrayList<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons = buttonInfo.getLookAndFeelSpecificIcons();

        if (lookAndFeelSpecificIcons != null) {
            String icon;
            String lookAndFeel;
            for (int i = 0; i < lookAndFeelSpecificIcons.size(); i++) {
                icon = lookAndFeelSpecificIcons.get(i).getIcon();
                lookAndFeel = lookAndFeelSpecificIcons.get(i).getLookAndFeel();

                if (lookAndFeel.equalsIgnoreCase(Constants.WJ1DOT5)) {
                    setIcon(btnWJ1dot5Icon, new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(icon)))));
                } else if (lookAndFeel.equalsIgnoreCase(Constants.WJ2DOT7)) {
                    setIcon(btnWJ2dot7Icon, new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(icon)))));
                } else if (lookAndFeel.equalsIgnoreCase(Constants.OMNI90)) {
                    setIcon(btnOmni90Icon, new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(icon)))));
                } else if (lookAndFeel.equalsIgnoreCase(Constants.OMNI140)) {
                    setIcon(btnOmni140Icon, new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(icon)))));
                } else if (lookAndFeel.equalsIgnoreCase(Constants.OMNI179)) {
                    setIcon(btnOmni179Icon, new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(icon)))));
                }
            }
        }

        NetworkCredentials networkCredentials = buttonInfo.getBrowserTarget().getWebApplication().getNetworkCredentials();
        if (networkCredentials != null) {
            if (networkCredentials.getDomain() != null) {
                textDomain.setText(networkCredentials.getDomain());
            }

            if (networkCredentials.getUserName() != null) {
                textUserName.setText(networkCredentials.getUserName());
            }

            if (networkCredentials.getPassword() != null) {
                textPassword.setText(networkCredentials.getPassword());
            }
        }

        if (buttonInfo.getBrowserTarget().getInitialPostQueryFormatString() != null) {
            textInitReqQueryStr.setText(buttonInfo.getBrowserTarget().getInitialPostQueryFormatString());
        }
    }

    public void setUUID(String uuid) {
        textUuid.setText(uuid);
    }

    public ButtonInfo getButtonInfo() {
        try {
            if (Utils.isValidUuid(textUuid.getText())) {
                buttonInfo.setUuid(UUID.fromString(textUuid.getText()));
            } else {
                throw new IllegalArgumentException("Invalid UUID string: " + textUuid.getText());
            }

            buttonInfo.setName((textName.getText().isEmpty())? null : textName.getText());
            buttonInfo.addTitle(getRequiredTitle());
            buttonInfo.addDescription(getRequiredDescription());
            buttonInfo.setRequestedPosition((textRequestedPosition.getText().isEmpty())? null : textRequestedPosition.getText());
            buttonInfo.setBrowserTarget(getBrowserTarget());

            ArrayList<LookAndFeelSpecificIcon> lookAndFeelSpecificIconArrayList = getLookAndFeelSpecificIconList();
/*            for (LookAndFeelSpecificIcon lookAndFeelSpecificIcon : lookAndFeelSpecificIconArrayList) {
                if(lookAndFeelSpecificIcon.getIcon().length() > Constants.ICON_MAXIMUM_SIZE) {
                    throw new Exception("The size of " + lookAndFeelSpecificIcon.getLookAndFeel() + " is too large.");
                }
            }*/
            if (lookAndFeelSpecificIconArrayList != null) {
                buttonInfo.setLookAndFeelSpecificIcons(lookAndFeelSpecificIconArrayList);
            }

            Utils.isVerifyFormat(buttonInfo, getClass().getResource(Constants.HPK_BUTTON_XSD_FILE_PATH));

            return buttonInfo;

        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_fail_save"),
                    e.getMessage());
        }
        return null;
    }

    public void createBtnXmlFile(ButtonInfo buttonInfo, File selectedFile) {

        try {
            Serializer serializer = new Persister(new AnnotationStrategy(),
                    new Format("<?xml version=\"1.0\"?>"));
            serializer.write(buttonInfo, selectedFile);

        } catch (Exception e) {
            showMsg(State.ERROR,
                    Constants.MESSAGE.getString("msg_error"),
                    Constants.MESSAGE.getString("msg_fail_save"),
                    e.getMessage());
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

    @Override
    public void onLocalizationActivityResult(LocalizationActivity.Type type, ArrayList<LocalizedString> values) {
        if (values != null) {
            if (type.equals(LocalizationActivity.Type.TITLE)) {
                buttonInfo.setTitle(values);
            } else if (type.equals(LocalizationActivity.Type.DESCRIPTION)) {
                buttonInfo.setDescription(values);
            }
        }
    }

    private BrowserTarget getBrowserTarget() {
        WebApplication webApplication = new WebApplication();
        webApplication.setBinding(Constants.MESSAGE.getString("plain"));

        if (!textUri.getText().isEmpty()) webApplication.setUri(textUri.getText());

        NetworkCredentials networkCredentials = new NetworkCredentials();
        if (!textUserName.getText().isEmpty()) {
            networkCredentials.setUserName(textUserName.getText());
        }
        if (!textPassword.getText().isEmpty()) {
            networkCredentials.setPassword(textPassword.getText());
        }
        if (!textDomain.getText().isEmpty()) {
            networkCredentials.setDomain(textDomain.getText());
        }
        if (!networkCredentials.isEmpty())
            webApplication.setNetworkCredentials(networkCredentials);

        BrowserTarget browserTarget = new BrowserTarget();
        if (!textInitReqQueryStr.getText().isEmpty()) {
            browserTarget.setInitialPostQueryFormatString(textInitReqQueryStr.getText());
        }
        browserTarget.setWebApplication(webApplication);

        return browserTarget;
    }

    private ArrayList<LookAndFeelSpecificIcon> getLookAndFeelSpecificIconList() {
        ArrayList<LookAndFeelSpecificIcon> iconArr = new ArrayList<>();

        if (btnWJ1dot5Icon.getGraphic() != null) {
            LookAndFeelSpecificIcon lookAndFeelSpecificIcon = new LookAndFeelSpecificIcon();
            lookAndFeelSpecificIcon.setIcon(UtilsFX.encodeImageToBase64Binary(((ImageView) btnWJ1dot5Icon.getGraphic()).getImage()));
            lookAndFeelSpecificIcon.setLookAndFeel(Constants.WJ1DOT5);
            iconArr.add(lookAndFeelSpecificIcon);
        }
        if (btnWJ2dot7Icon.getGraphic() != null) {
            LookAndFeelSpecificIcon lookAndFeelSpecificIcon = new LookAndFeelSpecificIcon();
            lookAndFeelSpecificIcon.setIcon(UtilsFX.encodeImageToBase64Binary(((ImageView) btnWJ2dot7Icon.getGraphic()).getImage()));
            lookAndFeelSpecificIcon.setLookAndFeel(Constants.WJ2DOT7);
            iconArr.add(lookAndFeelSpecificIcon);
        }
        if (btnOmni90Icon.getGraphic() != null) {
            LookAndFeelSpecificIcon lookAndFeelSpecificIcon = new LookAndFeelSpecificIcon();
            lookAndFeelSpecificIcon.setIcon(UtilsFX.encodeImageToBase64Binary(((ImageView) btnOmni90Icon.getGraphic()).getImage()));
            lookAndFeelSpecificIcon.setLookAndFeel(Constants.OMNI90);
            iconArr.add(lookAndFeelSpecificIcon);
        }
        if (btnOmni140Icon.getGraphic() != null) {
            LookAndFeelSpecificIcon lookAndFeelSpecificIcon = new LookAndFeelSpecificIcon();
            lookAndFeelSpecificIcon.setIcon(UtilsFX.encodeImageToBase64Binary(((ImageView) btnOmni140Icon.getGraphic()).getImage()));
            lookAndFeelSpecificIcon.setLookAndFeel(Constants.OMNI140);
            iconArr.add(lookAndFeelSpecificIcon);
        }
        if (btnOmni179Icon.getGraphic() != null) {
            LookAndFeelSpecificIcon lookAndFeelSpecificIcon = new LookAndFeelSpecificIcon();
            lookAndFeelSpecificIcon.setIcon(UtilsFX.encodeImageToBase64Binary(((ImageView) btnOmni179Icon.getGraphic()).getImage()));
            lookAndFeelSpecificIcon.setLookAndFeel(Constants.OMNI179);
            iconArr.add(lookAndFeelSpecificIcon);
        }

        return iconArr;
    }

    protected void setMacros(String macro, boolean isSelected) {
        if (isSelected) {
            textInitReqQueryStr.setText(textInitReqQueryStr.getText() + macro);
        } else {
            textInitReqQueryStr.setText(textInitReqQueryStr.getText().replace(macro, ""));
        }

        textInitReqQueryStr.positionCaret(textInitReqQueryStr.getLength());
    }

    @FXML
    protected void handleIconRemove(MouseEvent event) {
        ImageView btnImage = (ImageView) event.getSource();
        String id = btnImage.getId();
        if (id.contains("btnWJ1dot5Icon")) {
            btnWJ1dot5Icon.setGraphic(null);
            btnWJ1dot5Icon.setText("+");
            btnWJ1dot5IconRemove.setVisible(false);
        } else if (id.contains("btnWJ2dot7Icon")) {
            btnWJ2dot7Icon.setGraphic(null);
            btnWJ2dot7Icon.setText("+");
            btnWJ2dot7IconRemove.setVisible(false);
        } else if (id.contains("btnOmni90Icon")) {
            btnOmni90Icon.setGraphic(null);
            btnOmni90Icon.setText("+");
            btnOmni90IconRemove.setVisible(false);
        } else if (id.contains("btnOmni140Icon")) {
            btnOmni140Icon.setGraphic(null);
            btnOmni140Icon.setText("+");
            btnOmni140IconRemove.setVisible(false);
        } else if (id.contains("btnOmni179Icon")) {
            btnOmni179Icon.setGraphic(null);
            btnOmni179Icon.setText("+");
            btnOmni179IconRemove.setVisible(false);
        }
    }

    @FXML
    protected void handleEditIconButtonAction(ActionEvent actionEvent) {
        File selectedFile = selectFile(stage, ActionType.IMPORT_BUTTON_IMAGE, FileChooserMode.OPEN);
        if (selectedFile != null) {
            Button btnIcon = (Button) actionEvent.getSource();
            setIcons(btnIcon, selectedFile);
        }
    }

    @FXML
    protected void handleInitReqQueryStrButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/macros_activity.fxml"));
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            VBox newWindow = (VBox) loader.load();
            Stage stage = new Stage();
            stage.setTitle(Constants.MESSAGE.getString("macros_activity_title_macros"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnInitReqQueryStr.getScene().getWindow());
            Scene scene = new Scene(newWindow);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            ((MacrosActivity) loader.getController()).setMainWindow(this);
            if (!textInitReqQueryStr.getText().isEmpty()) {
                ((MacrosActivity) loader.getController()).setSelect(textInitReqQueryStr.getText());
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleMoreNameAction() {
        ArrayList<LocalizedString> values = null;
        if (buttonInfo != null) {
            values = buttonInfo.getTitle();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_name"),
                values, LocalizationActivity.Type.TITLE);
    }

    @FXML
    protected void handleMoreDescriptionAction() {
        ArrayList<LocalizedString> values = null;
        if (buttonInfo != null) {
            values = buttonInfo.getDescription();
        }
        showLocalizationActivity(
                Constants.MESSAGE.getString("menu_description"),
                values, LocalizationActivity.Type.DESCRIPTION);
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
}
