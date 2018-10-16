package flow;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class FlowModelScope {
  private final static String TAGS = "flow-model-scope:tags";

  // tag to model relations
  private final HashMap<String, Object> models = new LinkedHashMap<>();
  // tag to users relations
  private final HashMap<String, ArrayList<FlowModelUser>> users = new LinkedHashMap<>();

  final void setUp(@NonNull FlowModelUser user) {
    Object model = models.get(user.getTag());
    if (model == null) {
      models.put(user.getTag(), createModel());
      users.put(user.getTag(), new ArrayList<>());
    }

    users.get(user.getTag()).add(user);
  }

  final void tearDown(@NonNull FlowModelUser user) {
    ArrayList<FlowModelUser> modelUsers = users.get(user.getTag());
    if (modelUsers == null) {
      return;
    }

    modelUsers.remove(user);

    if (modelUsers.isEmpty()) {
      models.remove(user.getTag());
      users.remove(user.getTag());
    }
  }

  @NonNull final Object getModel(@NonNull FlowModelUser user) {
    Object model = models.get(user.getTag());
    if (model == null) {
      throw new IllegalStateException("No model currently exists for this user");
    }

    return model;
  }

  public final void saveModels(@NonNull Bundle bundle) {
    HashMap<String, Parcelable> modelsToSave = new LinkedHashMap<>();

    for (Map.Entry<String, Object> entry : models.entrySet()) {
      if (entry.getKey() != null && entry.getValue() instanceof Parcelable) {
        modelsToSave.put(entry.getKey(), (Parcelable) entry.getValue());
      }
    }

    ArrayList<String> tags = new ArrayList<>(modelsToSave.keySet());
    bundle.putStringArrayList(TAGS, tags);

    for (Map.Entry<String, Parcelable> modelToSave : modelsToSave.entrySet()) {
      bundle.putParcelable(modelToSave.getKey(), modelToSave.getValue());
    }
  }

  public final void restoreModels(@NonNull Bundle bundle) {
    ArrayList<String> tags = bundle.getStringArrayList(TAGS);
    if (tags == null) {
      return;
    }

    for (String tag : tags) {
      models.put(tag, bundle.getParcelable(tag));
      users.put(tag, new ArrayList<>());
    }
  }

  abstract public Object createModel();
}
