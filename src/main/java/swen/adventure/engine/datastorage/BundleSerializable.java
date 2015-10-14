/* Contributor List  */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 package swen.adventure.engine.datastorage;

/**
 * BundleSerializable classes need to implement this special method:
 * {@code ANY-ACCESS-MODIFIER static *class name here* createFromBundle(BundleObject);}
 */
public interface BundleSerializable {

    /**
     * Convert this instance to its bundle object representation.
     *
     * @return a bundle object containing all information necessary to reconstruct this object.
     */
    BundleObject toBundle();
}