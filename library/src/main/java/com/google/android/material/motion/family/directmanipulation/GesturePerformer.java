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

import android.support.v4.util.SimpleArrayMap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.material.motion.family.directmanipulation.GestureRecognizer.GestureStateChangeListener;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;

/**
 * A performer that uses a {@link DragGestureRecognizer} to translate a {@link View} target.
 */
public class GesturePerformer extends Performer implements ContinuousPerforming {

  private final SimpleArrayMap<Class<? extends GestureRecognizer>, GestureRecognizer> gestureRecognizers =
    new SimpleArrayMap<>();

  private IsActiveTokenGenerator isActiveTokenGenerator;

  @Override
  public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
    this.isActiveTokenGenerator = isActiveTokenGenerator;
  }

  @Override
  protected void onInitialize(Object target) {
    super.onInitialize(target);

    ((View) target).setOnTouchListener(onTouchListener);
  }

  @Override
  public void addPlan(BasePlan plan) {
    if (plan instanceof Draggable) {
      addDraggable((Draggable) plan);
    } else {
      throw new IllegalArgumentException("Plan type not supported for " + plan);
    }
  }

  private void addDraggable(Draggable plan) {
    addGesturePlanCommon(plan);
    plan.gestureRecognizer.addStateChangeListener(dragGestureListener);
  }

  private void addGesturePlanCommon(GesturePlan plan) {
    plan.gestureRecognizer.addStateChangeListener(createTokenGestureListener());
    gestureRecognizers.put(plan.gestureRecognizer.getClass(), plan.gestureRecognizer);
  }

  /**
   * Single touch listener that delegates to all the gesture recognizers.
   */
  private final OnTouchListener onTouchListener = new OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      boolean handled = false;

      for (int i = 0, count = gestureRecognizers.size(); i < count; i++) {
        GestureRecognizer gestureRecognizer = gestureRecognizers.valueAt(i);
        handled |= gestureRecognizer.onTouchEvent(event);
      }

      return handled;
    }
  };

  private GestureStateChangeListener createTokenGestureListener() {
    return new GestureStateChangeListener() {

      private IsActiveToken token;

      @Override
      public void onStateChanged(GestureRecognizer gestureRecognizer) {
        switch (gestureRecognizer.getState()) {
          case GestureRecognizer.BEGAN:
            token = isActiveTokenGenerator.generate();
            break;
          case GestureRecognizer.RECOGNIZED:
          case GestureRecognizer.CANCELLED:
            token.terminate();
            break;
        }
      }
    };
  }

  private final GestureStateChangeListener dragGestureListener = new GestureStateChangeListener() {

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      switch (gestureRecognizer.getState()) {
        case GestureRecognizer.CHANGED:
          View target = getTarget();
          target.offsetLeftAndRight((int) gestureRecognizer.getTranslationX());
          target.offsetTopAndBottom((int) gestureRecognizer.getTranslationY());
          break;
      }
    }
  };
}
