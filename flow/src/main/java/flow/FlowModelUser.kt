package flow

interface FlowModelUser {
  val relations: Relations

  class Relations(
    private val relations: Map<Class<*>, String>
  ) {
    internal val scopes: Set<Class<*>>
      get() = relations.keys

    internal val tags: Collection<String>
      get() = relations.values
  }
}