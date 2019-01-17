package flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import androidx.annotation.NonNull;

public interface FlowModelUser {
  class Relations {
  	@NonNull
    private final HashMap<Class, String> relations;

    public Relations(@NonNull HashMap<Class, String> relations) {
      this.relations = relations;
    }

    HashMap<Class, String> getRelations() {
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