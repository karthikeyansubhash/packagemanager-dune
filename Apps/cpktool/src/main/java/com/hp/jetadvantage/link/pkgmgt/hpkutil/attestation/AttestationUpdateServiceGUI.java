package com.hp.jetadvantage.link.pkgmgt.hpkutil.attestation;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Attestation;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import javafx.concurrent.Task;

import java.io.File;
import java.util.UUID;

public class AttestationUpdateServiceGUI extends javafx.concurrent.Service<Boolean> {
    private AttestationUpdateService attestationUpdateService;

    public AttestationUpdateServiceGUI(String host, UUID uuid, Attestation attestation, File commandLocation, TaskInterface taskInterface) {
        attestationUpdateService = new AttestationUpdateService(host, uuid, attestation, commandLocation, taskInterface);
    }

    public void execute() {
        attestationUpdateService.execute();
    }

    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                execute();
                return null;
            }
        };
    }
}
