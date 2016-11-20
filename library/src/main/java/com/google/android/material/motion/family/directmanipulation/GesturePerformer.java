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
    } else if (plan instanceof Pinchable) {
      addPinchable((Pinchable) plan);
    } else if (plan instanceof Rotatable) {
      addRotatable((Rotatable) plan);
    } else {
      throw new IllegalArgumentException("Plan type not supported for " + plan);
    }
  }

  private void addDraggable(Draggable plan) {
    addGesturePlanCommon(plan);
    plan.gestureRecognizer.addStateChangeListener(dragGestureListener);
  }

  private void addPinchable(Pinchable plan) {
    addGesturePlanCommon(plan);
    plan.gestureRecognizer.addStateChangeListener(scaleGestureListener);
  }

  private void addRotatable(Rotatable plan) {
    addGesturePlanCommon(plan);
    plan.gestureRecognizer.addStateChangeListener(rotateGestureListener);
  }

  private void addGesturePlanCommon(GesturePlan plan) {
    if (plan.gestureRecognizer.getElement() == null) {
      plan.gestureRecognizer.setElement((View) getTarget());
    }
    plan.gestureRecognizer.addStateChangeListener(tokenGestureListener);
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

  private GestureStateChangeListener tokenGestureListener = new GestureStateChangeListener() {

    private SimpleArrayMap<GestureRecognizer, IsActiveToken> tokens = new SimpleArrayMap<>();

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      switch (gestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          tokens.put(gestureRecognizer, isActiveTokenGenerator.generate());
          break;
        case GestureRecognizer.RECOGNIZED:
        case GestureRecognizer.CANCELLED:
          tokens.remove(gestureRecognizer).terminate();
          break;
      }
    }
  };

  private final GestureStateChangeListener dragGestureListener = new GestureStateChangeListener() {
    private float initialTranslationX;
    private float initialTranslationY;

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      DragGestureRecognizer dragGestureRecognizer = (DragGestureRecognizer) gestureRecognizer;
      switch (dragGestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialTranslationX = target.getTranslationX();
          initialTranslationY = target.getTranslationY();
          break;
        case GestureRecognizer.CHANGED:
          float translationX = dragGestureRecognizer.getTranslationX();
          float translationY = dragGestureRecognizer.getTranslationY();

          target.setTranslationX(initialTranslationX + translationX);
          target.setTranslationY(initialTranslationY + translationY);
          break;
      }
    }
  };

  private final GestureStateChangeListener scaleGestureListener = new GestureStateChangeListener() {
    private float initialScaleX;
    private float initialScaleY;

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      ScaleGestureRecognizer scaleGestureRecognizer = (ScaleGestureRecognizer) gestureRecognizer;
      switch (scaleGestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialScaleX = target.getScaleX();
          initialScaleY = target.getScaleY();
          break;
        case GestureRecognizer.CHANGED:
          float scale = scaleGestureRecognizer.getScale();

          target.setScaleX(initialScaleX * scale);
          target.setScaleY(initialScaleY * scale);
          break;
      }
    }
  };

  private final GestureStateChangeListener rotateGestureListener = new GestureStateChangeListener() {
    private float initialRotation;

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      RotateGestureRecognizer rotateGestureRecognizer = (RotateGestureRecognizer) gestureRecognizer;
      switch (rotateGestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialRotation = target.getRotation();
          break;
        case GestureRecognizer.CHANGED:
          float rotation = rotateGestureRecognizer.getRotation();

          target.setRotation((float) (initialRotation + rotation * (180 / Math.PI)));
          break;
      }
    }
  };
}
