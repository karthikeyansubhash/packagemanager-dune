package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.SubActivityInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubActivity extends CommonController {
    @FXML
    private TextField textUuid;
    @FXML
    private TextField textPlatformId;
    @FXML
    private Button btnAddSubActivity;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnCancel;

    private MainActivity mainWindow;
    private SubActivityInfo info;

    public void setMainWindow(MainActivity mainWindow) {
        this.mainWindow = mainWindow;
        init();
    }

    private void init(){
        textUuid.textProperty().addListener(textChangeListener);
        textPlatformId.textProperty().addListener(textChangeListener);
        btnAddSubActivity.setDisable(true);
        btnAddSubActivity.setText(Constants.MESSAGE.getString("btn_add"));
    }

    public void setValues(SubActivityInfo info) {
        btnAddSubActivity.setDisable(true);
        btnAddSubActivity.setText(Constants.MESSAGE.getString("btn_add"));
        if (info != null) {
            this.info = info;
            textUuid.setText(info.getUuid().toString());
            textPlatformId.setText(info.getPlatformId());
            btnDelete.setDisable(false);
            btnAddSubActivity.setText(Constants.MESSAGE.getString("btn_save"));
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (textUuid.getText().isEmpty() ||
                textPlatformId.getText().isEmpty()){
            btnAddSubActivity.setDisable(true);
        } else {
            btnAddSubActivity.setDisable(false);
        }
    }

    @FXML
    protected void handleUuidGenerateButtonAction(ActionEvent actionEvent){
        textUuid.setText(UUID.randomUUID().toString());
    }

    @FXML
    protected void handleAddSubActivity(ActionEvent actionEvent) {
        boolean isNew = false;
        if (info == null) {
            info = new SubActivityInfo();
            isNew = true;
        }

        List<SubActivityInfo> subActivityInfos = new ArrayList<> (mainWindow.getSubActivities());
        if (!isNew) {
            subActivityInfos.remove(info);
        }
        for (SubActivityInfo exist : subActivityInfos) {
            if (exist.getPlatformId().equals(textPlatformId.getText())
                    || exist.getUuid().toString().equals(textUuid.getText())) {
                info = null;
                showMsg(State.WARNING,
                        Constants.MESSAGE.getString("msg_warning"),
                        Constants.MESSAGE.getString("msg_exist_sub_activity"),
                        Constants.MESSAGE.getString("msg_activity_already_exist"));
                return;
            }
        }

        info.setUuid(UUID.fromString(textUuid.getText()));
        info.setPlatformId(textPlatformId.getText());

        mainWindow.setSubActivity(info, isNew);
        close(btnCancel);
    }

    @FXML
    protected void handleDeleteSubActivity(ActionEvent actionEvent) {
        if (info != null) {
            mainWindow.removeSubActivity(info);
            close(btnDelete);
        }
    }

    @FXML
    protected void handleCancel(ActionEvent actionEvent) {
        close(btnAddSubActivity);
    }

    ChangeListener textChangeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            updateEnableState();
        }
    };
}
