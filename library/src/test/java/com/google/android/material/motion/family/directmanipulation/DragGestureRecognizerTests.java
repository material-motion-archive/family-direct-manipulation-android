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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.android.material.motion.family.directmanipulation.GestureRecognizer.BEGAN;
import static com.google.android.material.motion.family.directmanipulation.GestureRecognizer.CANCELLED;
import static com.google.android.material.motion.family.directmanipulation.GestureRecognizer.CHANGED;
import static com.google.android.material.motion.family.directmanipulation.GestureRecognizer.POSSIBLE;
import static com.google.android.material.motion.family.directmanipulation.GestureRecognizer.RECOGNIZED;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DragGestureRecognizerTests {

  private View element;
  private DragGestureRecognizer dragGestureRecognizer;

  private long eventDownTime;
  private long eventTime;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    element = new View(context);
    dragGestureRecognizer = new DragGestureRecognizer();
    dragGestureRecognizer.setElement(element);

    eventDownTime = 0;
    eventTime = 0;
  }

  @Test
  public void defaultState() {
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(dragGestureRecognizer.getElement()).isEqualTo(element);
    assertThat(dragGestureRecognizer.getCentroidX()).isWithin(0).of(0f);
    assertThat(dragGestureRecognizer.getCentroidY()).isWithin(0).of(0f);
    assertThat(dragGestureRecognizer.getTranslationX()).isWithin(0).of(0f);
    assertThat(dragGestureRecognizer.getTranslationY()).isWithin(0).of(0f);
    assertThat(dragGestureRecognizer.getVelocityX()).isWithin(0).of(0f);
    assertThat(dragGestureRecognizer.getVelocityY()).isWithin(0).of(0f);
  }

  @Test
  public void smallMovementIsNotRecognized() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});

    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});

    // Move 1 pixel. Should not change the state.
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 1, 0));
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});
  }

  @Test
  public void largeMovementIsRecognized() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});

    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});

    // Move 100 pixel. Should change the state.
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 100, 0));
    assertThat(dragGestureRecognizer.getState()).isEqualTo(CHANGED);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE, BEGAN, CHANGED});

    // Move 1 pixel. Should still change the state.
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 1, 0));
    assertThat(dragGestureRecognizer.getState()).isEqualTo(CHANGED);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE, BEGAN, CHANGED, CHANGED});
  }

  @Test
  public void completedGestureIsRecognized() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 100, 0));
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 100, 0));

    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray())
      .isEqualTo(new Integer[]{POSSIBLE, BEGAN, CHANGED, RECOGNIZED, POSSIBLE});
  }

  @Test
  public void cancelledGestureIsNotRecognized() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_MOVE, 100, 0));
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_CANCEL, 100, 0));

    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray())
      .isEqualTo(new Integer[]{POSSIBLE, BEGAN, CHANGED, CANCELLED, POSSIBLE});
  }

  @Test
  public void noMovementIsNotRecognized() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0));
    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 0, 0));

    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});
  }

  @Test
  public void irrelevantMotionIsIgnored() {
    TrackingGestureStateChangeListener listener = new TrackingGestureStateChangeListener();
    dragGestureRecognizer.addStateChangeListener(listener);

    dragGestureRecognizer.onTouchEvent(createMotionEvent(MotionEvent.ACTION_HOVER_MOVE, 0, 0));

    assertThat(dragGestureRecognizer.getState()).isEqualTo(POSSIBLE);
    assertThat(listener.states.toArray()).isEqualTo(new Integer[]{POSSIBLE});
  }

  private MotionEvent createMotionEvent(int action, float x, float y) {
    return MotionEvent.obtain(eventDownTime, eventTime++, action, x, y, 0);
  }
}
