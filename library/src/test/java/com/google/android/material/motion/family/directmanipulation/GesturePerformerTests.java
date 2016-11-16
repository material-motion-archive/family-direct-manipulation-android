/*
 * Copyright 2016-present The Material Motion Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.motion.family.directmanipulation;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures;
import com.google.android.material.motion.runtime.Plan;
import com.google.android.material.motion.runtime.Runtime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GesturePerformerTests {

  private Runtime runtime;
  private View target;

  private long eventDownTime;
  private long eventTime;

  @Before
  public void setUp() {
    runtime = new Runtime();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);

    eventDownTime = 0;
    eventTime = 0;
  }

  @Test
  public void makesViewDraggable() {
    runtime.addPlan(new Draggable(), target);

    target.layout(0, 0, 50, 75);

    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 100, 200));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 100, 200));

    assertThat(target.getLeft()).isEqualTo(100);
    assertThat(target.getTop()).isEqualTo(200);
    assertThat(target.getRight()).isEqualTo(150);
    assertThat(target.getBottom()).isEqualTo(275);
  }

  @Test
  public void addingDraggableMultipleTimesIsOk() {
    DragGestureRecognizer gestureRecognizer = new DragGestureRecognizer();
    gestureRecognizer.setElement(target);

    runtime.addPlan(new Draggable(gestureRecognizer), target);
    runtime.addPlan(new Draggable(gestureRecognizer), target);

    makesViewDraggable();
  }

  @Test(expected = IllegalArgumentException.class)
  public void addingUnsupportedPlanCrashes() {
    Performer performer = new GesturePerformer();
    performer.initialize(target);

    performer.addPlan(new Plan() {
      @Override
      public Class<? extends PerformerFeatures.BasePerforming> getPerformerClass() {
        return GesturePerformer.class;
      }
    });
  }

  private MotionEvent createMotionEvent(int action, float x, float y) {
    return MotionEvent.obtain(eventDownTime, eventTime++, action, x, y, 0);
  }
}
