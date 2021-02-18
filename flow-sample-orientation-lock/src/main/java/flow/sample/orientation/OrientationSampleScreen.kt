package flow.sample.orientation

import androidx.annotation.LayoutRes

abstract class OrientationSampleScreen {
  @get:LayoutRes
  abstract val layoutId: Int
  open val requiresLandscape: Boolean = false
}