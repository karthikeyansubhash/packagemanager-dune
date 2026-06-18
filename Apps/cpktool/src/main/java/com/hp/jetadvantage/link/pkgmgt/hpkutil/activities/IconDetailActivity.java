package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.AppInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IconDetailActivity extends CommonController{
    @FXML
    private Button btnIcon;
    @FXML
    private Label labelName;
    @FXML
    private TextArea textUuid;
    @FXML
    private TextArea textVendor;
    @FXML
    private TextArea textDate;
    @FXML
    private TextArea textVersion;
    @FXML
    private TextArea textPackageName;
    @FXML
    private TextArea textDescription;
    @FXML
    private HBox hBoxVersion;
    @FXML
    private HBox hBoxPackageName;

    public void setValue(AppInfo appInfo) {
        if (appInfo.getIcon() != null) {
            btnIcon.setGraphic(new ImageView(new Image(new ByteArrayInputStream(Utils.decodeBase64BinaryToFile(appInfo.getIcon())))));
        }
        labelName.setText(appInfo.getName());
        textUuid.setText(appInfo.getUuid());
        textVendor.setText(appInfo.getVendorName());
        textVersion.setText(appInfo.getVersion());
		textPackageName.setText(appInfo.getPackageName());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
        String date = simpleDateFormat.format(new Date(Long.parseLong(appInfo.getInstallDate())));
        textDate.setText(date);

        OutputStreamWriter out = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = gson.fromJson(appInfo.getDescription(), JsonElement.class);
            String element = gson.toJson(jsonElement);

            out = new OutputStreamWriter(new ByteArrayOutputStream());
            textDescription.setText( new String(element.getBytes(out.getEncoding()), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {}
            }
        }

        if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
            hBoxVersion.getChildren().clear();
            hBoxPackageName.getChildren().clear();
        }
    }
}
