package com.hp.jetadvantage.link.pkgmgt.hpkutil.attestation;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Attestation;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;

import java.io.File;
import java.util.UUID;

public class AttestationUpdateService {
    private String host;
    private UUID uuid;
    private Attestation attestation;
    private TaskInterface taskInterface;
    private File commandLocation;

    public AttestationUpdateService(String host, UUID uuid, Attestation attestation, File commandLocation, TaskInterface taskInterface) {
        this.host = host;
        this.uuid = uuid;
        this.attestation = attestation;
        this.taskInterface = taskInterface;
        this.commandLocation = commandLocation;
    }

    public void execute() {
        try {
            AttestationManager attestationManager = new AttestationManager(host, uuid);
            attestationManager.updateAttestation(attestation, commandLocation, taskInterface);
        } catch (Exception e) {
            taskInterface.onFailed(e);
        }
    }
}
