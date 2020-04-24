package flow

interface FlowModelUser {
  val relations: Relations

  class Relations(
    private val relations: Map<Class<*>, String>
  ) {
    internal val scopes: Set<Class<*>>
      get() = relations.keys

    internal fun tag(scope: FlowModelScope): String? =
      relations.get(scope::class.java)
  }
}