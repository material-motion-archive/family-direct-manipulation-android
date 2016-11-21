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
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

/**
 * A gesture recognizer that generates scale events.
 */
public class RotateGestureRecognizer extends GestureRecognizer {
  private float currentCentroidX;
  private float currentCentroidY;

  private float initialAngle;
  private float currentAngle;

  public boolean onTouchEvent(MotionEvent event) {
    PointF centroid = calculateUntransformedCentroid(event, 2);
    float centroidX = centroid.x;
    float centroidY = centroid.y;
    float angle = calculateAngle(event);

    int action = MotionEventCompat.getActionMasked(event);
    int pointerCount = event.getPointerCount();
    if (action == MotionEvent.ACTION_DOWN
      || action == MotionEvent.ACTION_POINTER_DOWN && pointerCount == 2) {
      currentCentroidX = centroidX;
      currentCentroidY = centroidY;

      initialAngle = angle;
      currentAngle = angle;
    }
    if (action == MotionEvent.ACTION_POINTER_DOWN && pointerCount > 2
      || action == MotionEvent.ACTION_POINTER_UP && pointerCount > 2) {
      float adjustX = centroidX - currentCentroidX;
      float adjustY = centroidY - currentCentroidY;

      currentCentroidX += adjustX;
      currentCentroidY += adjustY;

      float adjustAngle = angle - currentAngle;

      initialAngle += adjustAngle;
      currentAngle += adjustAngle;
    }
    if (action == MotionEvent.ACTION_MOVE) {
      currentCentroidX = centroidX;
      currentCentroidY = centroidY;

      if (!isInProgress()) {
        float deltaAngle = angle - initialAngle;
        if (Math.abs(deltaAngle) > rotateSlop) {
          float adjustAngle = Math.signum(deltaAngle) * rotateSlop;

          initialAngle += adjustAngle;
          currentAngle += adjustAngle;

          setState(BEGAN);
        }
      }

      if (isInProgress()) {
        currentAngle = angle;

        setState(CHANGED);
      }
    }
    if (action == MotionEvent.ACTION_POINTER_UP && pointerCount == 2
      || action == MotionEvent.ACTION_CANCEL) {
      currentCentroidX = centroidX;
      currentCentroidY = centroidY;

      initialAngle = 0;
      currentAngle = 0;

      if (isInProgress()) {
        if (action == MotionEvent.ACTION_POINTER_UP) {
          setState(RECOGNIZED);
        } else {
          setState(CANCELLED);
        }
      }
    }

    return true;
  }

  /**
   * Returns the rotation of the rotate gesture in radians.
   * <p>
   * This reports the total rotation over time since the {@link #BEGAN beginning} of the gesture.
   * This is not a delta value from the last {@link #CHANGED update}.
   */
  public float getRotation() {
    return currentAngle - initialAngle;
  }

  @Override
  public float getUntransformedCentroidX() {
    return currentCentroidX;
  }

  @Override
  public float getUntransformedCentroidY() {
    return currentCentroidY;
  }

  /**
   * Calculates the angle between the first two pointers in the given motion event.
   * <p>
   * Angle is calculated from finger 0 to finger 1.
   */
  private float calculateAngle(MotionEvent event) {
    int action = MotionEventCompat.getActionMasked(event);
    int pointerIndex = MotionEventCompat.getActionIndex(event);
    int pointerCount = event.getPointerCount();
    if (pointerCount < 2) {
      return 0;
    }
    if (action == MotionEvent.ACTION_POINTER_UP && pointerCount == 2) {
      return 0;
    }

    int i0 = 0;
    int i1 = 1;
    if (action == MotionEvent.ACTION_POINTER_UP) {
      if (pointerIndex == 0) {
        i0++;
        i1++;
      } else if (pointerIndex == 1) {
        i1++;
      }
    }

    PointF point = calculateUntransformedPoint(event, i0);
    float x0 = point.x;
    float y0 = point.y;

    point = calculateUntransformedPoint(event, i1);
    float x1 = point.x;
    float y1 = point.y;

    return angle(x0, y0, x1, y1);
  }

  @VisibleForTesting
  static float angle(float x0, float y0, float x1, float y1) {
    return (float) Math.atan2(y1 - y0, x1 - x0);
  }
}
