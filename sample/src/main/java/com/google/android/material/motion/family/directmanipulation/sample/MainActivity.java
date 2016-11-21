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
package com.google.android.material.motion.family.directmanipulation.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.libraries.remixer.annotation.BooleanVariableMethod;
import com.google.android.libraries.remixer.annotation.RemixerBinder;
import com.google.android.libraries.remixer.ui.gesture.Direction;
import com.google.android.libraries.remixer.ui.view.RemixerFragment;
import com.google.android.material.motion.family.directmanipulation.Draggable;
import com.google.android.material.motion.family.directmanipulation.Pinchable;
import com.google.android.material.motion.family.directmanipulation.Rotatable;
import com.google.android.material.motion.runtime.Runtime;

/**
 * Material Motion Direct Manipulation Family sample Activity.
 */
public class MainActivity extends AppCompatActivity {

  private final Runtime runtime = new Runtime();
  private View target;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    target = findViewById(R.id.target);

    RemixerBinder.bind(this);
    RemixerFragment remixerFragment = RemixerFragment.newInstance();
    remixerFragment.attachToGesture(this, Direction.UP, 3);
    remixerFragment.attachToButton(this, (Button) findViewById(R.id.remixer_button));
  }

  @BooleanVariableMethod(defaultValue = true, title = "Draggable")
  public void setDraggable(Boolean draggable) {
    if (draggable) {
      runtime.addNamedPlan(new Draggable(), "draggable", target);
    } else {
      runtime.removeNamedPlan("draggable", target);
    }
  }

  @BooleanVariableMethod(defaultValue = true, title = "Pinchable")
  public void setPinchable(Boolean pinchable) {
    if (pinchable) {
      runtime.addNamedPlan(new Pinchable(), "pinchable", target);
    } else {
      runtime.removeNamedPlan("pinchable", target);
    }
  }

  @BooleanVariableMethod(defaultValue = true, title = "Rotatable")
  public void setRotatable(Boolean rotatable) {
    if (rotatable) {
      runtime.addNamedPlan(new Rotatable(), "rotatable", target);
    } else {
      runtime.removeNamedPlan("rotatable", target);
    }
  }
}
