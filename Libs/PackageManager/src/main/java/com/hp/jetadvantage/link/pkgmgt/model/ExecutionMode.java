// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jetadvantage.link.pkgmgt.model;

/**
 * Enumeration of print job states.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public enum ExecutionMode {

    // Summary:
    //     This does not map to Debug or Release builds but to whether or not the system
    //     is in "development" mode. Typically development mode is an environment where
    //     tracing is more verbose, debuggers can be attached and other configuration
    //     options may be available.  The system could move from Production to Development
    //     or vice versa through insertion of a secure key.
    Development,
    //
    // Summary:
    //     This does not map to Debug or Release builds but to whether or not the system
    //     is in "diagnostics" mode. Typically diagnostics is identical to development
    //     mode.  The system could move from Production to Development to Diagnostics
    //     in any order but may depend on the insertion of a secure key.
    Diagnostics,
    //
    // Summary:
    //     The device is being unit tested. This is only a valid option during unit
    //     testing scenarios and would cause a fault should code attempt to set this
    //     mode.
    UnitTest,
    //
    // Summary:
    //     The device is being system tested. This is only a valid option during system
    //     testing scenarios and would cause a fault should code attempt to set this
    //     mode.
    SystemTest,
    //
    // Summary:
    //     This does not map to Debug or Release builds but to whether or not the system
    //     is in "customer" or deployment mode. Typically Production mode is an environment
    //     where tracing is less verbose, debuggers cannot be attached and other configuration
    //     options may are un-available.  The system could move from Production to Development
    //     or vice versa through insertion of a secure key.
    Production,

}
