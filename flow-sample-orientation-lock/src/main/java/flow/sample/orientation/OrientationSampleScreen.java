package flow.sample.orientation;

import androidx.annotation.LayoutRes;

abstract class OrientationSampleScreen {
  @LayoutRes abstract int getLayoutId();

  boolean requiresLandscape() {
    return false;
  }
}
