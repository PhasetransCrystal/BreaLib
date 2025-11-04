package com.phasetranscrystal.brealib.api.material.property;

public class PropertyKey<T extends IMaterialProperty> {

    // Empty property used to allow property-less Materials without removing base type enforcement
    public static final PropertyKey<EmptyProperty> EMPTY = new PropertyKey<>("empty", EmptyProperty.class);

    private final String key;
    private final Class<T> type;

    public PropertyKey(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    protected String getKey() {
        return key;
    }

    protected T constructDefault() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public T cast(IMaterialProperty property) {
        return this.type.cast(property);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PropertyKey) {
            return ((PropertyKey<?>) o).getKey().equals(key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    public static class EmptyProperty implements IMaterialProperty {

        private EmptyProperty() {}

        @Override
        public void verifyProperty(MaterialProperties properties) {
            // no-op
        }
    }
}
