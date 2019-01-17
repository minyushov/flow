package flow;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class FlowModelManager {
  private final List<FlowModelScope> scopes;

  FlowModelManager(@NonNull List<FlowModelScope> scopes) {
    this.scopes = scopes;
  }

  Object getModel(@NonNull Class scopeClass, @NonNull String tag) {
    FlowModelScope scope = getScope(scopeClass);
    if (scope == null) {
      return null;
    }

    return scope.getModel(tag);
  }

  void setUp(@NonNull FlowModelUser key) {
    for (Class scopeClass: key.getRelations().getScopes()) {
      FlowModelScope scope = getScope(scopeClass);
      if (scope == null) {
        continue;
      }

      scope.setUp(key);
    }
  }

  void tearDown(@NonNull FlowModelUser key) {
    for (Class scopeClass: key.getRelations().getScopes()) {
      FlowModelScope scope = getScope(scopeClass);
      if (scope == null) {
        return;
      }

      scope.tearDown(key);
	}
  }

  @Nullable
  private FlowModelScope getScope(@NonNull Class scopeClass) {
    for (FlowModelScope scope : scopes) {
      if (scope.getClass().isAssignableFrom(scopeClass)) {
        return scope;
      }
    }

    return null;
  }

}
