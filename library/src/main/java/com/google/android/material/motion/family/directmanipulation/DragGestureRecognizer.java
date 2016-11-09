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

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.google.android.material.motion.family.directmanipulation.GestureRecognizer.GestureRecognizerState;

/**
 * A gesture recognizer that generates translation events.
 */
public class DragGestureRecognizer extends GestureRecognizer {

  private static final int PIXELS_PER_SECOND = 1000;

  private final int touchSlopSquare;
  private final float maximumFlingVelocity;

  private float initialX;
  private float initialY;
  private VelocityTracker velocityTracker;

  private float previousX;
  private float previousY;
  private float currentX;
  private float currentY;
  private float currentVelocityX;
  private float currentVelocityY;

  public DragGestureRecognizer(View element) {
    super(element);
    Context context = element.getContext();
    int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    this.touchSlopSquare = touchSlop * touchSlop;
    this.maximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
  }

  public boolean onTouchEvent(MotionEvent event) {
    MotionEvent copy = MotionEvent.obtain(event);
    copy.setLocation(event.getRawX(), event.getRawY());
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(copy);

    int action = MotionEventCompat.getActionMasked(event);
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        initialX = event.getRawX();
        initialY = event.getRawY();
        previousX = initialX;
        previousY = initialY;
        currentX = initialX;
        currentY = initialY;
        currentVelocityX = 0f;
        currentVelocityY = 0f;
        break;
      case MotionEvent.ACTION_MOVE:
        if (!isInProgress()) {
          float deltaX = event.getRawX() - initialX;
          float deltaY = event.getRawY() - initialY;
          float distance = deltaX * deltaX + deltaY * deltaY;
          if (distance > touchSlopSquare) {
            setState(BEGAN);
          }
        }

        if (isInProgress()) {
          previousX = currentX;
          previousY = currentY;
          currentX = event.getRawX();
          currentY = event.getRawY();

          setState(CHANGED);
        }
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
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
        break;
    }

    return true;
  }

  @Override
  public float getCentroidX() {
    return currentX;
  }

  @Override
  public float getCentroidY() {
    return currentY;
  }

  @Override
  public float getTranslationX() {
    return currentX - previousX;
  }

  @Override
  public float getTranslationY() {
    return currentY - previousY;
  }

  @Override
  public float getVelocityX() {
    return currentVelocityX;
  }

  @Override
  public float getVelocityY() {
    return currentVelocityY;
  }
}
