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
import android.view.View;

import com.google.android.material.motion.gestures.DragGestureRecognizer;
import com.google.android.material.motion.gestures.RotateGestureRecognizer;
import com.google.android.material.motion.gestures.ScaleGestureRecognizer;
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
public class DirectlyManipulableTests {

  private Runtime runtime;
  private View target;

  @Before
  public void setUp() {
    runtime = new Runtime();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);
  }

  @Test
  public void attachesNewGestureRecognizersToElement() {
    DirectlyManipulable plan = new DirectlyManipulable();

    assertThat(plan.dragGestureRecognizer.getElement()).isNull();
    assertThat(plan.scaleGestureRecognizer.getElement()).isNull();
    assertThat(plan.rotateGestureRecognizer.getElement()).isNull();

    runtime.addPlan(plan, target);

    assertThat(plan.dragGestureRecognizer.getElement()).isEqualTo(target);
    assertThat(plan.scaleGestureRecognizer.getElement()).isEqualTo(target);
    assertThat(plan.rotateGestureRecognizer.getElement()).isEqualTo(target);
  }

  @Test
  public void attachesExistingGestureRecognizersToElement() {
    DragGestureRecognizer dragGestureRecognizer = new DragGestureRecognizer();
    ScaleGestureRecognizer scaleGestureRecognizer = new ScaleGestureRecognizer();
    RotateGestureRecognizer rotateGestureRecognizer = new RotateGestureRecognizer();

    DirectlyManipulable plan = new DirectlyManipulable(
      dragGestureRecognizer, scaleGestureRecognizer, rotateGestureRecognizer);

    assertThat(dragGestureRecognizer.getElement()).isNull();
    assertThat(scaleGestureRecognizer.getElement()).isNull();
    assertThat(rotateGestureRecognizer.getElement()).isNull();

    runtime.addPlan(plan, target);

    assertThat(dragGestureRecognizer.getElement()).isEqualTo(target);
    assertThat(scaleGestureRecognizer.getElement()).isEqualTo(target);
    assertThat(rotateGestureRecognizer.getElement()).isEqualTo(target);
  }
}
