package com.hp.jetadvantage.link.pkgmgt.hpkutil.attestation;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Attestation;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OSValidator;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

public class AttestationManager extends CommonManager {
    private static final String WIN_RUNTIME = "cmd.exe /C ";
    private static final String LINUX_RUNTIME = "/bin/bash -l -c ";

    private UUID uuid;

    public AttestationManager(String host, UUID uuid) {
        super(host);
        this.uuid = uuid;
    }

    private boolean validate(Process proc) throws Exception {
        BufferedReader brInput = null;
        BufferedReader brError = null;
        try {
            proc.waitFor();
            brInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = brInput.readLine()) != null) {
                if (line.contains("unable to connect") || line.contains("cannot connect")) {
                    return false;
                }
            }

            brError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = brError.readLine()) != null) {
                if (line.contains("daemon not running; starting now at") || line.contains("daemon started successfully")) {
                    // ignore errors that starting adb server.
                    continue;
                }
                return false;
            }
        } finally {
            if (brInput != null) {
                try { brInput.close(); } catch (Exception e) {}
            }

            if (brError != null) {
                try {brError.close(); } catch (Exception e) {}
            }

            if (proc != null) {
                proc.destroy();
            }
        }

        return true;
    }

    public void updateAttestation(Attestation attestation, File commandLocation, TaskInterface taskInterface) throws Exception {
        try {
            isInstallerStateIdle(getPkgMgtUrl());

            String base;
            if (commandLocation == null) {
                if (OSValidator.isUnix()) {
                    base = LINUX_RUNTIME;
                } else {
                    base = WIN_RUNTIME;
                }

            } else {
                base = commandLocation.getAbsolutePath() + "/";
            }

            Runtime runtime = Runtime.getRuntime();
            
            //adb connect
            String connectCmd = base + String.format("adb connect %s", attestation.getHost());

            if (!validate(runtime.exec(connectCmd))) {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_connect")));
                return;
            }

            //adb shell am broadcast
            String updateAttestationCmd = base + String.format("adb -s %s shell am broadcast -a com.hp.jetadvantage.link.intent.action.ATTESTATION " +
                            "--es UUID %s --es EXTRA_DATA '%s' --es EXTRA_USER '%s' --es EXTRA_LDBKEY '%s'",
                    attestation.getHost(), uuid, attestation.getData().toString().replaceAll("\\\"","\\\\\""), attestation.getUser(), attestation.getKey());

            if (!validate(runtime.exec(updateAttestationCmd))) {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_update_attestation")));
                return;
            }

            taskInterface.onSucceed(null);

        } catch (Exception ex) {
            taskInterface.onFailed(ex);
        }
    }
}
