package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LookAndFeelSpecificIcon;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.NetworkCredentials;

import java.util.ArrayList;

public class OXPdButtonHelper {

    public static String getOXPdButtonString(ButtonInfo buttonInfo, String iconSize) {
        return getRegisterTopLevelButtonBegining()
                + getTopLevelButtonRecordBegining()
                + getId(buttonInfo.getUuid().toString())
                + getTitle(buttonInfo.getTitle())
                + getDescription(buttonInfo.getDescription())
                + getRequestedPosition(buttonInfo.getRequestedPosition())
                + getBrowserTargetBegining()
                + getInitialPostQueryFormatString(buttonInfo.getBrowserTarget().getInitialPostQueryFormatString())
                + getWebApplicationBegining()
                + getUri(buttonInfo.getBrowserTarget().getWebApplication().getUri())
                + getBinding(buttonInfo.getBrowserTarget().getWebApplication().getBinding())
                + getNetworkCredentials(buttonInfo.getBrowserTarget().getWebApplication().getNetworkCredentials())
                + getWebApplicationEnd()
                + getBrowserTargetEnd()
                + getIcon(buttonInfo.getLookAndFeelSpecificIcons(), iconSize)
                + getTopLevelButtonRecordEnd()
                + getRegisterTopLevelButtonEnd();
    }

    private static String getRegisterTopLevelButtonBegining() {
        return "<ns2:RegisterTopLevelButton xmlns=\"http://www.hp.com/schemas/imaging/OXPd/common/2010/04/14\" xmlns:ns2=\"http://www.hp.com/schemas/imaging/OXPd/service/uiconfiguration/2010/04/14\">";
    }

    private static String getRegisterTopLevelButtonEnd() {
        return "</ns2:RegisterTopLevelButton>";
    }

    private static String getTopLevelButtonRecordBegining() {
        return "<ns2:topLevelButtonRecord>";
    }

    private static String getTopLevelButtonRecordEnd() {
        return "</ns2:topLevelButtonRecord>";
    }

    private static String getId(String uuid){
        return "<ns2:id>"
                + uuid
                + "</ns2:id>";
    }

    private static String getTitle(ArrayList<LocalizedString> titles){
        String titleStr = "<ns2:title>";
        for(LocalizedString title: titles) {
            titleStr += "<LocalizedString>"
                    + "<code>" + title.getCode() + "</code>"
                    + "<value>" + title.getValue() + "</value>"
                    + "</LocalizedString>";
        }
        return titleStr + "</ns2:title>";
    }

    private static String getDescription(ArrayList<LocalizedString> titles){
        String titleStr = "<ns2:description>";
        for(LocalizedString title: titles) {
            titleStr += "<LocalizedString>"
                    + "<code>" + title.getCode() + "</code>"
                    + "<value>" + title.getValue() + "</value>"
                    + "</LocalizedString>";
        }
        return titleStr + "</ns2:description>";
    }

    private static String getRequestedPosition(String requestPosition) {
        return "<ns2:requestedPosition>" + requestPosition + "</ns2:requestedPosition>";
    }

    private static String getBrowserTargetBegining() {
        return "<ns2:browserTarget>";
    }

    private static String getBrowserTargetEnd(){
        return "</ns2:browserTarget>";
    }

    private static String getInitialPostQueryFormatString(String initialPostQueryFormatString) {
        if(initialPostQueryFormatString != null && !initialPostQueryFormatString.isEmpty()) {
            return "<ns2:initialPostQueryFormatString>"
                    + initialPostQueryFormatString
                    + "</ns2:initialPostQueryFormatString>";
        }
        return "";
    }

    private static String getWebApplicationBegining() {
        return "<ns2:webApplication>";
    }

    private static String getWebApplicationEnd() {
        return "</ns2:webApplication>";
    }

    private static String getUri(String uri) {
        return "<uri>"
                + uri
                + "</uri>";
    }

    private static String getBinding(String binding) {
        return "<binding>"
                + binding
                + "</binding>";
    }

    private static String getNetworkCredentials(NetworkCredentials networkCredentials) {
        String networkCredential = "";
        if(networkCredentials != null) {
            networkCredential += "<networkCredentials>";
            if(networkCredentials.getUserName() != null && !networkCredentials.getUserName().isEmpty()) {
                networkCredential += "<userName>" + networkCredentials.getUserName() + "</userName>";
            }
            if(networkCredentials.getPassword() != null && !networkCredentials.getPassword().isEmpty()) {
                networkCredential += "<password>" + networkCredentials.getPassword() + "</password>";
            }
            // temporary not include domain in NetworkCredentials
//            if(networkCredentials.getDomain() != null && !networkCredentials.getDomain().isEmpty()) {
//                networkCredential += "<domain>" + networkCredentials.getDomain() + "</domain>";
//            }
            networkCredential += "</networkCredentials>";
        }
        return networkCredential;
    }

    private static String getIcon(ArrayList<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons, String size) {
        String icon = "";
        if(lookAndFeelSpecificIcons != null) {
            for (LookAndFeelSpecificIcon specificIcon : lookAndFeelSpecificIcons) {
                if(size != null && !size.isEmpty()
                        && size.equals(specificIcon.getLookAndFeel())){
                    icon += "<ns2:icon>"
                            + specificIcon.getIcon()
                            + "</ns2:icon>";
                    break;
                }
            }
        }
        return icon;
    }
}
