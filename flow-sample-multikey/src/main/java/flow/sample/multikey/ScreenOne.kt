package flow.sample.multikey

class ScreenOne {

  override fun toString() =
    "Click to advance to screen two."

  override fun equals(other: Any?) =
    javaClass.isInstance(other)

  override fun hashCode() =
    javaClass.hashCode()

}