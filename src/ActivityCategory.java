public enum ActivityCategory {
    TRANSPORT,
    ELECTRICITY,
    WASTE,
    WATER;

    public static ActivityCategory fromString(String text) {
        for (ActivityCategory category : ActivityCategory.values()) {
            if (category.name().equalsIgnoreCase(text.trim())) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + text);
    }
}