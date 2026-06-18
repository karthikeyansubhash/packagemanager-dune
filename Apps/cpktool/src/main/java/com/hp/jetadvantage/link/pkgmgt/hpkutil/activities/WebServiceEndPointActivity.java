package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.WebServiceEndPoint;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebServiceEndPointActivity extends CommonController {
    @FXML
    private ToggleGroup toggleMethod;
    @FXML
    private RadioButton radioGet;
    @FXML
    private RadioButton radioPut;
    @FXML
    private RadioButton radioPost;
    @FXML
    private RadioButton radioDelete;
    @FXML
    private TextField textCategory;
    @FXML
    private TextField textAbsolutePath;
    @FXML
    private ToggleGroup toggleAuthType;
    @FXML
    private RadioButton radioAuthTypeNone;
    @FXML
    private RadioButton radioAuthTypeXAuth;
    @FXML
    private RadioButton radioAuthTypeAdmin;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    private MainActivity mainWindow;
    private WebServiceEndPoint webServiceEndPoint;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init(){
        radioGet.selectedProperty().addListener(propertyChangeListener);
        radioPut.selectedProperty().addListener(propertyChangeListener);
        radioPost.selectedProperty().addListener(propertyChangeListener);
        radioDelete.selectedProperty().addListener(propertyChangeListener);
        textCategory.textProperty().addListener(propertyChangeListener);
        textAbsolutePath.textProperty().addListener(propertyChangeListener);
        radioAuthTypeNone.selectedProperty().addListener(propertyChangeListener);
        radioAuthTypeXAuth.selectedProperty().addListener(propertyChangeListener);
        radioAuthTypeAdmin.selectedProperty().addListener(propertyChangeListener);
        btnAdd.setDisable(true);
        btnAdd.setText(Constants.MESSAGE.getString("btn_add"));
    }

    public void setValues(WebServiceEndPoint info) {
        btnAdd.setDisable(true);
        btnAdd.setText(Constants.MESSAGE.getString("btn_add"));
        if (info != null) {
            this.webServiceEndPoint = info;
            if (this.webServiceEndPoint.getMethodType().equals(WebServiceEndPoint.MethodType.GET)) {
                toggleMethod.selectToggle(radioGet);
            } else if (this.webServiceEndPoint.getMethodType().equals(WebServiceEndPoint.MethodType.PUT)) {
                toggleMethod.selectToggle(radioPut);
            } else if (this.webServiceEndPoint.getMethodType().equals(WebServiceEndPoint.MethodType.POST)) {
                toggleMethod.selectToggle(radioPost);
            } else if (this.webServiceEndPoint.getMethodType().equals(WebServiceEndPoint.MethodType.DELETE)) {
                toggleMethod.selectToggle(radioDelete);
            }
            textCategory.setText(this.webServiceEndPoint.getCategory());
            textAbsolutePath.setText(this.webServiceEndPoint.getAbsolutePath());
            if (this.webServiceEndPoint.getAuthType().equals(WebServiceEndPoint.AuthType.NONE)) {
                toggleAuthType.selectToggle(radioAuthTypeNone);
            } else if (this.webServiceEndPoint.getAuthType().equals(WebServiceEndPoint.AuthType.XAUTH)) {
                toggleAuthType.selectToggle(radioAuthTypeXAuth);
            } else if (this.webServiceEndPoint.getAuthType().equals(WebServiceEndPoint.AuthType.ADMIN)) {
                toggleAuthType.selectToggle(radioAuthTypeAdmin);
            }
            btnAdd.setText(Constants.MESSAGE.getString("btn_save"));
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (!(radioGet.isSelected() || radioPut.isSelected() || radioPost.isSelected() || radioDelete.isSelected())
                || textCategory.getText().isEmpty() || textAbsolutePath.getText().isEmpty()
                || !(radioAuthTypeNone.isSelected() || radioAuthTypeXAuth.isSelected() || radioAuthTypeAdmin.isSelected())) {
            btnAdd.setDisable(true);
        } else {
            btnAdd.setDisable(false);
        }
    }

    @FXML
    protected void handleAdd(ActionEvent actionEvent) {
        boolean isNew = false;
        if (webServiceEndPoint == null) {
            webServiceEndPoint = new WebServiceEndPoint();
            isNew = true;
        }

        WebServiceEndPoint.MethodType methodType = WebServiceEndPoint.MethodType.GET;
        if (toggleMethod.getSelectedToggle().equals(radioGet)) {
            webServiceEndPoint.setMethodType(WebServiceEndPoint.MethodType.GET);
            methodType = WebServiceEndPoint.MethodType.GET;
        } else if (toggleMethod.getSelectedToggle().equals(radioPut)) {
            webServiceEndPoint.setMethodType(WebServiceEndPoint.MethodType.PUT);
            methodType = WebServiceEndPoint.MethodType.PUT;
        } else if (toggleMethod.getSelectedToggle().equals(radioPost)) {
            webServiceEndPoint.setMethodType(WebServiceEndPoint.MethodType.POST);
            methodType = WebServiceEndPoint.MethodType.POST;
        } else if (toggleMethod.getSelectedToggle().equals(radioDelete)) {
            webServiceEndPoint.setMethodType(WebServiceEndPoint.MethodType.DELETE);
            methodType = WebServiceEndPoint.MethodType.DELETE;
        } else {
            webServiceEndPoint = null;
            showMsg(State.WARNING,
                    Constants.MESSAGE.getString("msg_warning"),
                    Constants.MESSAGE.getString("msg_toggle_method_exception_webservice"),
                    Constants.MESSAGE.getString("msg_webservice_toggle_method_radio"));
            return;
        }
        String category = textCategory.getText();
        String absolutePath = textAbsolutePath.getText();
        WebServiceEndPoint.AuthType authType = WebServiceEndPoint.AuthType.NONE;
        if (toggleAuthType.getSelectedToggle().equals(radioAuthTypeNone)) {
            webServiceEndPoint.setAuthType(WebServiceEndPoint.AuthType.NONE);
            authType = WebServiceEndPoint.AuthType.NONE;
        } else if (toggleAuthType.getSelectedToggle().equals(radioAuthTypeXAuth)) {
            webServiceEndPoint.setAuthType(WebServiceEndPoint.AuthType.XAUTH);
            authType = WebServiceEndPoint.AuthType.XAUTH;
        } else if (toggleAuthType.getSelectedToggle().equals(radioAuthTypeAdmin)) {
            webServiceEndPoint.setAuthType(WebServiceEndPoint.AuthType.ADMIN);
            authType = WebServiceEndPoint.AuthType.ADMIN;
        } else {
            webServiceEndPoint = null;
            showMsg(State.WARNING,
                    Constants.MESSAGE.getString("msg_warning"),
                    Constants.MESSAGE.getString("msg_toggle_authtype_exception_webservice"),
                    Constants.MESSAGE.getString("msg_webservice_toggle_authtype_radio"));
            return;
        }

        List<WebServiceEndPoint> webServiceEndPoints = new ArrayList<> (mainWindow.getWebServiceEndPointList());
        if (!isNew) {
            webServiceEndPoints.remove(webServiceEndPoint);
        }
        for (WebServiceEndPoint exist : webServiceEndPoints) {
            if (Objects.equals(exist.getMethodType(), methodType)
                    && Objects.equals(exist.getCategory(), category)
                    && Objects.equals(exist.getAbsolutePath(), absolutePath)
                    && Objects.equals(exist.getAuthType(), authType)) {
                webServiceEndPoint = null;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_warning"),
                        Constants.MESSAGE.getString("msg_exist_webservice"),
                        Constants.MESSAGE.getString("msg_webservice_already_exist"));
                return;
            }
        }

        webServiceEndPoint.setCategory(category);
        webServiceEndPoint.setAbsolutePath(absolutePath);

        mainWindow.setWebServiceEndPoint(webServiceEndPoint, isNew);
        close(btnCancel);
    }

    @FXML
    protected void handleCancel(ActionEvent actionEvent) {
        close(btnCancel);
    }

    ChangeListener propertyChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            updateEnableState();
        }
    };
}
