package com.hp.jetadvantage.link.pkgmgt.hpkutil.activities;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LocalizedString;

import java.util.ArrayList;

public abstract class CommonLocalizationController extends CommonController {
    public abstract void onLocalizationActivityResult(LocalizationActivity.Type type, ArrayList<LocalizedString> values);
}
