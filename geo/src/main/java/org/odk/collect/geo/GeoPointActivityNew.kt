package org.odk.collect.geo

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class GeoPointActivityNew : LocalizedActivity(), GeoPointDialogFragment.Listener {

    @Inject
    lateinit var geoPointViewModelFactory: GeoPointViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as GeoDependencyComponentProvider).geoDependencyComponent.inject(this)

        val viewModel =
            ViewModelProvider(this, geoPointViewModelFactory).get(GeoPointViewModel::class.java)

        viewModel.accuracyThreshold =
            this.intent.getDoubleExtra(
                EXTRA_ACCURACY_THRESHOLD,
                Double.MAX_VALUE
            )

        viewModel.location.observe(this) {
            if (it != null) {
                ExternalAppUtils.returnSingleValue(this, GeoUtils.formatLocationResultString(it))
            }
        }

        DialogFragmentUtils.showIfNotShowing(
            GeoPointDialogFragment::class.java,
            supportFragmentManager
        )
    }

    override fun onCancel() {
        finish()
    }

    companion object {
        const val EXTRA_ACCURACY_THRESHOLD = "extra_accuracy_threshold"
    }
}
