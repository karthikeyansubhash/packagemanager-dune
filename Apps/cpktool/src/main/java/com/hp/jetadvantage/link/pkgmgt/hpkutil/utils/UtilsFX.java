package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.activities.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.HPKVersion;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ResourceBundle;

public class UtilsFX {
    public static void changeScreen(Stage stage, Class<?> cls, ActionType actionType) throws Exception {
        Parent root;
        MenuBar menuBar = null;
        FXMLLoader loader = null;

        if (actionType.equals(ActionType.INSTALL)) {
            loader = new FXMLLoader(cls.getResource("/install_activity_controller.fxml"));
        } else if (actionType.equals(ActionType.CONFIGURATION)) {
            loader = new FXMLLoader(cls.getResource("/configuration_activity_controller.fxml"));
        } else if (actionType.equals(ActionType.NEW_LINK_FOR_DEVICE)) {
            loader = new FXMLLoader(cls.getResource("/main_activity_controller.fxml"));
        } else if (actionType.equals(ActionType.NEW_LINK_FOR_WEB)) {
            loader = new FXMLLoader(cls.getResource("/main_activity_for_web_controller.fxml"));
        } else if (actionType.equals(ActionType.OPEN)) {
            if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForDevice)) {
                loader = new FXMLLoader(cls.getResource("/main_activity_controller.fxml"));
            } else if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
                loader = new FXMLLoader(cls.getResource("/main_activity_for_web_controller.fxml"));
            }
        } else if (actionType.equals(ActionType.ICONLIST)) {
            loader = new FXMLLoader(cls.getResource("/icon_list_activity.fxml"));
        } else if (actionType.equals(ActionType.SETTINGS)) {
            loader = new FXMLLoader(cls.getResource("/settings_activity.fxml"));
        } else if (actionType.equals(ActionType.ATTESTATION)) {
            loader = new FXMLLoader(cls.getResource("/attestation_activity.fxml"));
        }

        if (loader != null) {
            loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
            root = (Parent) loader.load();
            //create a new scene with root and set the stage
            if (root != null && stage != null) {
                if (actionType.equals(ActionType.NEW_LINK_FOR_DEVICE)) {
                    ((MainActivity) loader.getController()).setStage(stage, ActionType.NEW_LINK_FOR_DEVICE);
                } else if (actionType.equals(ActionType.NEW_LINK_FOR_WEB)) {
                    ((MainActivityForWeb) loader.getController()).setStage(stage, ActionType.NEW_LINK_FOR_WEB);
                } else if (actionType.equals(ActionType.INSTALL)) {
                    ((InstallActivity) loader.getController()).setStage(stage, ActionType.INSTALL);
                } else if (actionType.equals(ActionType.UNINSTALL)) {
                    ((InstallActivity) loader.getController()).setStage(stage, ActionType.UNINSTALL);
                } else if (actionType.equals(ActionType.OPEN)) {
                    if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForDevice)) {
                        ((MainActivity) loader.getController()).setStage(stage, ActionType.OPEN);
                    } else if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForWeb)) {
                        ((MainActivityForWeb) loader.getController()).setStage(stage, ActionType.OPEN);
                    }
                } else if (actionType.equals(ActionType.CONFIGURATION)) {
                    ((ConfigurationActivity) loader.getController()).setStage(stage, ActionType.CONFIGURATION);
                } else if (actionType.equals(ActionType.ICONLIST)) {
                    ((IconListActivity) loader.getController()).setStage(stage, ActionType.ICONLIST);
                } else if (actionType.equals(ActionType.SETTINGS)) {
                    ((SettingsActivity) loader.getController()).setStage(stage, ActionType.SETTINGS);
                } else if (actionType.equals(ActionType.ATTESTATION)) {
                    ((AttestationActivity) loader.getController()).setStage(stage, ActionType.ATTESTATION);
                }
                loader = new FXMLLoader(cls.getResource("/menu_controller.fxml"));
                if (loader != null) {
                    loader.setResources(ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE));
                    menuBar = (MenuBar) loader.load();
                    ((CommonController) loader.getController()).setStageForMenu(stage, actionType);
                }

                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setContent(root);
                BorderPane borderPane = new BorderPane();
                borderPane.setTop(menuBar);
                borderPane.setCenter(scrollPane);
                Scene scene = new Scene(borderPane);
                stage.getIcons().add(new Image(cls.getResourceAsStream("/icon/hp.png")));
                if (Constants.DEFAULT_PLATFORM_TYPE.equals(PlatformType.LinkForDevice)) {
                    String title = Constants.MESSAGE.getString("menu_toolname");
                    HPKVersion hpkVersion = Constants.getHPKVersion();
                    if (hpkVersion != null) {
                        if (hpkVersion.equals(HPKVersion.HPK_1_0)) {
                            title = title + String.format(" (Version %s) - %s", Constants.TOOL_VERSION, Constants.MESSAGE.getString("hpk_version_1_0"));
                        } else if (hpkVersion.equals(HPKVersion.HPK_1_1)) {
                            title = title + String.format(" (Version %s) - %s", Constants.TOOL_VERSION, Constants.MESSAGE.getString("hpk_version_1_1"));
                        } else if (hpkVersion.equals(HPKVersion.HPK_1_2)) {
                            title = title + String.format(" (Version %s) - %s", Constants.TOOL_VERSION, Constants.MESSAGE.getString("hpk_version_1_2"));
                        } else if (hpkVersion.equals(HPKVersion.HPK_1_3)) {
                            title = title + String.format(" (Version %s) - %s", Constants.TOOL_VERSION, Constants.MESSAGE.getString("hpk_version_1_3"));
                        } else if (hpkVersion.equals(HPKVersion.HPK_1_4)) {
                            title = title + String.format(" (Version %s) - %s", Constants.TOOL_VERSION, Constants.MESSAGE.getString("hpk_version_1_4"));
                        }
                    }
                    stage.setTitle(title);
                } else {
                    stage.setTitle(String.format(Constants.MESSAGE.getString("menu_toolname_for_web") + " (Build: %s)", Constants.TOOL_BUILD_DATE));
                }

                scrollPane.setMaxHeight(Constants.SCROLL_PANE_MAX_HEIGHT);
                scrollPane.setFitToWidth(true);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        } else {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_failed_to_open"));
        }
    }

    public static boolean isJavaFX8Above() {
        try {
            String javaFXVersion = (String) System.getProperties().get("javafx.runtime.version");
            if (javaFXVersion != null) {
                if (Integer.parseInt(javaFXVersion.substring(0, 1)) > 8) {
                    return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    public static String encodeImageToBase64Binary(Image image) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", s);
            byte[] res = s.toByteArray();

            return DatatypeConverter.printBase64Binary(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
