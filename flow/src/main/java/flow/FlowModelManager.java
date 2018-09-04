package flow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

final class FlowModelManager {
	private final List<FlowModelAdapter> adapters;

	FlowModelManager(@NonNull List<FlowModelAdapter> adapters) {
		this.adapters = adapters;
	}

	Object getModel(@NonNull Object key) {
		FlowModelAdapter adapter = getAdapter(key);
		if (adapter == null) {
			return null;
		}

		return adapter.getModel((FlowModelUser) key);
	}

	void setUp(@NonNull Object key) {
		FlowModelAdapter adapter = getAdapter(key);
		if (adapter == null) {
			return;
		}

		adapter.setUp(((FlowModelUser) key));
	}

	void tearDown(@NonNull Object key) {
		FlowModelAdapter adapter = getAdapter(key);
		if (adapter == null) {
			return;
		}

		adapter.tearDown(((FlowModelUser) key));
	}

	@Nullable
	private FlowModelAdapter getAdapter(@NonNull Object key) {
		if (!(key instanceof FlowModelUser)) {
			return null;
		}

		Class adapterClass = ((FlowModelUser) key).getAdapterClass();

		for (FlowModelAdapter adapter : adapters) {
			if (adapter.getClass().isAssignableFrom(adapterClass)) {
				return adapter;
			}
		}

		return null;
	}

}
