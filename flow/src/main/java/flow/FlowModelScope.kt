package flow

import android.os.Bundle
import android.os.Parcelable
import java.util.ArrayList
import java.util.LinkedHashMap

abstract class FlowModelScope {

  // tag to model relations
  private val models = LinkedHashMap<String, Any>()
  // tag to users relations
  private val users = LinkedHashMap<String, ArrayList<FlowModelUser>>()

  internal fun setUp(user: FlowModelUser) {
    for (tag in user.relations.tags) {
      val model = models[tag]

      if (model == null) {
        models[tag] = createModel()
        users[tag] = ArrayList()
      }

      users[tag]?.add(user)
    }
  }

  internal fun tearDown(user: FlowModelUser) {
    for (tag in user.relations.tags) {
      val modelUsers = users[tag] ?: continue

      modelUsers.remove(user)

      if (modelUsers.isEmpty()) {
        models.remove(tag)
        users.remove(tag)
      }
    }
  }

  internal fun getModel(tag: String): Any =
    models[tag] ?: throw IllegalStateException("No model currently exists for tag: $tag")

  fun saveModels(bundle: Bundle) {
    val modelsToSave = LinkedHashMap<String, Parcelable>()

    for ((key, value) in models) {
      if (value is Parcelable) {
        modelsToSave[key] = value
      }
    }

    val tags = ArrayList(modelsToSave.keys)
    bundle.putStringArrayList(TAGS, tags)

    for ((key, value) in modelsToSave) {
      bundle.putParcelable(key, value)
    }
  }

  fun restoreModels(bundle: Bundle) {
    val tags = bundle.getStringArrayList(TAGS) ?: return

    for (tag in tags) {
      models[tag] = bundle.getParcelable(tag) ?: throw IllegalStateException("Unable to restore model: missing value for tag '$tag'")
      users[tag] = ArrayList()
    }
  }

  abstract fun createModel(): Any

  companion object {
    private const val TAGS = "flow-model-scope:tags"
  }

}