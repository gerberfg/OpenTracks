package de.dennisguse.opentracks.settings;

import android.net.Uri;
import android.os.Bundle;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.io.file.TrackFileFormat;
import de.dennisguse.opentracks.io.file.TrackFilenameGenerator;
import de.dennisguse.opentracks.util.IntentUtils;

public class ImportExportSettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = ImportExportSettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_import_export);

        setExportTrackFileFormatOptions();
        setExportDirectorySummary();
        setFilenameTemplate();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_import_export_title);
    }

    @Override
    public void onResume() {
        super.onResume();

        setExportDirectorySummary();

        Preference instantExportEnabledPreference = findPreference(getString(R.string.post_workout_export_enabled_key));
        instantExportEnabledPreference.setEnabled(PreferencesUtils.isDefaultExportDirectoryUri());
    }

    private void setExportTrackFileFormatOptions() {
        final TrackFileFormat[] trackFileFormats = {
                TrackFileFormat.KMZ_WITH_TRACKDETAIL_AND_SENSORDATA_AND_PICTURES,
                TrackFileFormat.KMZ_WITH_TRACKDETAIL_AND_SENSORDATA,
                TrackFileFormat.KML_WITH_TRACKDETAIL_AND_SENSORDATA,
                TrackFileFormat.GPX,
                TrackFileFormat.CSV
        };
        String[] entries = new String[trackFileFormats.length];
        String[] entryValues = new String[trackFileFormats.length];

        for (int i = 0; i < entries.length; i++) {
            TrackFileFormat trackFileFormat = trackFileFormats[i];
            String trackFileFormatUpperCase = trackFileFormat.getExtension().toUpperCase(Locale.US); //ASCII upper case
            int photoMessageId = trackFileFormat.includesPhotos() ? R.string.export_with_photos : R.string.export_without_photos;
            entries[i] = String.format("%s (%s)", trackFileFormatUpperCase, getString(photoMessageId));
            entryValues[i] = trackFileFormat.getPreferenceId();
        }

        ListPreference listPreference = findPreference(getString(R.string.export_trackfileformat_key));
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);
    }

    private void setExportDirectorySummary() {
        Preference instantExportDirectoryPreference = findPreference(getString(R.string.settings_default_export_directory_key));
        instantExportDirectoryPreference.setSummaryProvider(preference -> {
            Uri directoryUri = PreferencesUtils.getDefaultExportDirectoryUri();
            DocumentFile directory = IntentUtils.toDocumentFile(getContext(), directoryUri);
            //Use same value for not set as Androidx ListPreference and EditTextPreference
            if (directory == null) {
                return getString(R.string.not_set);
            }

            return directoryUri + (directory.canWrite() ? "" : getString(R.string.export_dir_not_writable));
        });
    }

    private void setFilenameTemplate() {
        EditTextPreference preference = findPreference(getString(R.string.export_filename_format_key));
        preference.setOnBindEditTextListener(t -> {
            t.setHint(getString(R.string.export_filename_format_default));
        });
        preference.setDialogMessage(TrackFilenameGenerator.getAllOptions());

        preference.setOnPreferenceChangeListener((p, newValue) -> new TrackFilenameGenerator(newValue.toString()).isValid());

        preference.setSummaryProvider(p ->
                PreferencesUtils.getTrackFileformatGenerator()
                        .getExample()
        );
    }
}
