package flow.sample.multikey

class ScreenTwo {

  override fun toString() =
    "Click to pop back to screen one, skipping the dialog. " + "Or hit the back button to see the dialog again."

  override fun equals(other: Any?) =
    javaClass.isInstance(other)

  override fun hashCode() =
    javaClass.hashCode()

}