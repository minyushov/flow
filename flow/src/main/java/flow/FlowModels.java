//package flow;
//
//import android.support.annotation.NonNull;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//public class FlowModels {
//	private final Map<FlowModelsFactory, List<Object>> models = new LinkedHashMap<>();
//	private final ArrayList<Object> modelUsers = new ArrayList<>();
//
//	FlowModels(@NonNull List<FlowModelsFactory> flowModelsFactories) {
//		for (FlowModelsFactory factory : flowModelsFactories) {
//			models.put(factory, null);
//		}
//	}
//
//	void setUp(Object key) {
//		UseModel usage = key.getClass().getAnnotation(UseModel.class);
//		if (usage == null) {
//			return;
//		}
//
//		modelUsers.add(key);
//
//		for (FlowModelsFactory factory : models.keySet()) {
//			if (factory.getClass().isInstance(usage.modelFactory()) && models.get(factory) == null) {
//				models.put(factory, factory.createModels());
//				return;
//			}
//		}
//	}
//
//	void tearDown(Object key) {
//		UseModel usage = key.getClass().getAnnotation(UseModel.class);
//		if (usage == null) {
//			return;
//		}
//
//		modelUsers.remove(key);
//
//		for (Object user : modelUsers) {
//			UseModel otherUsage = user.getClass().getAnnotation(UseModel.class);
//			if (otherUsage != null && otherUsage.modelFactory() == usage.modelFactory()) {
//				return;
//			}
//		}
//
//		for (FlowModelsFactory factory : models.keySet()) {
//			if (factory.getClass().isInstance(usage.modelFactory()) && models.get(factory) != null) {
//				models.put(factory, null);
//				return;
//			}
//		}
//	}
//}
