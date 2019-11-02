package flow.sample.multikey

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import flow.DefaultKeyDispatcher
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import flow.flow
import flow.withFlow

/**
 * Demonstrates MultiKeys, e.g. screens with dialogs.
 */
class MultiKeySampleActivity : AppCompatActivity() {

  override fun attachBaseContext(baseContext: Context) {
    super.attachBaseContext(
      baseContext.withFlow(
        activity = this,
        dispatcher = DefaultKeyDispatcher(
          activity = this,
          keyChanger = Changer()
        ),
        defaultKey = ScreenOne()
      )
    )
  }

  override fun onBackPressed() {
    flow.goBack()
  }

  private inner class Changer : KeyChanger {
    internal var visibleDialog: Dialog? = null

    override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
    ) {

      val mainKey: Any
      val dialogKey: Any?

      val showThis = incomingState.getKey<Any>()
      if (showThis is DialogScreen) {
        mainKey = showThis.mainContent
        dialogKey = showThis
      } else {
        mainKey = showThis
        dialogKey = null
      }

      val mainView = TextView(incomingContexts[mainKey])
      if (mainKey is ScreenOne) {
        mainView.setOnClickListener { flow.set(DialogScreen(mainKey)) }
      } else {
        mainView.setOnClickListener { flow.set(ScreenOne()) }
      }
      mainView.text = mainKey.toString()

      setContentView(mainView)

      dismissOldDialog()
      if (dialogKey != null) {
        val context = incomingContexts[dialogKey] ?: throw IllegalStateException()
        visibleDialog = AlertDialog.Builder(context)
          .setNegativeButton("No") { _, _ -> flow.goBack() }
          .setOnCancelListener { flow.goBack() }
          .setPositiveButton("Yes") { _, _ ->
            flow.set(ScreenTwo())

            // In real life you'd be more likely to do something like this,
            // to prevent the dialog from showing up again when the back
            // button is hit.
            //
            //val newHistory = flow.history.buildUpon()
            //newHistory.pop() // drop the dialog
            //newHistory.push(ScreenTwo())
            //flow.setHistory(newHistory.build(), Direction.FORWARD)
          }
          .setTitle(dialogKey.toString())
          .show()

        // Prevent logging of android.view.WindowLeaked.
        lateinit var listener: Application.ActivityLifecycleCallbacks
        listener = application.onActivityDestroyed {
          application.unregisterActivityLifecycleCallbacks(listener)
          dismissOldDialog()
        }
      }

      callback.onTraversalCompleted()
    }

    private fun dismissOldDialog() {
      visibleDialog?.dismiss()
      visibleDialog = null
    }
  }

}