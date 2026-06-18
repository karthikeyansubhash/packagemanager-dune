package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.HPKVersion;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LinkPlatformVersion;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SettingsActivity extends CommonController {
    @FXML
    private CheckBox checkVersion_1_0;
    @FXML
    private CheckBox checkVersion_1_1;
    @FXML
    private CheckBox checkVersion_1_2;
    @FXML
    private CheckBox checkVersion_1_3;
    @FXML
    private CheckBox checkVersion_1_4;
    @FXML
    private Button btnApply;
    @FXML
    private Label textApplyMsg;

    private HPKVersion hpkVersion = Constants.HPK_LATEST_VERSION;

    public void setStage(Stage stage, ActionType actionType) {
        this.stage = stage;
        this.currentAction = actionType;
        initView();
    }

    private void initView(){
        hpkVersion = Constants.getHPKVersion();
        if(hpkVersion != null) {
            if (hpkVersion.equals(HPKVersion.HPK_1_0)) {
                checkVersion_1_0.setSelected(true);
            } else if (hpkVersion.equals(HPKVersion.HPK_1_1)) {
                checkVersion_1_1.setSelected(true);
            } else if (hpkVersion.equals(HPKVersion.HPK_1_2)) {
                checkVersion_1_2.setSelected(true);
            } else if (hpkVersion.equals(HPKVersion.HPK_1_3)) {
                checkVersion_1_3.setSelected(true);
            } else if (hpkVersion.equals(HPKVersion.HPK_1_4)) {
                checkVersion_1_4.setSelected(true);
            }
        }
    }

    @FXML
    protected void handleVersion_1_0_Checked(ActionEvent actionEvent){
        checkVersion_1_0.setSelected(true);
        checkVersion_1_1.setSelected(false);
        checkVersion_1_2.setSelected(false);
        checkVersion_1_3.setSelected(false);
        checkVersion_1_4.setSelected(false);
        hpkVersion = HPKVersion.HPK_1_0;
        textApplyMsg.setText(null);
    }

    @FXML
    protected void handleVersion_1_1_Checked(ActionEvent actionEvent){
        checkVersion_1_0.setSelected(false);
        checkVersion_1_1.setSelected(true);
        checkVersion_1_2.setSelected(false);
        checkVersion_1_3.setSelected(false);
        checkVersion_1_4.setSelected(false);
        hpkVersion = HPKVersion.HPK_1_1;
        textApplyMsg.setText(null);
    }

    @FXML
    protected void handleVersion_1_2_Checked(ActionEvent actionEvent){
        checkVersion_1_0.setSelected(false);
        checkVersion_1_1.setSelected(false);
        checkVersion_1_2.setSelected(true);
        checkVersion_1_3.setSelected(false);
        checkVersion_1_4.setSelected(false);
        hpkVersion = HPKVersion.HPK_1_2;
        textApplyMsg.setText(null);
    }

    @FXML
    protected void handleVersion_1_3_Checked(ActionEvent actionEvent){
        checkVersion_1_0.setSelected(false);
        checkVersion_1_1.setSelected(false);
        checkVersion_1_2.setSelected(false);
        checkVersion_1_3.setSelected(true);
        checkVersion_1_4.setSelected(false);
        hpkVersion = HPKVersion.HPK_1_3;
        textApplyMsg.setText(null);
    }

    @FXML
    protected void handleVersion_1_4_Checked(ActionEvent actionEvent){
        checkVersion_1_0.setSelected(false);
        checkVersion_1_1.setSelected(false);
        checkVersion_1_2.setSelected(false);
        checkVersion_1_3.setSelected(false);
        checkVersion_1_4.setSelected(true);
        hpkVersion = HPKVersion.HPK_1_4;
        textApplyMsg.setText(null);
    }

    @FXML
    protected void handleApply(ActionEvent actionEvent) {
        Constants.setHPKVersion(hpkVersion);
        if (LinkPlatformVersion.getEnumByHPKVersion(hpkVersion) != null) {
            Constants.setPlatformVersion(LinkPlatformVersion.getEnumByHPKVersion(hpkVersion));
        }
        textApplyMsg.setText(Constants.MESSAGE.getString("msg_apply_hpk_version"));
        showMsg(State.INFORMATION,
                Constants.MESSAGE.getString("menu_settings"),
                Constants.MESSAGE.getString("msg_apply_hpk_version"),
                Constants.MESSAGE.getString("msg_apply_hpk_version_content"));

        changeScreen(ActionType.SETTINGS);
    }
}
