package swen.adventure.engine.datastorage;

/**
 * BundleSerializable classes need to implement this special method:
 * {@code ANY-ACCESS-MODIFIER static *class name here* createFromBundle(BundleObject);}
 */
public interface BundleSerializable {
    BundleObject toBundle();
}
