package com.hp.jetadvantage.link.pkgmgt.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.ext.service.application.ApplicationAgentRegistrationRecord;
import com.hp.ext.service.application.MessageCenterAgentRegistrationRecord;
import com.hp.ext.service.authentication.AuthenticationAgentRegistrationRecord;
import com.hp.ext.service.copy.CopyAgentRegistrationRecord;
import com.hp.ext.service.deviceUsage.DeviceUsageAgentRegistrationRecord;
import com.hp.ext.service.jobStatistics.JobStatisticsAgentRegistrationRecord;
import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord;
import com.hp.ext.service.scanJob.ScanJobAgentRegistrationRecord;
import com.hp.ext.service.security.SecurityAgentRegistrationRecord;
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord;
import com.hp.ext.service.supplies.SuppliesAgentRegistrationRecord;
import com.hp.ext.service.usbAccessories.UsbAccessoriesAgentRegistrationRecord;
import com.hp.ext.types.solutionManager.RegistrationRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SolutionManifestHelper {

    /**
     * getPackageName(ApplicationAgentRegistrationRecord appRecord)
     * Pick up the package name from "main=com.hp.workpath.sample.multilanguagesample/.MainActivity"
     *
     */
    public static String getPackageName(ApplicationAgentRegistrationRecord appRecord) {
        String packageName = appRecord.getTarget().getWorkpathApplicationTarget().getPackage().toString();
        packageName = packageName.substring(packageName.indexOf("=") + 1, packageName.indexOf("/"));
        return packageName;
    }

    /**
     * getMainRecord(List<RegistrationRecord> records)
     * 1. Check if TypeGUN is ApplicationAgentRegistrationRecord
     * 2. In this record, check if "main=" is exist in the package.value
     * 3. If it is exist, this record is main record.
     *
     */
    public static ApplicationAgentRegistrationRecord getMainRecord(List<RegistrationRecord> records) throws Exception {
        String targetTypeGUN = new ApplicationAgentRegistrationRecord().getTypeGUN();
        for (RegistrationRecord record : records) {
            if (record.getRecord().getTypeGUN().contains(targetTypeGUN)) {
                ApplicationAgentRegistrationRecord applicationAgentRegistrationRecord = getRegistrationRecordFromRecordValue(record.getRecord().getValue(), ApplicationAgentRegistrationRecord.class);
                String _package = applicationAgentRegistrationRecord.getTarget().getWorkpathApplicationTarget().getPackage().getValue();
                if (_package.startsWith("main="))
                    return applicationAgentRegistrationRecord;
            }
        }
        return null;
    }

    public static PrintJobAgentRegistrationRecord getPrintJobAgentRegistrationRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, PrintJobAgentRegistrationRecord.class, new PrintJobAgentRegistrationRecord().getTypeGUN());
    }

    public static ScanJobAgentRegistrationRecord getScanJobAgentRegistrationRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, ScanJobAgentRegistrationRecord.class, new ScanJobAgentRegistrationRecord().getTypeGUN());
    }

    public static CopyAgentRegistrationRecord getCopyAgentRegistrationRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, CopyAgentRegistrationRecord.class, new CopyAgentRegistrationRecord().getTypeGUN());
    }

    public static List<AuthenticationAgentRegistrationRecord> getAuthenticationRecords(List<RegistrationRecord> records) throws IOException {
        return getRecordList(records, AuthenticationAgentRegistrationRecord.class, new AuthenticationAgentRegistrationRecord().getTypeGUN());
    }

    public static UsbAccessoriesAgentRegistrationRecord getUsbAccessoriesRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, UsbAccessoriesAgentRegistrationRecord.class, new UsbAccessoriesAgentRegistrationRecord().getTypeGUN());
    }

    public static SuppliesAgentRegistrationRecord getSuppliesRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, SuppliesAgentRegistrationRecord.class, new SuppliesAgentRegistrationRecord().getTypeGUN());
    }

    public static SecurityAgentRegistrationRecord getSecurityRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, SecurityAgentRegistrationRecord.class, new SecurityAgentRegistrationRecord().getTypeGUN());
    }

    public static SolutionNotificationAgentRegistrationRecord getSolutionNotificationRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, SolutionNotificationAgentRegistrationRecord.class, new SolutionNotificationAgentRegistrationRecord().getTypeGUN());
    }

    public static JobStatisticsAgentRegistrationRecord getJobStatisticsRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, JobStatisticsAgentRegistrationRecord.class, new JobStatisticsAgentRegistrationRecord().getTypeGUN());
    }

    public static DeviceUsageAgentRegistrationRecord getDeviceUsageRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, DeviceUsageAgentRegistrationRecord.class, new DeviceUsageAgentRegistrationRecord().getTypeGUN());
    }

    public static MessageCenterAgentRegistrationRecord getMessageCenterRecord(List<RegistrationRecord> records) throws IOException {
        return getRecord(records, MessageCenterAgentRegistrationRecord.class, new MessageCenterAgentRegistrationRecord().getTypeGUN());
    }

    public static <T> T getRecord(List<RegistrationRecord> records, Class<T> clazz, String targetTypeGUN) throws IOException {
        for (RegistrationRecord record : records) {
            if (record.getRecord().getTypeGUN().contains(targetTypeGUN)) {
                return getRegistrationRecordFromRecordValue(record.getRecord().getValue(), clazz);
            }
        }
        return null;
    }

    public static <T> List<T> getRecordList(List<RegistrationRecord> records, Class<T> clazz, String targetTypeGUN) throws IOException {
        List<T> registrationRecordList = new ArrayList<>();
        for (RegistrationRecord record : records) {
            if (record.getRecord().getTypeGUN().contains(targetTypeGUN)) {
                T registrationRecord = getRegistrationRecordFromRecordValue(record.getRecord().getValue(), clazz);
                registrationRecordList.add(registrationRecord);
            }
        }
        return registrationRecordList;
    }

    public static <T> T getRegistrationRecordFromRecordValue(Object value, Class<T> className) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter stringEmp = new StringWriter();
        objectMapper.writeValue(stringEmp, value);
        return objectMapper.readValue(stringEmp.toString(), className);
    }

    public static boolean isMainRecord(ApplicationAgentRegistrationRecord appRecord) {
        String _package = appRecord.getTarget().getWorkpathApplicationTarget().getPackage().toString();
        return _package.startsWith("main=");
    }
}
