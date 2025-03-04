package de.dennisguse.opentracks;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static de.dennisguse.opentracks.util.EspressoUtils.selectTabAtIndex;
import static de.dennisguse.opentracks.util.EspressoUtils.waitFor;

import android.view.MotionEvent;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.MotionEvents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoUITest {

    @Rule
    public ActivityScenarioRule<TrackListActivity> mActivityTestRule = new ActivityScenarioRule<>(TrackListActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @LargeTest
    @Test
    public void record_stop_resume_stop_finish() {
        {
            // TrackListActivity: start recording
            ViewInteraction trackControllerRecordButton = onView(withId(R.id.track_list_fab_action));
            trackControllerRecordButton.perform(click());
        }
        {
            // TrackRecordingActivity: wait to record some time and then stop
            onView(withId(R.id.track_recording_fab_action))
                    .perform(waitFor(5000))
                    .perform(longClick());

            // TrackStoppedActivity: resume
            onView(withId(R.id.resume_button))
                    .perform(click());

            // TrackRecordingActivity: wait and then stop
            onView(withId(R.id.track_recording_fab_action))
                    .perform(waitFor(5000))
                    .perform(longClick());

            // TrackStoppedActivity
            onView(withId(R.id.finish_button))
                    .perform(click());
        }
    }

    @LargeTest
    @Test
    public void record_move_through_tabs() {
        {
            // TrackListActivity: start recording
            ViewInteraction trackControllerRecordButton = onView(withId(R.id.track_list_fab_action));
            trackControllerRecordButton.perform(click());
        }
        {
            // TrackRecordingActivity
            ViewInteraction tabLayout = onView(withId(R.id.track_detail_activity_tablayout));
            ViewInteraction trackControllerStopButton = onView(withId(R.id.track_recording_fab_action));

            tabLayout.perform(selectTabAtIndex(1));
            tabLayout.perform(waitFor(1000));

            tabLayout.perform(selectTabAtIndex(2));
            tabLayout.perform(waitFor(1000));

            tabLayout.perform(selectTabAtIndex(3));
            tabLayout.perform(waitFor(1000));

            tabLayout.perform(selectTabAtIndex(0));
            tabLayout.perform(waitFor(1000));

            // stop
            trackControllerStopButton.perform(veryLongTouch(1600));
        }
    }

    @LargeTest
    @Test
    public void selectAndDeleteTrack() {
        onView(withId(R.id.track_list)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.track_list)).atPosition(0).perform(veryLongTouch(2000));
    }

    private static ViewAction veryLongTouch(final int duration_ms) {
        return new ViewAction() {
            @Override
            public String getDescription() {
                return "Perform long touch.";
            }

            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public void perform(UiController uiController, final View view) {
                // Get view absolute position
                int[] location = new int[2];
                view.getLocationOnScreen(location);

                // Offset coordinates by view position
                float[] coordinates = new float[]{location[0] + 1, location[1] + 1};

                // Send down event, pause, and send up
                MotionEvent down = MotionEvents.sendDown(uiController, coordinates, new float[]{1f, 1f}).down;
                uiController.loopMainThreadForAtLeast(duration_ms);
                MotionEvents.sendUp(uiController, down, coordinates);
            }
        };
    }

}
