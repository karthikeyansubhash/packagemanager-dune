package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class MacrosActivity extends CommonController {

    @FXML
    private CheckBox checkBoxIpAddress;
    @FXML
    private CheckBox checkBoxUserName;
    @FXML
    private CheckBox checkBoxHostName;
    @FXML
    private CheckBox checkBoxFullyQualifiedUserName;
    @FXML
    private CheckBox checkBoxModelName;
    @FXML
    private CheckBox checkBoxDisplayName;
    @FXML
    private CheckBox checkBoxDate;
    @FXML
    private CheckBox checkBoxDomain;
    @FXML
    private CheckBox checkBoxTime;
    @FXML
    private CheckBox checkBoxPassword;
    @FXML
    private CheckBox checkBoxFormatterSerialNum;
    @FXML
    private CheckBox checkBoxEmail;
    @FXML
    private CheckBox checkBoxFwVersion;
    @FXML
    private CheckBox checkBoxExchangeMailBoxUri;
    @FXML
    private CheckBox checkBoxSerialNum;
    @FXML
    private CheckBox checkBoxHomeFolderPath;
    @FXML
    private CheckBox checkBoxProductNumber;
    @FXML
    private CheckBox checkBoxAlias;
    @FXML
    private CheckBox checkBoxMacAddress;
    @FXML
    private CheckBox checkBoxLdapBindUser;
    @FXML
    private CheckBox checkBoxUserPrincipalName;
    @FXML
    private CheckBox checkBoxSamAccountName;
    @FXML
    private CheckBox checkBoxSidString;
    @FXML
    private CheckBox checkBoxAuthenticationType;

    private ButtonManagerActivity mainWindow;

    public void setMainWindow(ButtonManagerActivity mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setSelect(String macros) {
        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("ip_address_macros") + "$")) {
            checkBoxIpAddress.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("hostname_macros") + "$")) {
            checkBoxHostName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("model_name_macros") + "$")) {
            checkBoxModelName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("date_macros") + "$")) {
            checkBoxDate.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("time_macros") + "$")) {
            checkBoxTime.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("formatter_serial_num_macros") + "$")) {
            checkBoxFormatterSerialNum.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("fw_version_macros") + "$")) {
            checkBoxFwVersion.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("serial_num_macros") + "$")) {
            checkBoxSerialNum.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("product_number_macros") + "$")) {
            checkBoxProductNumber.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("device_macros") + "_"
                + Constants.MESSAGE.getString("macaddress_macros") + "$")) {
            checkBoxMacAddress.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("user_name_macros") + "$")) {
            checkBoxUserName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("fully_qualified_username_macros") + "$")) {
            checkBoxFullyQualifiedUserName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("display_name_macros") + "$")) {
            checkBoxDisplayName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("domain_macros") + "$")) {
            checkBoxDomain.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("password_macros") + "$")) {
            //checkBoxPassword.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("email_macros") + "$")) {
            checkBoxEmail.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("exchange_mail_box_uri_macros") + "$")) {
            checkBoxExchangeMailBoxUri.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("home_folder_path_macros") + "$")) {
            checkBoxHomeFolderPath.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("alias_macros") + "$")) {
            checkBoxAlias.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("ldap_bind_user_macros") + "$")) {
            checkBoxLdapBindUser.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("user_principal_name_macros") + "$")) {
            checkBoxUserPrincipalName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("sam_account_name_macros") + "$")) {
            checkBoxSamAccountName.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("sid_string_macros") + "$")) {
            checkBoxSidString.setSelected(true);
        }

        if (macros.contains("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                + Constants.MESSAGE.getString("authentication_type_macros") + "$")) {
            checkBoxAuthenticationType.setSelected(true);
        }
    }

    @FXML
    protected void handleDeviceCheckBoxAction(ActionEvent actionEvent) {
        mainWindow.setMacros("$" + Constants.MESSAGE.getString("device_macros") + "_"
                        + ((CheckBox)actionEvent.getSource()).getText() + "$",
                ((CheckBox)actionEvent.getSource()).isSelected());
    }

    @FXML
    protected void handleAuthUserCheckBoxAction(ActionEvent actionEvent) {
        mainWindow.setMacros("$" + Constants.MESSAGE.getString("authuser_macros") + "_"
                        + ((CheckBox)actionEvent.getSource()).getText() + "$",
                ((CheckBox)actionEvent.getSource()).isSelected());
    }
}
