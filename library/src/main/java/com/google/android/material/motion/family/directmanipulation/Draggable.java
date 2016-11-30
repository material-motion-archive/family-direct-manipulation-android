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

import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.material.motion.gestures.DragGestureRecognizer;
import com.google.android.material.motion.runtime.Performer;

/**
 * Makes the target draggable. The target {@link View} is offset by the translation of a {@link
 * DragGestureRecognizer}.
 * <p>
 * Note that this will overwrite the {@link Performer#target target}'s {@link OnTouchListener}.
 */
public class Draggable extends GesturePlan {

  public Draggable() {
    this(new DragGestureRecognizer());
  }

  public Draggable(DragGestureRecognizer gestureRecognizer) {
    super(gestureRecognizer);
  }
}
