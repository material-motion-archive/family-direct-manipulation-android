# Material Motion Direct Manipulation Family

[![Build Status](https://travis-ci.org/material-motion/family-direct-manipulation-android.svg?branch=develop)](https://travis-ci.org/material-motion/family-direct-manipulation-android)
[![codecov](https://codecov.io/gh/material-motion/family-direct-manipulation-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/material-motion/family-direct-manipulation-android)

## Features

This library consists of the following plans:

- `Draggable`, `Pinchable`, and `Rotatable`
- `DirectlyManipulable`

The `Draggable`, `Pinchable`, and `Rotatable` plans allow a user to drag, scale, and rotate a view.
They each listen for deltas emitted by a gesture recognizer and add them to the target.

If a view can be dragged then it can sometimes be pinched and rotated too. To make this easy, we
provide a `DirectlyManipulable` plan. It's equivalent to individually adding `Draggable`,
`Pinchable`, and `Rotatable` to the same target.

The collection of `Draggable`, `Pinchable`, `Rotatable`, and `DirectlyManipulable` represent traits
that can describe behavior of a target view. If the plan's associated gesture recognizer is not yet
associated with a view then the gesture recognizer will be added to the target view.

Learn more about the APIs defined in the library by reading our
[technical documentation](https://jitpack.io/com/github/material-motion/family-direct-manipulation-android/1.0.0/javadoc/) and our
[Starmap](https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/).

## Installation

### Installation with Jitpack

Add the Jitpack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Depend on the [latest version](https://github.com/material-motion/family-direct-manipulation-android/releases) of the library.
Take care to occasionally [check for updates](https://github.com/ben-manes/gradle-versions-plugin).

```gradle
dependencies {
    compile 'com.github.material-motion:family-direct-manipulation-android:1.0.0'
}
```

For more information regarding versioning, see:

- [Material Motion Versioning Policies](https://material-motion.gitbooks.io/material-motion-team/content/essentials/core_team_contributors/release_process.html#versioning)

### Using the files from a folder local to the machine

You can have a copy of this library with local changes and test it in tandem
with its client project. To add a local dependency on this library, add this
library's identifier to your project's `local.dependencies`:

```
com.github.material-motion:family-direct-manipulation-android
```

> Because `local.dependencies` is never to be checked into Version Control
Systems, you must also ensure that any local dependencies are also defined in
`build.gradle` as explained in the previous section.

**Important**

For each local dependency listed, you *must* run `gradle install` from its
project root every time you make a change to it. That command will publish your
latest changes to the local maven repository. If your local dependencies have
local dependencies of their own, you must `gradle install` them as well. See
[Issue #16](https://github.com/material-motion/runtime-android/issues/16).

You must `gradle clean` your project every time you add or remove a local
dependency.

### Usage

How to use the library in your project.

#### Editing the library in Android Studio

Open Android Studio,
choose `File > New > Import`,
choose the root `build.gradle` file.

## Example apps/unit tests

To build the sample application, run the following commands:

    git clone https://github.com/material-motion/family-direct-manipulation-android.git
    cd family-direct-manipulation-android
    gradle installDebug

To run all unit tests, run the following commands:

    git clone https://github.com/material-motion/family-direct-manipulation-android.git
    cd family-direct-manipulation-android
    gradle test

# Guides

1. [How to make a view directly manipulable](#how-to-make-a-view-directly-manipulable)
2. [How to make a view draggable](#how-to-make-a-view-draggable)
3. [How to use an existing gesture recognizer to make a view draggable](#how-to-use-an-existing-gesture-recognizer-to-make-a-view-draggable)

## How to make a view directly manipulable

```java
runtime.addPlan(new DirectlyManipulable(), view);
```

## How to make a view draggable

```java
runtime.addPlan(new Draggable(), view);
```

## How to use an existing gesture recognizer to make a view draggable

```java
runtime.addPlan(new Draggable(dragGestureRecognizer), view);
```

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/family-direct-manipulation-android/milestones).

Learn more about [our team](https://material-motion.gitbooks.io/material-motion-team/content/),
[our community](https://material-motion.gitbooks.io/material-motion-team/content/community/),
and our [contributor essentials](https://material-motion.gitbooks.io/material-motion-team/content/essentials/).

## License

Licensed under the Apache 2.0 license. See LICENSE for details.
