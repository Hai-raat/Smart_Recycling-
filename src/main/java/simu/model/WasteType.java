package simu.model;

public enum WasteType {
	PLASTIC("Plastic"), PAPER("Paper"), GLASS("Glass"), METAL("Metal"), CONTAMINATED("Contaminated");

	private final String label;
	WasteType(String label) { this.label = label; }
	public String getLabel() { return label; }
}
