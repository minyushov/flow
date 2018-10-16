package flow;

import android.support.annotation.NonNull;

public interface FlowModelUser {
  @NonNull
  Class getScope();

  @NonNull
  String getTag();
}