package org.odk.collect.android.activities.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.configure.SettingsUtils;
import org.odk.collect.android.formmanagement.InstancesAppState;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.shared.Settings;

import javax.inject.Inject;

public class MainMenuViewModel extends ViewModel {

    private final VersionInformation version;
    private final Settings generalSettings;
    private final Settings adminSettings;
    private final InstancesAppState instancesAppState;
    private final Application application;

    public MainMenuViewModel(Application application, VersionInformation versionInformation,
                             SettingsProvider settingsProvider, InstancesAppState instancesAppState) {
        this.application = application;
        this.version = versionInformation;
        this.generalSettings = settingsProvider.getGeneralSettings();
        this.adminSettings = settingsProvider.getAdminSettings();
        this.instancesAppState = instancesAppState;
    }

    public String getVersion() {
        return version.getVersionToDisplay();
    }

    @Nullable
    public String getVersionCommitDescription() {
        String commitDescription = "";

        if (version.getCommitCount() != null) {
            commitDescription = appendToCommitDescription(commitDescription, version.getCommitCount().toString());
        }

        if (version.getCommitSHA() != null) {
            commitDescription = appendToCommitDescription(commitDescription, version.getCommitSHA());
        }

        if (version.isDirty()) {
            commitDescription = appendToCommitDescription(commitDescription, "dirty");
        }

        if (!commitDescription.isEmpty()) {
            return commitDescription;
        } else {
            return null;
        }
    }

    public boolean shouldEditSavedFormButtonBeVisible() {
        return adminSettings.getBoolean(AdminKeys.KEY_EDIT_SAVED);
    }

    public boolean shouldSendFinalizedFormButtonBeVisible() {
        return adminSettings.getBoolean(AdminKeys.KEY_SEND_FINALIZED);
    }

    public boolean shouldViewSentFormButtonBeVisible() {
        return adminSettings.getBoolean(AdminKeys.KEY_VIEW_SENT);
    }

    public boolean shouldGetBlankFormButtonBeVisible() {
        boolean buttonEnabled = adminSettings.getBoolean(AdminKeys.KEY_GET_BLANK);
        return !isMatchExactlyEnabled() && buttonEnabled;
    }

    public boolean shouldDeleteSavedFormButtonBeVisible() {
        return adminSettings.getBoolean(AdminKeys.KEY_DELETE_SAVED);
    }

    public LiveData<Integer> getUnsentFormsCount() {
        return instancesAppState.getUnsentCount();
    }

    public LiveData<Integer> getFinalizedFormsCount() {
        return instancesAppState.getFinalizedCount();
    }

    public LiveData<Integer> getSentFormsCount() {
        return instancesAppState.getSentCount();
    }

    private boolean isMatchExactlyEnabled() {
        return SettingsUtils.getFormUpdateMode(application, generalSettings) == FormUpdateMode.MATCH_EXACTLY;
    }

    @NotNull
    private String appendToCommitDescription(String commitDescription, String part) {
        if (commitDescription.isEmpty()) {
            commitDescription = part;
        } else {
            commitDescription = commitDescription + "-" + part;
        }
        return commitDescription;
    }

    public void resume() {
        instancesAppState.update();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final VersionInformation versionInformation;
        private final Application application;
        private final SettingsProvider settingsProvider;
        private final InstancesAppState instancesAppState;

        @Inject
        public Factory(VersionInformation versionInformation, Application application,
                       SettingsProvider settingsProvider, InstancesAppState instancesAppState) {
            this.versionInformation = versionInformation;
            this.application = application;
            this.settingsProvider = settingsProvider;
            this.instancesAppState = instancesAppState;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(application, versionInformation, settingsProvider, instancesAppState);
        }
    }
}
