//package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.util;
//
//import android.content.Context;
//import android.text.TextUtils;
//import android.util.Base64;
//import android.util.Log;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.HomeScreenData;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.OXPdSerializer;
//import com.hp.oxpdlib.uiconfiguration.Error;
//import com.hp.oxpdlib.uiconfiguration.ErrorName;
//import com.hp.oxpdlib.uiconfiguration.TopLevelButtonRecord;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.OXPdButtonData;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.OXPdData;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.LocalizedString;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.LookAndFeelSpecificIcon;
//import com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil.NetworkCredentials;
//import com.hp.jetadvantage.link.services.connect.OXPdConnect;
//import com.hp.jetadvantage.link.services.connect.oxpd.OXPdAsyncCallFuture;
//import com.hp.jetadvantage.link.services.connect.oxpd.OXPdClient;
//import com.hp.jetadvantage.link.services.connect.request.OmniRequest;
//import com.hp.jetadvantage.link.services.connect.util.OmniResponseException;
//
//import org.json.JSONObject;
//import org.simpleframework.xml.Serializer;
//import org.simpleframework.xml.convert.AnnotationStrategy;
//import org.simpleframework.xml.core.Persister;
//import org.simpleframework.xml.stream.Format;
//
//import java.io.File;
//import java.io.UnsupportedEncodingException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class ButtonHelper {
//    private static final String TAG = "ButtonHelper";
//
//    public static final Serializer SERIALIZER = new Persister(new AnnotationStrategy(),
//            new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" standalone=\"yes\"?>"));
//
//    // set as home / config on install
//    public static final String DEVICE_APIS_VERSION = "/hp/device/apis/v1";
//    public static final String APPLICATIONS = DEVICE_APIS_VERSION + "/applications";
//    public static final String HOME_SCREEN = DEVICE_APIS_VERSION + "/homeScreen/instance";
//
//    private static final String HOSTNAME_INTERNAL = "fwprinter2"; // "jdiloopback";
//
//    public static final Gson GSON = new Gson();
//    private static final String USER_NAME = "guest";
//
//    private final OXPdConnect oxpdConnect;
//    private final OXPdClient oxpdClient;
//
//    public ButtonHelper(Context context, String authorization) {
//        this.oxpdConnect = OXPdConnect.getInstance();
//        this.oxpdClient = OXPdClient.getInstance(context, HOSTNAME_INTERNAL);
//        updateCredentials(oxpdClient, authorization);
//    }
//
//    public static void updateCredentials(OXPdClient client, String authorization) {
//        // reset to default ("admin" and <empty password>)
//        client.updateCredentials(USER_NAME, "");
//
//        if (authorization != null) {
//            byte[] decodedPairBytes = Base64.decode(authorization, Base64.DEFAULT);
//            if (decodedPairBytes != null) {
//                try {
//                    // decodedPair is "username:password"
//                    String decodedPair = new String(decodedPairBytes, "ISO-8859-1");
//                    int sep = decodedPair.indexOf(":");
//                    if (sep >= 0) {
//                        String userName = decodedPair.substring(0, sep);
//                        String password = decodedPair.substring(sep+1);
//                        if ("admin".equalsIgnoreCase(userName) || !USER_NAME.equals(userName) || !"".equals(password)) {
//                            // not default - update client
//                            client.updateCredentials(userName, password);
//                        }
//                    }
//                } catch (UnsupportedEncodingException e) {
//                    Log.e(TAG, "Failed to convert password bytes to string", e);
//                }
//            }
//        }
//    }
//
//    public static String toJson(final Object object) {
//        return GSON.toJson(object);
//    }
//
//    public boolean createButton(String uuid, OXPdData data) {
//        Log.d(TAG, "Calling create button for Workpath");
//
//        if (retrieveOXPdButtonForSmartUX(uuid) != null) {
//            if (deleteOXPdButtonForSmartUX(uuid)) {
//                Log.d(TAG, "Workpath button successfully deleted");
//            } else {
//                Log.d(TAG, "Failed to delete Workpath button");
//            }
//        } else {
//            Log.d(TAG, "Workpath button not exist, create new");
//        }
//
//        return createOXPdButtonForSmartUX(data);
//    }
//
//    public TopLevelButtonRecord createButtonByOXPd(String installFilePath) {
//        File installFile = new File(installFilePath);
//        if (installFile.exists()) {
//            return createOXPdButtonForOXPd(installFilePath);
//        }
//
//        return null;
//    }
//
//    public boolean deleteButton(String uuid) {
//        Log.d(TAG, "Calling delete button for Workpath");
//
//        // delete only if exists, if not exist - return true (already deleted)
//        if (retrieveOXPdButtonForSmartUX(uuid) == null) {
//            Log.d(TAG, "Button is not found, returning true");
//            return true;
//        }
//
//        return deleteOXPdButtonForSmartUX(uuid);
//    }
//
//    public boolean deleteButtonByOXPd(final String uuid) {
//        Log.d(TAG, "Calling delete button for OXPd");
//
//        try {
//            TopLevelButtonRecord button = getButtonForOXPd(uuid);
//
//            // if button is not found - return true, otherwise try to delete it
//            return button == null || deleteOXPdButtonForOXPd(uuid);
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to call UnregisterTopLevelButtonRecord", e);
//        }
//
//        return false;
//    }
//
//    private String retrieveOXPdButtonForSmartUX(String uuid) {
//        try {
//            return oxpdConnect.callOmniInternal(OmniRequest.GET, OmniRequest.PACKAGE_MANAGEMENT + "/" + uuid);
//        } catch (OmniResponseException error) {
//            if (error.getStatus() == 404) {
//                Log.d(TAG, "Button is missing - return false");
//            } else {
//                Log.e(TAG, error.getMessage(), error);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to get OXPd button", e);
//        }
//
//        return null;
//    }
//
//    public boolean configOnInstallToHome(String uuid){
//        try {
//            String app = oxpdConnect.callOmniInternal(OmniRequest.GET, APPLICATIONS + "/" + uuid + "?expand=href");
//
//            JSONObject object = new JSONObject(app);
//            JSONObject values = object.getJSONObject("Values");
//
//            HomeScreenData data = new HomeScreenData();
//            data.setAppId(values.getString("id"));
//            data.setIntentUri(values.getString("intentUri"));
//            String module = values.getString("module");
//            if (!TextUtils.isEmpty(module) && !"null".equalsIgnoreCase(module)) {
//                data.setModule(values.getString("module"));
//            }
//            data.setTitle(values.getString("stringId"));
//            data.setNativeHomeScreenFallbackEnabled(false);
//            oxpdConnect.callOmniInternal(OmniRequest.POST, HOME_SCREEN, toJson(data));
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to change homescreen with id : " + uuid, e);
//        }
//
//        return false;
//    }
//
//    private boolean createOXPdButtonForSmartUX(OXPdData oxPdData) {
//        Log.i(TAG, "createOXPdButtonForSmartUX started");
//        try {
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            gsonBuilder.registerTypeAdapter(OXPdData.class, new OXPdSerializer());
//            Gson gson = gsonBuilder.create();
//            oxpdConnect.callOmniInternal(OmniRequest.POST, OmniRequest.PACKAGE_MANAGEMENT, gson.toJson(oxPdData));
//            Log.i(TAG, "createOXPdButtonForSmartUX completed: " + oxPdData.toLogString());
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to create OXPd button", e);
//        }
//
//        return false;
//    }
//
//    public OXPdButtonData getOXPdButtonData(String installFilePath) {
//        OXPdButtonData data = null;
//        try {
//            data = SERIALIZER.read(OXPdButtonData.class, new File(installFilePath));
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to parse " + installFilePath, e);
//        }
//        return data;
//    }
//
//    public TopLevelButtonRecord loadOXPdData(String installFilePath) throws Exception {
//        OXPdButtonData data = getOXPdButtonData(installFilePath);
//
//        if (data == null) {
//            return null;
//        }
//
//        final TopLevelButtonRecord.Builder buttonRecord = new TopLevelButtonRecord.Builder();
//        buttonRecord.setButtonID(data.id);
//        List<LocalizedString> titles = data.title;
//        for (int inx = 0; inx < titles.size(); inx++) {
//            buttonRecord.addTitle(new com.hp.oxpdlib.uiconfiguration.LocalizedString(titles.get(inx).code, titles.get(inx).value));
//        }
//        List<LocalizedString> description = data.description;
//        for (int inx = 0; inx < description.size(); inx++) {
//            buttonRecord.addDescription(new com.hp.oxpdlib.uiconfiguration.LocalizedString(description.get(inx).code, description.get(inx).value));
//        }
//        buttonRecord.setRequestedPosition(data.requestedPosition);
//
//        if (data.browserTarget == null || data.browserTarget.webApplication == null) {
//            return null; // missing data
//        }
//
//        try {
//            buttonRecord.setButtonTarget(new URL(data.browserTarget.webApplication.uri));
//        } catch (MalformedURLException mue) {
//            Log.e(TAG, "Failed to set button URL", mue);
//            return null;
//        }
//
//        if (data.browserTarget.webApplication.binding != null) {
//            buttonRecord.setButtonTargetBinding(com.hp.oxpdlib.common.Binding.fromAttributeValue(data.browserTarget.webApplication.binding));
//        }
//        buttonRecord.setButtonTargetPostQuery(data.browserTarget.initialPostQueryFormatString);
//
//        NetworkCredentials credentials = data.browserTarget.webApplication.networkCredentials;
//        if (credentials != null) {
//            buttonRecord.setNetworkCredentials(new com.hp.oxpdlib.common.NetworkCredentials(credentials.userName, credentials.password, credentials.domain));
//        }
//
//        Map<String, String> uiAttributes = getUIAttributes();
//
//        String lookAndFeel = uiAttributes.get("lookAndFeel");
//        if (lookAndFeel == null) {
//            if (uiAttributes.containsKey("userInterfaceId")) {
//                // use new Omni ui
//                lookAndFeel = uiAttributes.get("userInterfaceId") + ":"
//                        + uiAttributes.get("buttonIconWidth") + "x" + uiAttributes.get("buttonIconHeight");
//            } else {
//                lookAndFeel = "WJ1.5";
//            }
//        }
//
//        Log.d(TAG, "Omni look and feel: " + lookAndFeel);
//
//        List<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons = data.lookAndFeelSpecificIcons;
//        int primaryIndex = -1;
//        int secondaryIndex = -1;
//        if (lookAndFeelSpecificIcons != null) {
//            for (int inx = 0; inx < lookAndFeelSpecificIcons.size(); inx++) {
//                if (lookAndFeel.equals(lookAndFeelSpecificIcons.get(inx).lookAndFeel)) {
//                    primaryIndex = inx;
//                    Log.d(TAG, "createOXPdButtonForOXPdData: found icon");
//                    break;
//                } else if ("WJ1.5".equals(lookAndFeelSpecificIcons.get(inx).lookAndFeel)) {
//                    secondaryIndex = inx;
//                    Log.d(TAG, "createOXPdButtonForOXPdData: found secondary icon");
//                }
//            }
//        }
//
//        // First set correct icon if found matching lookAndFeel, then try icon for old "WJ1.5" UI, otherwise empty (null) - Omni will show default icon
//        if (primaryIndex >= 0) {
//            buttonRecord.setIconData(Base64.decode(lookAndFeelSpecificIcons.get(primaryIndex).icon, 0));
//        } else if (secondaryIndex >= 0) {
//            buttonRecord.setIconData(Base64.decode(lookAndFeelSpecificIcons.get(secondaryIndex).icon, 0));
//        }
//
//        return buttonRecord.build();
//    }
//
//    private TopLevelButtonRecord createOXPdButtonForOXPd(String installFilePath) {
//        Log.i(TAG, "createOXPdButtonForOXPdData started");
//
//        try {
//            TopLevelButtonRecord topLevelButtonRecord = loadOXPdData(installFilePath);
//            if (topLevelButtonRecord != null) {
//                createButtonOXPd(topLevelButtonRecord);
//            }
//
//            Log.i(TAG, "createOXPdButtonForOXPdData completed successfully");
//
//            return topLevelButtonRecord;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to create OXPd button", e);
//        }
//
//        return null;
//    }
//
//    public Map<String, String> getUIAttributes() throws Exception {
//        return new OXPdAsyncCallFuture<Map<String, String>>() {
//            @Override
//            public void execute() throws Exception {
//                oxpdClient.getUIConfiguration().GetUIAttributes(0, this);
//            }
//        }.getResult();
//    }
//
//    private boolean deleteOXPdButtonForSmartUX(String uuid) {
//        Log.i(TAG, "deleteOXPdButtonForSmartUX started");
//        try {
//            Log.i(TAG, "deleteOXPdButtonForSmartUX started 2");
//            oxpdConnect.callOmniInternal(OmniRequest.DELETE, OmniRequest.PACKAGE_MANAGEMENT + "/" + uuid);
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to delete OXPd button", e);
//        }
//
//        return false;
//    }
//
//    public boolean deleteOXPdButtonForOXPd(final String uuid) {
//        try {
//            new OXPdAsyncCallFuture() {
//                @Override
//                public void execute() throws Exception {
//                    oxpdClient.getUIConfiguration().UnregisterTopLevelButtonRecord(UUID.fromString(uuid), 0, this);
//                }
//            }.getResult();
//
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to call UnregisterTopLevelButtonRecord", e);
//        }
//
//        return false;
//    }
//
//    private TopLevelButtonRecord getButtonForOXPd(final String uuid) throws Exception {
//        try {
//            // check if button exists
//            return new OXPdAsyncCallFuture<TopLevelButtonRecord>() {
//                @Override
//                public void execute() throws Exception {
//                    oxpdClient.getUIConfiguration().GetTopLevelButtonRecord(UUID.fromString(uuid), 0, this);
//                }
//            }.getResult();
//        } catch (Error e) {
//            if (e.name == ErrorName.NotFound ||
//                    (e.soapFault != null && e.soapFault.mFaultCode != null && e.soapFault.mFaultCode.mSubCode != null &&
//                            e.soapFault.mFaultCode.mSubCode.mValue.contains("InvalidParameter"))) {
//                Log.d(TAG, "Button is missing - return true");
//                // if missing return true - button already removed
//                return null;
//            }
//
//            throw e;
//        }
//    }
//
//    /**
//     * Returns OXPd button record or null if it's missing or error
//     * @param uuid of the button
//     * @return button record
//     */
//    public TopLevelButtonRecord getButtonOXPd(final String uuid) {
//        try {
//            return getButtonForOXPd(uuid);
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to get OXPd button", e);
//        }
//        return null;
//    }
//
//    private void createButtonOXPd(final TopLevelButtonRecord button) throws Exception {
//        new OXPdAsyncCallFuture() {
//            @Override
//            public void execute() throws Exception {
//                oxpdClient.getUIConfiguration().RegisterTopLevelButtonRecord(button, 0, this);
//            }
//        }.getResult();
//    }
//
//    public boolean updateButtonOXPd(TopLevelButtonRecord button, String url, String userid, String password) {
//        try {
//            TopLevelButtonRecord.Builder buttonBuilder = new TopLevelButtonRecord.Builder();
//
//            // fill the rest of parameters
//            buttonBuilder.setButtonID(button.mButtonID);
//            buttonBuilder.setButtonTarget(new URL(url));
//            buttonBuilder.setButtonTargetPostQuery(button.mButtonTargetPostQuery);
//            buttonBuilder.setButtonTargetBinding(button.mButtonTargetBinding);
//            buttonBuilder.setRequestedPosition(button.mRequestedPosition);
//            buttonBuilder.setIconData(button.mIconData);
//            if (userid != null && password != null) {
//                buttonBuilder.setNetworkCredentials(new com.hp.oxpdlib.common.NetworkCredentials(userid, password));
//            }
//
//            for (com.hp.oxpdlib.uiconfiguration.LocalizedString title : button.mTitle) {
//                buttonBuilder.addTitle(title);
//            }
//
//            for (com.hp.oxpdlib.uiconfiguration.LocalizedString description : button.mDescription) {
//                buttonBuilder.addDescription(description);
//            }
//
//            createButtonOXPd(buttonBuilder.build());
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to update OXPd button", e);
//        }
//
//        return false;
//    }
//}
