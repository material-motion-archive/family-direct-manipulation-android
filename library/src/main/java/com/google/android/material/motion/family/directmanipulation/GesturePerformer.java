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

import android.graphics.Matrix;
import android.support.v4.util.SimpleArrayMap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.material.motion.gestures.DragGestureRecognizer;
import com.google.android.material.motion.gestures.GestureRecognizer;
import com.google.android.material.motion.gestures.GestureRecognizer.GestureStateChangeListener;
import com.google.android.material.motion.gestures.RotateGestureRecognizer;
import com.google.android.material.motion.gestures.ScaleGestureRecognizer;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;

/**
 * A performer that uses {@link GestureRecognizer}s to drag, scale, and rotate a view.
 */
public class GesturePerformer extends Performer
  implements ContinuousPerforming, NamedPlanPerforming {

  /* Temporary variables. */
  private final float[] array = new float[2];
  private final Matrix matrix = new Matrix();
  private final Matrix inverse = new Matrix();

  private final SimpleArrayMap<Class<? extends GestureRecognizer>, GestureRecognizer> gestureRecognizers =
    new SimpleArrayMap<>();

  private float initialTranslationX;
  private float initialTranslationY;
  private float initialScaleX;
  private float initialScaleY;
  private float initialRotation;

  private IsActiveTokenGenerator isActiveTokenGenerator;

  @Override
  public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
    this.isActiveTokenGenerator = isActiveTokenGenerator;
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

  @Override
  public void addPlan(NamedPlan plan, String name) {
    addPlan(plan);
  }

  @Override
  public void removePlan(String name) {
    Class<? extends GestureRecognizer> klass;
    switch (name) {
      case "draggable":
        klass = DragGestureRecognizer.class;
        break;
      case "pinchable":
        klass = ScaleGestureRecognizer.class;
        break;
      case "rotatable":
        klass = RotateGestureRecognizer.class;
        break;
      default:
        throw new IllegalArgumentException(
          "Only \"draggable\", \"pinchable\", or \"rotatable\" names may be used.");
    }

    GestureRecognizer gestureRecognizer = gestureRecognizers.remove(klass);
    gestureRecognizer.setElement(null);
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
      View element = getTarget();
      element.setOnTouchListener(onTouchListener);
      plan.gestureRecognizer.setElement(element);
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

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      switch (gestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialTranslationX = target.getTranslationX();
          initialTranslationY = target.getTranslationY();
          break;
        case GestureRecognizer.CHANGED:
          float translationX = ((DragGestureRecognizer) gestureRecognizer).getTranslationX();
          float translationY = ((DragGestureRecognizer) gestureRecognizer).getTranslationY();

          target.setTranslationX(initialTranslationX + translationX);
          target.setTranslationY(initialTranslationY + translationY);
          break;
      }
    }
  };

  private final GestureStateChangeListener scaleGestureListener = new GestureStateChangeListener() {

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      switch (gestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialScaleX = target.getScaleX();
          initialScaleY = target.getScaleY();
          break;
        case GestureRecognizer.CHANGED:
          float scale = ((ScaleGestureRecognizer) gestureRecognizer).getScale();

          target.setScaleX(initialScaleX * scale);
          target.setScaleY(initialScaleY * scale);

          if (gestureRecognizers.containsKey(DragGestureRecognizer.class)) {
            setPivotToCentroid(target, gestureRecognizer);
          }
          break;
        case GestureRecognizer.RECOGNIZED:
        case GestureRecognizer.CANCELLED:
          if (gestureRecognizers.containsKey(DragGestureRecognizer.class)) {
            resetPivot(target);
          }
          break;
      }
    }
  };

  private final GestureStateChangeListener rotateGestureListener = new GestureStateChangeListener() {

    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      View target = getTarget();
      switch (gestureRecognizer.getState()) {
        case GestureRecognizer.BEGAN:
          initialRotation = target.getRotation();
          break;
        case GestureRecognizer.CHANGED:
          float rotation = ((RotateGestureRecognizer) gestureRecognizer).getRotation();

          target.setRotation((float) (initialRotation + rotation * (180 / Math.PI)));

          if (gestureRecognizers.containsKey(DragGestureRecognizer.class)) {
            setPivotToCentroid(target, gestureRecognizer);
          }
          break;
        case GestureRecognizer.RECOGNIZED:
        case GestureRecognizer.CANCELLED:
          if (gestureRecognizers.containsKey(DragGestureRecognizer.class)) {
            resetPivot(target);
          }
          break;
      }
    }
  };

  private void setPivotToCentroid(View target, GestureRecognizer gestureRecognizer) {
    setPivotToCentroid(
      target,
      gestureRecognizer.getCentroidX(),
      gestureRecognizer.getCentroidY(),
      gestureRecognizer.getUntransformedCentroidX(),
      gestureRecognizer.getUntransformedCentroidY());
  }

  private void resetPivot(View target) {
    float centroidX = target.getWidth() / 2f;
    float centroidY = target.getHeight() / 2f;

    array[0] = centroidX;
    array[1] = centroidY;
    GestureRecognizer.getTransformationMatrix(target, matrix, inverse);
    matrix.mapPoints(array);

    float untransformedCentroidX = array[0];
    float untransformedCentroidY = array[1];

    setPivotToCentroid(
      target, centroidX, centroidY, untransformedCentroidX, untransformedCentroidY);
  }

  private void setPivotToCentroid(
    View target,
    float centroidX,
    float centroidY,
    float untransformedCentroidX,
    float untransformedCentroidY) {
    target.setPivotX(centroidX);
    target.setPivotY(centroidY);

    // Ensure that the pivot is over the untransformed centroid.

    array[0] = target.getPivotX();
    array[1] = target.getPivotY();
    GestureRecognizer.getTransformationMatrix(target, matrix, inverse);
    matrix.mapPoints(array);

    float adjustX = untransformedCentroidX - array[0];
    float adjustY = untransformedCentroidY - array[1];

    initialTranslationX += adjustX;
    initialTranslationY += adjustY;
    target.setTranslationX(target.getTranslationX() + adjustX);
    target.setTranslationY(target.getTranslationY() + adjustY);
  }
}
