package org.odk.collect.geo

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.location.Location
import org.odk.collect.testshared.Extensions.isFinishing
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class GeoPointActivityNewTest {

    private val locationLiveData: MutableLiveData<Location?> = MutableLiveData(null)
    private val viewModel = mock<GeoPointViewModel> {
        on { location } doReturn locationLiveData
        on { currency } doReturn MutableLiveData(null)
    }
    private val scheduler = FakeScheduler()

    @Before
    fun setup() {
        val application = getApplicationContext<RobolectricApplication>()
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesScheduler() = scheduler

                override fun providesGeoPointViewModelFactory(application: Application) =
                    mock<GeoPointViewModelFactory> {
                        on { create(GeoPointViewModel::class.java) } doReturn viewModel
                    }
            })
            .build()
    }

    @Test
    fun `sets threshold`() {
        val intent = Intent(getApplicationContext(), GeoPointActivityNew::class.java)
        intent.putExtra(GeoPointActivityNew.EXTRA_ACCURACY_THRESHOLD, 5.0)

        ActivityScenario.launch<GeoPointActivityNew>(intent)
        verify(viewModel).accuracyThreshold = 5.0
    }

    @Test
    fun `shows dialog`() {
        val scenario = ActivityScenario.launch(GeoPointActivityNew::class.java)
        scenario.onActivity {
            val fragments = it.supportFragmentManager.fragments
            assertThat(fragments[0].javaClass, equalTo(GeoPointDialogFragment::class.java))
        }
    }

    @Test
    fun `finishes with location when available`() {
        val scenario = ActivityScenario.launch(GeoPointActivityNew::class.java)

        val location = Location(0.0, 0.0, 0.0, 0.0f)
        locationLiveData.value = location

        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
        val resultIntent = scenario.result.resultData
        val returnedValue = ExternalAppUtils.getReturnedSingleValue(resultIntent)
        assertThat(
            returnedValue,
            equalTo(GeoUtils.formatLocationResultString(location))
        )
    }

    @Test
    fun `finishes when dialog is cancelled`() {
        val scenario = ActivityScenario.launch(GeoPointActivityNew::class.java)
        scenario.onActivity {
            it.onCancel()
        }

        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_CANCELED))
    }
}
