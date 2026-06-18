package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.AccessoryInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProviderForAccessoryActivity extends CommonController {
    @FXML
    private CheckBox checkOwned;
    @FXML
    private CheckBox checkShared;
    @FXML
    private TextField textVendorId;
    @FXML
    private TextField textProductId;
    @FXML
    private CheckBox checkSerialNull;
    @FXML
    private TextField textSerialNumber;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    private MainActivity mainWindow;
    private AccessoryInfo info;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init(){
        textVendorId.textProperty().addListener(textChangeListener);
        textProductId.textProperty().addListener(textChangeListener);
        btnAdd.setDisable(true);
        btnAdd.setText(Constants.MESSAGE.getString("btn_add"));
    }

    public void setValues(AccessoryInfo info) {
        btnAdd.setDisable(true);
        btnAdd.setText(Constants.MESSAGE.getString("btn_add"));
        if (info != null) {
            this.info = info;
            if (AccessoryInfo.RegistrationType.OWNED == info.getRegistrationType()) {
                checkOwned.setSelected(true);
                checkShared.setSelected(false);
            } else {
                checkOwned.setSelected(false);
                checkShared.setSelected(true);
            }
            textVendorId.setText(info.getVendorId());
            textProductId.setText(info.getProductId());
            if ("NULL".equalsIgnoreCase(info.getSerialNumber())) {
                checkSerialNull.setSelected(true);
                textSerialNumber.setDisable(true);
            } else {
                textSerialNumber.setText(info.getSerialNumber());
            }
            btnAdd.setText(Constants.MESSAGE.getString("btn_save"));
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (textProductId.getText().isEmpty() ||
                textVendorId.getText().isEmpty()){
            btnAdd.setDisable(true);
        } else {
            btnAdd.setDisable(false);
        }
    }

    @FXML
    protected void handleCheckOwned(ActionEvent actionEvent){
        checkOwned.setSelected(true);
        checkShared.setSelected(false);
    }

    @FXML
    protected void handleCheckShared(ActionEvent actionEvent){
        checkOwned.setSelected(false);
        checkShared.setSelected(true);
    }

    @FXML
    protected void handleCheckSerialNull(ActionEvent actionEvent){
        if (checkSerialNull.isSelected()) {
            textSerialNumber.setDisable(true);
        } else {
            textSerialNumber.setDisable(false);
        }
    }

    @FXML
    protected void handleAdd(ActionEvent actionEvent) {
        boolean isNew = false;
        if (info == null) {
            info = new AccessoryInfo();
            isNew = true;
        }

        String vendorId = textVendorId.getText();
        String productId = textProductId.getText();
        String serialNum;
        if (checkSerialNull.isSelected()) {
            serialNum = "NULL";
        } else {
            serialNum = textSerialNumber.getText();
        }

        List<AccessoryInfo> accessoryInfos = new ArrayList<> (mainWindow.getAccessoryInfos());
        if (!isNew) {
            accessoryInfos.remove(info);
        }
        for (AccessoryInfo exist : accessoryInfos) {
            if (Objects.equals(exist.getVendorId(), vendorId)
                    && Objects.equals(exist.getProductId(), productId)
                    && Objects.equals(exist.getSerialNumber(), serialNum)) {
                info = null;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_warning"),
                        Constants.MESSAGE.getString("msg_exist_accessory"),
                        Constants.MESSAGE.getString("msg_accessory_already_exist"));
                return;
            }
        }

        if (checkOwned.isSelected()) {
            info.setRegistrationType(AccessoryInfo.RegistrationType.OWNED);
        } else if (checkShared.isSelected()) {
            info.setRegistrationType(AccessoryInfo.RegistrationType.SHARED);
        }
        info.setVendorId(vendorId);
        info.setProductId(productId);
        info.setSerialNumber(serialNum);

        mainWindow.setAccessoryInfo(info, isNew);
        close(btnCancel);
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
}
