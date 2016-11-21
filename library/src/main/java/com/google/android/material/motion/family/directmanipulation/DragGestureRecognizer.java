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

import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * A gesture recognizer that generates translation events.
 */
public class DragGestureRecognizer extends GestureRecognizer {
  private VelocityTracker velocityTracker;

  private float initialCentroidX;
  private float initialCentroidY;
  private float currentCentroidX;
  private float currentCentroidY;
  private float currentVelocityX;
  private float currentVelocityY;

  public boolean onTouchEvent(MotionEvent event) {
    MotionEvent copy = MotionEvent.obtain(event);
    copy.setLocation(event.getRawX(), event.getRawY());
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(copy);

    PointF centroid = calculateCentroid(event);
    float centroidX = centroid.x;
    float centroidY = centroid.y;

    int action = MotionEventCompat.getActionMasked(event);
    if (action == MotionEvent.ACTION_DOWN) {
      initialCentroidX = centroidX;
      initialCentroidY = centroidY;
      currentCentroidX = centroidX;
      currentCentroidY = centroidY;
      currentVelocityX = 0f;
      currentVelocityY = 0f;
    }
    if (action == MotionEvent.ACTION_POINTER_DOWN
      || action == MotionEvent.ACTION_POINTER_UP) {
      float adjustX = centroidX - currentCentroidX;
      float adjustY = centroidY - currentCentroidY;

      initialCentroidX += adjustX;
      initialCentroidY += adjustY;
      currentCentroidX += adjustX;
      currentCentroidY += adjustY;
    }
    if (action == MotionEvent.ACTION_MOVE) {
      if (!isInProgress()) {
        float deltaX = centroidX - initialCentroidX;
        float deltaY = centroidY - initialCentroidY;
        if (Math.abs(deltaX) > dragSlop || Math.abs(deltaY) > dragSlop) {
          float adjustX = Math.signum(deltaX) * Math.min(Math.abs(deltaX), dragSlop);
          float adjustY = Math.signum(deltaY) * Math.min(Math.abs(deltaY), dragSlop);

          initialCentroidX += adjustX;
          initialCentroidY += adjustY;
          currentCentroidX += adjustX;
          currentCentroidY += adjustY;

          setState(BEGAN);
        }
      }

      if (isInProgress()) {
        currentCentroidX = centroidX;
        currentCentroidY = centroidY;

        setState(CHANGED);
      }
    }
    if (action == MotionEvent.ACTION_UP
      || action == MotionEvent.ACTION_CANCEL) {
      initialCentroidX = centroidX;
      initialCentroidY = centroidY;
      currentCentroidX = centroidX;
      currentCentroidY = centroidY;

      if (isInProgress()) {
        velocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND, maximumFlingVelocity);
        currentVelocityX = velocityTracker.getXVelocity();
        currentVelocityY = velocityTracker.getYVelocity();

        if (action == MotionEvent.ACTION_UP) {
          setState(RECOGNIZED);
        } else {
          setState(CANCELLED);
        }
      }

      velocityTracker.recycle();
      velocityTracker = null;
    }

    return true;
  }

  /**
   * Returns the translationX of the drag gesture.
   * <p>
   * This reports the total translation over time since the {@link #BEGAN beginning} of the gesture.
   * This is not a delta value from the last {@link #CHANGED update}.
   */
  public float getTranslationX() {
    return currentCentroidX - initialCentroidX;
  }

  /**
   * Returns the translationY of the drag gesture.
   * <p>
   * This reports the total translation over time since the {@link #BEGAN beginning} of the gesture.
   * This is not a delta value from the last {@link #CHANGED update}.
   */
  public float getTranslationY() {
    return currentCentroidY - initialCentroidY;
  }

  /**
   * Returns the velocityX of the drag gesture.
   * <p>
   * Only read this when the state is {@link #RECOGNIZED} or {@link #CANCELLED}.
   *
   * @return The velocityX in pixels per second.
   */
  public float getVelocityX() {
    return currentVelocityX;
  }

  /**
   * Returns the velocityY of the drag gesture.
   * <p>
   * Only read this when the state is {@link #RECOGNIZED} or {@link #CANCELLED}.
   *
   * @return The velocityY in pixels per second.
   */
  public float getVelocityY() {
    return currentVelocityY;
  }

  @Override
  public float getCentroidX() {
    return currentCentroidX;
  }

  @Override
  public float getCentroidY() {
    return currentCentroidY;
  }
}
