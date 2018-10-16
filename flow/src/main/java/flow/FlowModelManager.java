package flow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

final class FlowModelManager {
  private final List<FlowModelScope> scopes;

  FlowModelManager(@NonNull List<FlowModelScope> scopes) {
    this.scopes = scopes;
  }

  Object getModel(@NonNull Object key) {
    FlowModelScope scope = getScope(key);
    if (scope == null) {
      return null;
    }

    return scope.getModel((FlowModelUser) key);
  }

  void setUp(@NonNull Object key) {
    FlowModelScope scope = getScope(key);
    if (scope == null) {
      return;
    }

    scope.setUp(((FlowModelUser) key));
  }

  void tearDown(@NonNull Object key) {
    FlowModelScope scope = getScope(key);
    if (scope == null) {
      return;
    }

    scope.tearDown(((FlowModelUser) key));
  }

  @Nullable
  private FlowModelScope getScope(@NonNull Object key) {
    if (!(key instanceof FlowModelUser)) {
      return null;
    }

    Class scopeClass = ((FlowModelUser) key).getScope();

    for (FlowModelScope scope : scopes) {
      if (scope.getClass().isAssignableFrom(scopeClass)) {
        return scope;
      }
    }

    return null;
  }

}
