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

import android.view.View;

import com.google.android.material.motion.family.directmanipulation.DragGestureRecognizer;
import com.google.android.material.motion.family.directmanipulation.Draggable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.material.motion.runtime.Runtime;

/**
 * Material Motion Direct Manipulation Family sample Activity.
 */
public class MainActivity extends AppCompatActivity {

  private final Runtime runtime = new Runtime();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    View target = findViewById(R.id.target);

    runtime.addPlan(new Draggable(new DragGestureRecognizer(target)), target);
  }
}
