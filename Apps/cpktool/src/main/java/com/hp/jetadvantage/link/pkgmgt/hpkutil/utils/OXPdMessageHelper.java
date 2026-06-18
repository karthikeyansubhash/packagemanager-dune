package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import java.net.URI;
import java.util.ArrayList;

public class OXPdMessageHelper {

    private static final String HEADER_ACTION_INSTALL = "http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14/IUIConfigurationService/RegisterTopLevelButton";
    private static final String HEADER_ACTION_UIConfigurationService = "http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14/IUIConfigurationService/";
    private static final String HEADER_ACTION_UIConfigurationService_SetBrowserConfiguration = HEADER_ACTION_UIConfigurationService + "SetBrowserConfiguration";
    private static final String HEADER_ACTION_UIConfigurationService_GetBrowserConfiguration = HEADER_ACTION_UIConfigurationService + "GetBrowserConfiguration";
    private static final String HEADER_ACTION_UIConfigurationService_UnregisterButton = HEADER_ACTION_UIConfigurationService + "UnregisterTopLevelButton";
    private static final String HEADER_ACTION_UIConfigurationService_GetUIProfile = HEADER_ACTION_UIConfigurationService + "GetUIProfile";
    private static final String HEADER_ACTION_UIConfigurationService_GetUIAttributes = HEADER_ACTION_UIConfigurationService + "GetUIAttributes";
    private static final String HEADER_ACTION_UIConfigurationService_GetTopLevelButtonRecord = HEADER_ACTION_UIConfigurationService + "GetTopLevelButtonRecord";
    private static final String HEADER_ACTION_UIConfigurationService_GetTopLevelButtonRecords = HEADER_ACTION_UIConfigurationService + "GetTopLevelButtonRecords";
    private static final String HEADER_ACTION_Standard = "http://schemas.xmlsoap.org/ws/2004/09/transfer/";
    private static final String HEADER_ACTION_Standard_Put = HEADER_ACTION_Standard + "Put";

    public static String getOXPdInstallBody(String url, String buttonInfo) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_INSTALL) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                buttonInfo +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getSoapTrustSites(String url) {
        return getSoapBeginning()
                + getSoapHeaderBeginning()
                + getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_GetBrowserConfiguration)
                + getSoapHeaderReplyTo()
                + getSoapHeaderTo(url)
                + getSoapHeaderEnd()
                + getSoapBodyBeginning()
                + "<GetBrowserConfiguration xmlns=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\"></GetBrowserConfiguration>"
                + getSoapBodyEnd()
                + getSoapEnd();
    }

    public static String setSoapTrustSites(String url, ArrayList<String> trustSites) {
        return getSoapBeginning()
                + getSoapHeaderBeginning()
                + getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_SetBrowserConfiguration)
                + getSoapHeaderReplyTo()
                + getSoapHeaderTo(url)
                + getSoapHeaderEnd()
                + getSoapBodyBeginning()
                + getSoapSetBrowserConfigurationBegining()
                + getTrustSites(trustSites)
                + getSoapSetBrowserConfigurationEnd()
                + getSoapBodyEnd()
                + getSoapEnd();
    }

    public static String setSoapCors(String url, String corsValue) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_Standard_Put) +
                getSoapHeaderLocale() +
                getSoapHeaderResourceUri("WebSettings") +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                setCors(corsValue) +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getOXPdUninstallBody(String url, String uuid) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_UnregisterButton) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                getUnregisterTopLevelButton(uuid) +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getSoapUIProfile(String url) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_GetUIProfile) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                getUIProfile() +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getSoapUIAttributes(String url) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_GetUIAttributes) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                getUIAttributes() +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getTopLevelButtonRecord(String url, String uuid) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_GetTopLevelButtonRecord) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                getButtonId(uuid) +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    public static String getTopLevelButtonRecords(String url) {
        return getSoapBeginning() +
                getSoapHeaderBeginning() +
                getSoapHeaderAction(HEADER_ACTION_UIConfigurationService_GetTopLevelButtonRecords) +
                getSoapHeaderReplyTo() +
                getSoapHeaderTo(url) +
                getSoapHeaderEnd() +
                getSoapBodyBeginning() +
                getTopLevelButtonRecords() +
                getSoapBodyEnd() +
                getSoapEnd();
    }

    private static String getSoapBeginning() {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">";
    }

    private static String getSoapHeaderBeginning() {
        return "<s:Header>";
    }

    private static String getSoapHeaderAction(String headerAction) {
        return "<a:Action s:mustUnderstand=\"1\">" +
                headerAction +
                "</a:Action>";
    }

    private static String getSoapHeaderLocale() {
        return "<h:Locale xml:lang=\"en-US\" a:mustUnderstand=\"false\" " +
                "xmlns:h=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                "xmlns:a=\"http://www.w3.org/2003/05/soap-envelope\" " +
                "xmlns=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" />";
    }

    private static String getSoapHeaderResourceUri(String serviceUri) {
        String uri =
                "<h:ResourceURI xmlns:h=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                        "xmlns=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" >" +
                        "urn:hp:imaging:con:service:systemconfiguration:SystemConfigurationService:";

        if (serviceUri == null) {
            uri += "WebSettings";
        } else {
            uri += serviceUri;
        }

        return uri + "</h:ResourceURI>";
    }

    private static String getSoapHeaderReplyTo() {
        return "<a:MessageID>urn:uuid:f9fc2597-5f25-4e62-b355-8332280682d1</a:MessageID>" +
                "<a:ReplyTo>" +
                "<a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address>" +
                "</a:ReplyTo>";
    }

    private static String getSoapHeaderTo(String serviceUrl) {
        return "<a:To s:mustUnderstand=\"1\">" +
                serviceUrl +
                "</a:To>";
    }

    private static String getSoapHeaderEnd() {
        return "</s:Header>";
    }

    private static String getSoapBodyBeginning() {
        return "<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
    }

    private static String getSoapBodyEnd() {
        return "</s:Body>";
    }

    private static String getSoapEnd() {
        return "</s:Envelope>";
    }

    private static String setCors(String corsValue) {
        return "<config:WebSettings " +
                "xmlns:config=\"http://www.hp.com/schemas/imaging/con/service/systemconfiguration/2009/02/20\" " +
                "xmlns:dd=\"http://www.hp.com/schemas/imaging/con/dictionaries/1.0/\">" +
                "<config:UseCrossOriginResourceSharing>" +
                corsValue +
                "</config:UseCrossOriginResourceSharing>" +
                "</config:WebSettings>";
    }

    private static String getSoapSetBrowserConfigurationBegining() {
        return "<ns2:SetBrowserConfiguration xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">" +
                "<ns2:browserConfiguration>" +
                "<ns2:connectionTimeout>30</ns2:connectionTimeout>" +
                "<ns2:responseTimeout>30</ns2:responseTimeout>";

    }

    private static String getSoapSetBrowserConfigurationEnd() {
        return "</ns2:browserConfiguration>" +
                "</ns2:SetBrowserConfiguration>";
    }

    private static String getTrustSites(ArrayList<String> trustSites) {
        String trustSitesStr = "<ns2:trustedSites>";
        for (String site : trustSites) {
            trustSitesStr += "<ns2:trustedSite>";
            trustSitesStr += site;
            trustSitesStr += "</ns2:trustedSite>";
        }
        return trustSitesStr + "</ns2:trustedSites>";
    }

    private static String getUnregisterTopLevelButton(String uuid) {
        return "<ns2:UnregisterTopLevelButton xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">" +
                "<ns2:buttonId>"
                + uuid
                + "</ns2:buttonId>" +
                " </ns2:UnregisterTopLevelButton>";
    }

    private static String getUIProfile() {
        return "<ns2:GetUIProfile xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">" +
                "</ns2:GetUIProfile>";
    }

    private static String getUIAttributes() {
        return "<ns2:GetUIAttributes xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">" +
                "</ns2:GetUIAttributes>";
    }

    private static String getButtonId(String uuid) {
        return "<ns2:GetTopLevelButtonRecord xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">" +
                "<ns2:buttonId>"
                + uuid
                + "</ns2:buttonId>" +
                "</ns2:GetTopLevelButtonRecord>";
    }

    private static String getTopLevelButtonRecords(){
        return "<ns2:GetTopLevelButtonRecords xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\"></ns2:GetTopLevelButtonRecords>";
    }
}
