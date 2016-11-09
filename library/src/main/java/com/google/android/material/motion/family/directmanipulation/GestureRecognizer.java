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

import android.support.annotation.IntDef;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A gesture recognizer generates continuous or discrete events from a stream of device input
 * events. When attached to an element, any interactions with that element will be interpreted by
 * the gesture recognizer and turned into gesture events. The output is often a linear
 * transformation of translation, rotation, and/or scale.
 */
public abstract class GestureRecognizer {

  /**
   * A listener that receives {@link GestureRecognizer} events.
   */
  public interface GestureStateChangeListener {

    /**
     * Notifies every time on {@link GestureRecognizerState state} change.
     * <p>
     * <p>
     * Implementations should query the provided gesture recognizer for its current state and
     * properties.
     *
     * @param gestureRecognizer the gesture recognizer where the event originated from.
     */
    void onStateChanged(GestureRecognizer gestureRecognizer);
  }

  /**
   * The gesture recognizer has not yet recognized its gesture, but may be evaluating touch events.
   * This is the default state.
   */
  public static final int POSSIBLE = 0;
  /**
   * The gesture recognizer has received touch objects recognized as a continuous gesture.
   */
  public static final int BEGAN = 1;
  /**
   * The gesture recognizer has received touches recognized as a change to a continuous gesture.
   */
  public static final int CHANGED = 2;
  /**
   * The gesture recognizer has received touches recognized as the end of a continuous gesture.
   * At the next cycle of the run loop, the gesture recognizer resets its state to
   * {@link #POSSIBLE}.
   */
  public static final int RECOGNIZED = 3;
  /**
   * The gesture recognizer has received touches resulting in the cancellation of a continuous
   * gesture. At the next cycle of the run loop, the gesture recognizer resets its state to
   * {@link #POSSIBLE}.
   */
  public static final int CANCELLED = 4;

  /**
   * The state of the gesture recognizer.
   */
  @IntDef({POSSIBLE, BEGAN, CHANGED, RECOGNIZED, CANCELLED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface GestureRecognizerState {

  }

  private final View element;
  private final List<GestureStateChangeListener> listeners = new CopyOnWriteArrayList<>();

  @GestureRecognizerState
  private int state = POSSIBLE;

  GestureRecognizer(View element) {
    this.element = element;
  }

  /**
   * Returns the current state of the gesture recognizer.
   */
  @GestureRecognizerState
  public int getState() {
    return state;
  }

  /**
   * Forwards touch events from a {@link OnTouchListener} to this gesture recognizer.
   */
  public abstract boolean onTouchEvent(MotionEvent event);

  /**
   * Returns the view associated with this gesture recognizer.
   */
  public View getElement() {
    return element;
  }

  /**
   * Adds a listener to this gesture recognizer.
   */
  public void addStateChangeListener(GestureStateChangeListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Removes a listener from this gesture recognizer.
   */
  public void removeStateChangeListener(GestureStateChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Sets the state of the gesture recognizer and notifies all listeners.
   */
  protected void setState(@GestureRecognizerState int state) {
    this.state = state;

    for (GestureStateChangeListener listener : listeners) {
      listener.onStateChanged(this);
    }

    element.removeCallbacks(setStateToPossible);
    if (state == RECOGNIZED || state == CANCELLED) {
      element.post(setStateToPossible);
    }
  }

  private final Runnable setStateToPossible = new Runnable() {
    @Override
    public void run() {
      setState(POSSIBLE);
    }
  };

  protected boolean isInProgress() {
    return state == BEGAN || state == CHANGED;
  }

  /**
   * Returns the raw centroid x position of the current gesture.
   */
  public abstract float getCentroidX();

  /**
   * Returns the raw centroid y position of the current gesture.
   */
  public abstract float getCentroidY();

  /**
   * Returns the change in centroid x of the current gesture.
   */
  public abstract float getTranslationX();

  /**
   * Returns the change in centroid y of the current gesture.
   */
  public abstract float getTranslationY();

  /**
   * Returns the x velocity of the current gesture.
   */
  public abstract float getVelocityX();

  /**
   * Returns the y velocity of the current gesture.
   */
  public abstract float getVelocityY();
}
