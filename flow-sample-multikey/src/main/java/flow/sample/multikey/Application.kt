package flow.sample.multikey

import android.app.Activity
import android.app.Application
import android.os.Bundle

inline fun Application.onActivityDestroyed(
  crossinline onActivityDestroyed: (activity: Activity) -> Unit
): Application.ActivityLifecycleCallbacks =
  registerActivityLifecycleCallbacks(onActivityDestroyed = onActivityDestroyed)

inline fun Application.registerActivityLifecycleCallbacks(
  crossinline onActivityCreated: (activity: Activity, savedInstanceState: Bundle?) -> Unit = { _, _ -> },
  crossinline onActivityStarted: (activity: Activity) -> Unit = { _ -> },
  crossinline onActivityResumed: (activity: Activity) -> Unit = { _ -> },
  crossinline onActivityPaused: (activity: Activity) -> Unit = { _ -> },
  crossinline onActivityStopped: (activity: Activity) -> Unit = { _ -> },
  crossinline onActivitySaveInstanceState: (activity: Activity, outState: Bundle?) -> Unit = { _, _ -> },
  crossinline onActivityDestroyed: (activity: Activity) -> Unit = { _ -> }
): Application.ActivityLifecycleCallbacks {

  val listener = object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
      onActivityCreated.invoke(activity, savedInstanceState)

    override fun onActivityStarted(activity: Activity) =
      onActivityStarted.invoke(activity)

    override fun onActivityResumed(activity: Activity) =
      onActivityResumed.invoke(activity)

    override fun onActivityPaused(activity: Activity) =
      onActivityPaused.invoke(activity)

    override fun onActivityStopped(activity: Activity) =
      onActivityStopped.invoke(activity)

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) =
      onActivitySaveInstanceState.invoke(activity, outState)

    override fun onActivityDestroyed(activity: Activity) =
      onActivityDestroyed.invoke(activity)
  }

  registerActivityLifecycleCallbacks(listener)

  return listener
}