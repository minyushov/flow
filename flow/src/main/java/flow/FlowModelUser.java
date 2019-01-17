package flow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

public interface FlowModelUser {
  class Relations {
    @NonNull
    private final Map<Class, String> relations;

    public Relations(@NonNull Map<Class, String> relations) {
      this.relations = relations;
    }

    Map<Class, String> getRelations() {
      return relations;
    }

    Set<Class> getScopes() {
      return relations.keySet();
    }

    Collection<String> getTags() {
      return relations.values();
    }
  }

  @NonNull
  Relations getRelations();
}