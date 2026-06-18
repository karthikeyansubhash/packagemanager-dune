package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.DIALOG_MIN_WIDTH;

class Controller {

    private static final String APK = "apk";
    static final String HPK = "hpk";
    private static final String XML = "xml";
    private static final String JSON = "json";
    private static final String BMP = "bmp";
    private static final String GIF = "gif";
    private static final String JPG = "jpg";
    private static final String PNG = "png";
    private static final String IMAGE = "image";
    private static File initialPath;

    public enum State {
        INFORMATION,
        WARNING,
        ERROR,
    }

    public enum FileChooserMode {
        OPEN,
        OPEN_MULTIPLE,
        SAVE,
    }

    public interface ButtonClickListener {
        void ok();

        void cancel();
    }

    void showYesOrNoDialog(final State state, final String error, final String headerText, final String contentText, final ButtonClickListener buttonClickListener) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final Stage dialogStage = new Stage(StageStyle.DECORATED);
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setTitle(error);

                //Text title = new Text(error);
                Text header = new Text(headerText);
                header.setWrappingWidth(DIALOG_MIN_WIDTH);

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.TOP_CENTER);
                hBox.setPadding(new Insets(15, 15, 15, 15));
                try {
                    Image icon = new Image(getClass().getResourceAsStream(getStateIcon(state)));
                    ImageView imageView = new ImageView(icon);
                    imageView.fitWidthProperty().setValue(48);
                    imageView.fitHeightProperty().setValue(48);
                    hBox.getChildren().add(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                VBox vbox = new VBox();

                VBox headerBox = new VBox();
                vbox.setAlignment(Pos.CENTER_LEFT);
                vbox.setPadding(new Insets(0, 0, 0, 15));

                headerBox.getChildren().add(header);
                headerBox.setAlignment(Pos.CENTER);
                vbox.getChildren().add(headerBox);

                hBox.getChildren().add(vbox);

                HBox buttonBox = new HBox();
                buttonBox.setAlignment(Pos.CENTER);
                VBox invisible = new VBox();
                invisible.setPadding(new Insets(10, 10, 10, 10));

                Button okButton = new Button("OK");
                okButton.setPrefWidth(100);
                okButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        buttonClickListener.ok();
                        dialogStage.close();
                    }
                });

                Button cancelButton = new Button("Cancel");
                cancelButton.setPrefWidth(100);
                cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        buttonClickListener.cancel();
                        dialogStage.close();
                    }
                });

                if (contentText != null) {
                    Text content = new Text(contentText);
                    content.setWrappingWidth(DIALOG_MIN_WIDTH);
                    HBox contentBox = new HBox();
                    contentBox.setPadding(new Insets(10, 0, 0, 0));
                    contentBox.setPrefWidth(DIALOG_MIN_WIDTH);
                    contentBox.getChildren().add(content);

                    vbox.getChildren().addAll(contentBox);
                }

                buttonBox.getChildren().addAll(okButton, invisible, cancelButton);

                VBox layoutBox = new VBox();
                layoutBox.setPadding(new Insets(0, 0, 10, 0));
                layoutBox.setPrefWidth(DIALOG_MIN_WIDTH);
                layoutBox.setAlignment(Pos.CENTER);
                layoutBox.getChildren().addAll(hBox, buttonBox);

                dialogStage.setResizable(false);
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
                dialogStage.setScene(new Scene(layoutBox));
                dialogStage.show();
            }
        });
    }

    void showMsg(final State state, final String error, final String headerText, final String contentText) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String contentStr = contentText;
                final Stage dialogStage = new Stage(StageStyle.DECORATED);
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setTitle(error);

                //Text title = new Text(error);
                Text header = new Text(headerText);
                if (DeviceMode.OXPD.equals(Constants.DEFAULT_DEVICE_MODE)) {
                    if (contentText != null && !contentText.isEmpty()) {
                        header = new Text(contentText);
                    }
                    contentStr = null;
                }
                header.setWrappingWidth(DIALOG_MIN_WIDTH);

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.TOP_CENTER);
                hBox.setPadding(new Insets(15, 15, 15, 15));
                try {
                    Image icon = new Image(getClass().getResourceAsStream(getStateIcon(state)));
                    ImageView imageView = new ImageView(icon);
                    imageView.fitWidthProperty().setValue(48);
                    imageView.fitHeightProperty().setValue(48);
                    hBox.getChildren().add(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                VBox vbox = new VBox();

                VBox headerBox = new VBox();
                vbox.setAlignment(Pos.CENTER_LEFT);
                vbox.setPadding(new Insets(0, 0, 0, 15));

                headerBox.getChildren().add(header);
                headerBox.setAlignment(Pos.CENTER);
                vbox.getChildren().add(headerBox);

                hBox.getChildren().add(vbox);

                Button okButton = new Button("OK");
                okButton.setPrefWidth(100);
                okButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        dialogStage.close();
                    }
                });

                if (contentStr != null) {
                    Text content = new Text(contentStr);
                    content.setWrappingWidth(DIALOG_MIN_WIDTH);
                    HBox contentBox = new HBox();
                    contentBox.setPadding(new Insets(10, 0, 0, 0));
                    contentBox.setPrefWidth(DIALOG_MIN_WIDTH);
                    contentBox.getChildren().add(content);

                    vbox.getChildren().addAll(contentBox);
                }

                VBox buttonBox = new VBox();
                buttonBox.setPadding(new Insets(0, 0, 10, 0));
                buttonBox.setPrefWidth(DIALOG_MIN_WIDTH);
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(hBox, okButton);

                dialogStage.setResizable(false);
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/hp.png")));
                dialogStage.setScene(new Scene(buttonBox));
                dialogStage.show();
            }
        });
    }

    private String getStateIcon(State state) {
        switch (state) {
            case ERROR:
                return "/icon/error.png";
            case WARNING:
                return "/icon/warning.png";
            default:
                return "/icon/information.png";
        }
    }

    protected File selectDirectory(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Directory");
        if (initialPath != null) {
            directoryChooser.setInitialDirectory(initialPath);
        }

        File currentPath = directoryChooser.showDialog(stage);
        if (currentPath != null) {
            initialPath = currentPath;
        }
        return currentPath;
    }

    protected File selectFile(Stage stage, ActionType currentType, FileChooserMode mode) {
        String extension;
        if (currentType != null && currentType.equals(ActionType.NEW_LINK_FOR_DEVICE)) {
            extension = APK;
        } else if (currentType != null && currentType.equals(ActionType.NEW_LINK_FOR_WEB)) {
            extension = XML;
        } else if (currentType != null && currentType.equals(ActionType.CONFIGURATION)) {
            extension = JSON;
        } else if (currentType != null && currentType.equals(ActionType.IMPORT_BUTTON_IMAGE)) {
            extension = IMAGE;
        } else {
            return null;
        }
        return startFileChooser(stage, extension, null, mode);
    }

    protected File selectHPKFile(Stage stage, String title, FileChooserMode mode) {
        return startFileChooser(stage, HPK, title, mode);
    }

    private File startFileChooser(Stage stage, String ext, String title, FileChooserMode mode) {
        if (title == null) {
            title = "Open " + ext + " file";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (initialPath != null && initialPath.exists()) {
            fileChooser.setInitialDirectory(initialPath);
        }

        if (ext != null && ext.equalsIgnoreCase(IMAGE)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext + " Files", "*." + BMP, "*." + GIF, "*." + JPG, "*." + PNG));
        } else if (ext != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext + " file", "*." + ext));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        }

        File currentPath;
        switch (mode) {
            case OPEN:
                currentPath = fileChooser.showOpenDialog(stage);
                break;
            case SAVE:
                currentPath = fileChooser.showSaveDialog(stage);
                break;
            default:
                currentPath = null;
        }

        if (currentPath != null) {
            initialPath = currentPath.getParentFile();
        }
        return currentPath;
    }

    /*
     * for close sub activity.
     */
    protected void close(Control control) {
        Stage stage = (Stage) control.getScene().getWindow();
        stage.close();
    }
}
