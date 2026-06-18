package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ActionType;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.ElementException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.HpkFileHelper.SERIALIZER;

public class Utils {
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isValidIP(final String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    private static final Pattern UUID_PATTERN = Pattern
            .compile("(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$");

    public static final boolean isValidUuid(final String uuid) {
        return ((uuid != null) && (uuid.trim().length() > 31)) && UUID_PATTERN.matcher(uuid).matches();
    }

    public static boolean isValidDate(String date) {
        return Constants.DATE_REGEX.matcher(date).matches();
    }

    public static final boolean isValidJSON(String jsonInString) {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonInString);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
            gson.toJson(gsonObject);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static final boolean isValidJSONArray(String jsonArrayInString) {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonArray gsonArray = (JsonArray) jsonParser.parse(jsonArrayInString);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
            gson.toJson(jsonArrayInString);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static final String getExtension(File file) {
        String name = file.getName();
        return getExtension(name);
    }

    public static final String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        if (dot > 0) {
            try {
                return fileName.substring(dot + 1);
            } catch (Exception e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static PlatformType checkSupportFormat(File hpkFile) throws Exception {
        HpkFile cpkFile = null;
        Connector connector;
        try {
            cpkFile = new HpkFile(hpkFile);
            connector = cpkFile.getConnector();
        } finally {
            if (cpkFile != null) {
                try {
                    cpkFile.close();
                } catch (Exception e) {
                }
            }
        }
        if (Utils.getExtension(connector.getInstallFile()).toLowerCase().contains("apk")) {
            return PlatformType.LinkForDevice;
        } else if (Utils.getExtension(connector.getInstallFile()).toLowerCase().contains("xml")) {
            return PlatformType.LinkForWeb;
        }
        throw new IllegalArgumentException(Constants.MESSAGE.getString("error_not_support_format"));
    }

    public static void isVerifyFormat(Object object, java.net.URL xsd) throws Exception {
        Writer writer = new StringWriter();
        try {
            SERIALIZER.write(object, writer);
        } catch (ElementException ex) {
            throw new ElementException(Constants.MESSAGE.getString("msg_input_error"));
        }
        String data = writer.toString();
        isVerifyFormat(data, xsd);
    }

    public static void isVerifyFormat(String data, java.net.URL xsd) throws Exception {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(data.getBytes("UTF-8"));
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(xsd);

            Validator validator = schema.newValidator();

            validator.validate(new StreamSource(bais));
        } catch (SAXException sae) {
            throw new SAXException(Constants.MESSAGE.getString("msg_install_file_is_invalid"));
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static ActionType getActionType(PlatformType platformType) {
        switch (platformType) {
            case LinkForDevice:
                return ActionType.NEW_LINK_FOR_DEVICE;
            case LinkForWeb:
                return ActionType.NEW_LINK_FOR_WEB;
            default:
                return ActionType.NEW_LINK_FOR_DEVICE;
        }
    }

    public static File checkDefaultConfigValidation(String path) throws Exception {
        if (path != null && !path.isEmpty()) {
            File defaultConfigFile = new File(path);
            String fileContent = Utils.readFile(defaultConfigFile.getAbsolutePath());
            if (!Utils.isValidJSON(fileContent)) {
                throw new Exception(Constants.MESSAGE.getString("msg_json_invalid_error"));
            }
            return defaultConfigFile;
        }
        return null;
    }

    public static ButtonInfo getButtonInfo(File file, Class<?> clz) throws Exception {
        if (file != null) {
            InputStream inputStream = new FileInputStream(file);
            return getButtonInfo(inputStream, clz);
        }
        throw new Exception(Constants.MESSAGE.getString("msg_file_not_exist"));
    }

    public static ButtonInfo getButtonInfo(InputStream inputStream, Class<?> clz) throws Exception {

        Serializer serializer = new Persister(new AnnotationStrategy(),
                new Format("<?xml version=\"1.0\"?>"));

        if (inputStream != null) {
            ButtonInfo buttonInfo = serializer.read(ButtonInfo.class, inputStream);
            Utils.isVerifyFormat(buttonInfo, clz.getResource(Constants.HPK_BUTTON_XSD_FILE_PATH));
            return buttonInfo;
        } else {
            throw new Exception(Constants.MESSAGE.getString("msg_file_not_exist"));
        }
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    public static String encodeFileToBase64Binary(File file) {
        FileInputStream fileInputStream = null;
        try {
            File icon = new File(file.getAbsolutePath());
            fileInputStream = new FileInputStream(icon);
            byte[] bytes = new byte[(int) icon.length()];
            fileInputStream.read(bytes);

            return DatatypeConverter.printBase64Binary(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    public static byte[] decodeBase64BinaryToFile(String encodedFile) {
        return DatatypeConverter.parseBase64Binary(encodedFile);
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = null;
        try {
            into = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = inputStream.read(buf)); ) {
                into.write(buf, 0, n);
            }

            return new String(into.toByteArray(), "UTF-8");
        } finally {
            if (into != null) {
                try {
                    into.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
