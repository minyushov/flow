package flow;

import android.support.annotation.NonNull;

public interface FlowModelUser {
	@NonNull
	Class getAdapterClass();

	@NonNull
	String getTag();
}
