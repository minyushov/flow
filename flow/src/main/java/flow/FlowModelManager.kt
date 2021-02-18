package flow

@Mockable
class FlowModelManager(
  private val scopes: List<FlowModelScope>
) {

  fun getModel(scopeClass: Class<*>, tag: String): Any? =
    findScope(scopeClass)?.getModel(tag)

  fun setUp(key: FlowModelUser) {
    for (scopeClass in key.relations.scopes) {
      findScope(scopeClass)?.setUp(key)
    }
  }

  fun tearDown(key: FlowModelUser) {
    for (scopeClass in key.relations.scopes) {
      findScope(scopeClass)?.tearDown(key)
    }
  }

  private fun findScope(scopeClass: Class<*>): FlowModelScope? =
    scopes.firstOrNull { it.javaClass.isAssignableFrom(scopeClass) }

}
