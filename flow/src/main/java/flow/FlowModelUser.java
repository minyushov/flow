package flow;

import androidx.annotation.NonNull;

public interface FlowModelUser {
  @NonNull
  Class getScope();

  @NonNull
  String getTag();
}