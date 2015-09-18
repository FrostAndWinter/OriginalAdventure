package swen.adventure.datastorage;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
class Property {
    final String name;
    final Object value;
    final Class<?> class0;

    Property(String name, Object value, Class<?> class0) {
        this.name = name;
        this.value = value;
        this.class0 = class0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Property property = (Property) object;

        if (name != null ? !name.equals(property.name) : property.name != null) return false;
        if (value != null ? !value.equals(property.value) : property.value != null) return false;
        return !(class0 != null ? !class0.equals(property.class0) : property.class0 != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (class0 != null ? class0.hashCode() : 0);
        return result;
    }
}
