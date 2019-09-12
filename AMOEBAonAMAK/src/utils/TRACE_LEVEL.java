package utils;

public enum TRACE_LEVEL {
	ERROR(200), NCS(100), EVENT(50), STATE(40), INFORM(20), DEBUG(0);

	private final int order;

	TRACE_LEVEL(final int order) {
		this.order = order;
	}

	public boolean isGE(final TRACE_LEVEL _other) {
		return order >= _other.order;
	}
}
