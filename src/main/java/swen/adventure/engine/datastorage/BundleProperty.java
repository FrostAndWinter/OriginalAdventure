package swen.adventure.engine.datastorage;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 *
 * BundleProperty is a container class which wraps up the information in a property in BundleObject or BundleArray.
 */
class BundleProperty {

    final String name;
    final Object value;
    final Class<?> class0;

    /**
     * Construct a new BundleProperty from the name, value and type.
     *
     * @param name name of the property.
     * @param value value of the property.
     * @param class0 type of the value in the property.
     */
    BundleProperty(String name, Object value, Class<?> class0) {
        if(value.getClass() != class0)
            throw new IllegalArgumentException();

        this.name = name;
        this.value = value;
        this.class0 = class0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        BundleProperty property = (BundleProperty) object;

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
