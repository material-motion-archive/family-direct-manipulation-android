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

import com.google.android.material.motion.gestures.DragGestureRecognizer;
import com.google.android.material.motion.gestures.RotateGestureRecognizer;
import com.google.android.material.motion.gestures.ScaleGestureRecognizer;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GesturePerformerTests {
  private static final float E = 0.0001f;

  private Runtime runtime;
  private View target;

  private long eventDownTime;
  private long eventTime;

  @Before
  public void setUp() {
    runtime = new Runtime();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);
    target.layout(0, 0, 50, 75);

    eventDownTime = 0;
    eventTime = 0;
  }

  @Test
  public void makesViewDraggable() {
    Draggable plan = new Draggable();
    ((DragGestureRecognizer) plan.gestureRecognizer).dragSlop = 0;

    runtime.addPlan(plan, target);

    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 100, 200));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 100, 200));

    assertThat(target.getTranslationX()).isWithin(E).of(100);
    assertThat(target.getTranslationY()).isWithin(E).of(200);
  }

  @Test
  public void makesViewPinchable() {
    Pinchable plan = new Pinchable();
    ((ScaleGestureRecognizer) plan.gestureRecognizer).scaleSlop = 0;

    runtime.addPlan(plan, target);

    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    // Span = 100.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 1, 0, 0, 100, 0));
    // Span = 200.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, 0, 200, 0));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_UP, 1, 0, 0, 200, 0));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 0, 0));

    assertThat(target.getScaleX()).isWithin(E).of(2f);
    assertThat(target.getScaleY()).isWithin(E).of(2f);
  }

  @Test
  public void makesViewRotatable() {
    Rotatable plan = new Rotatable();
    ((RotateGestureRecognizer) plan.gestureRecognizer).rotateSlop = 0;

    runtime.addPlan(plan, target);

    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    // Angle = 0 degrees.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 1, 0, 0, 100, 0));
    // Angle = 45 degrees.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, 0, 100, 100));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_UP, 1, 0, 0, 100, 100));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 0, 0));

    assertThat(target.getRotation()).isWithin(E).of(45);
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

  @Test
  public void namedPlanSupport() {
    Draggable draggable = new Draggable();
    Pinchable pinchable = new Pinchable();
    Rotatable rotatable = new Rotatable();

    runtime.addNamedPlan(draggable, "draggable", target);
    runtime.addNamedPlan(pinchable, "pinchable", target);
    runtime.addNamedPlan(rotatable, "rotatable", target);

    runtime.removeNamedPlan("draggable", target);
    runtime.removeNamedPlan("pinchable", target);
    runtime.removeNamedPlan("rotatable", target);

    // Move 2 fingers around.
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 1, 0, 0, 100, 0));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, 0, 200, 300));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_UP, 1, 0, 0, 200, 300));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 0, 0));

    // No effect.
    assertThat(target.getTranslationX()).isWithin(E).of(0f);
    assertThat(target.getTranslationY()).isWithin(E).of(0f);
    assertThat(target.getScaleX()).isWithin(E).of(1f);
    assertThat(target.getScaleY()).isWithin(E).of(1f);
    assertThat(target.getRotation()).isWithin(E).of(0f);
  }

  @Test
  public void removeNonExistentNamedPlanIsOk() {
    runtime.removeNamedPlan("draggable", target);
  }

  @Test(expected = IllegalArgumentException.class)
  public void unexpectedNameCrashes() {
    runtime.addNamedPlan(new Draggable(), "foobar", target);
    runtime.removeNamedPlan("foobar", target);
  }

  @Test(expected = NullPointerException.class)
  public void mismatchedNameCrashes() {
    runtime.addNamedPlan(new Draggable(), "pinchable", target);
    runtime.removeNamedPlan("pinchable", target);
  }

  @Test
  public void draggablePinchableAndRotatable() {
    Draggable draggable = new Draggable();
    ((DragGestureRecognizer) draggable.gestureRecognizer).dragSlop = 0;
    Pinchable pinchable = new Pinchable();
    ((ScaleGestureRecognizer) pinchable.gestureRecognizer).scaleSlop = 0;
    Rotatable rotatable = new Rotatable();
    ((RotateGestureRecognizer) rotatable.gestureRecognizer).rotateSlop = 0;

    runtime.addPlan(draggable, target);
    runtime.addPlan(pinchable, target);
    runtime.addPlan(rotatable, target);

    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    // Centroid = [0,0], span = 20, angle = 0.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 1, -10, 0, 10, 0));
    // Centroid = [0,0], * span = 40, angle = 0.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, -20, 0, 20, 0));
    // Centroid = [0,0], span = 40, * angle = 90.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, -20, 0, 20));
    // * Centroid = [10,10], span = 40, angle = 90.
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_MOVE, 1, 10, -10, 10, 30));
    target.dispatchTouchEvent(createMultiTouchMotionEvent(MotionEvent.ACTION_POINTER_UP, 1, 10, -10, 10, 30));
    target.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 10, -10));

    assertThat(target.getScaleX()).isWithin(E).of(2f);
    assertThat(target.getScaleY()).isWithin(E).of(2f);

    assertThat(target.getRotation()).isWithin(E).of(90);

    assertThat(target.getTranslationX()).isWithin(E).of(10f);
    assertThat(target.getTranslationY()).isWithin(E).of(10f);
  }

  private MotionEvent createMotionEvent(int action, float x, float y) {
    return MotionEvent.obtain(eventDownTime, eventTime++, action, x, y, 0);
  }

  private MotionEvent createMultiTouchMotionEvent(
    int action, int index, float x0, float y0, float x1, float y1) {
    MotionEvent event = mock(MotionEvent.class);

    when(event.getPointerCount()).thenReturn(2);
    when(event.getAction()).thenReturn(action | (index << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
    when(event.getActionMasked()).thenReturn(action);
    when(event.getActionIndex()).thenReturn(index);

    when(event.getRawX()).thenReturn(x0);
    when(event.getRawY()).thenReturn(y0);

    when(event.getX(0)).thenReturn(x0);
    when(event.getY(0)).thenReturn(y0);

    when(event.getX(1)).thenReturn(x1);
    when(event.getY(1)).thenReturn(y1);

    return event;
  }

  private MotionEvent createMultiTouchMotionEvent(
    int action, int index, float x0, float y0, float x1, float y1, float x2, float y2) {
    MotionEvent event = mock(MotionEvent.class);

    when(event.getPointerCount()).thenReturn(3);
    when(event.getAction()).thenReturn(action | (index << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
    when(event.getActionMasked()).thenReturn(action);
    when(event.getActionIndex()).thenReturn(index);

    when(event.getRawX()).thenReturn(x0);
    when(event.getRawY()).thenReturn(y0);

    when(event.getX(0)).thenReturn(x0);
    when(event.getY(0)).thenReturn(y0);

    when(event.getX(1)).thenReturn(x1);
    when(event.getY(1)).thenReturn(y1);

    when(event.getX(2)).thenReturn(x2);
    when(event.getY(2)).thenReturn(y2);

    return event;
  }
}
