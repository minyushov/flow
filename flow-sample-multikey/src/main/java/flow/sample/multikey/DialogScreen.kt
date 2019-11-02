package flow.sample.multikey

import flow.MultiKey

class DialogScreen(
  val mainContent: Any
) : MultiKey {

  override val keys =
    listOf(mainContent)

  override fun toString() =
    "Do you really want to see screen two?"

}