package com.hp.jetadvantage.link.pkgmgt.model;

import java.util.List;

public class AvatarRegistration {
    public static class Hint {
        public String method;
    }

    public static class Link {
        public String rel;
        public String href;
        public List<Hint> hints;
    }

    public static class Registration {
        public String registrationState;
        public String registrationStateReason;
        public String registrationStepCompleted;
        public String registrationTriggeredBy;
        public String signalingConnectionState;
        public List<Link> links;
    }

    public static class RegistrationRequest {
        public String version;
        public String registrationTriggeredBy;
    }

    public String version;
    public String cloudServicesEnabled;
    public List<Link> links;
    public Registration registration;
}
