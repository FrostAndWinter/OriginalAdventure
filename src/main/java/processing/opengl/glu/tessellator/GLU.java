/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package processing.opengl.glu.tessellator;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 23/09/15.
 */
public class GLU {
    protected static boolean availableGLUtessellatorImpl;
    protected static boolean checkedGLUtessellatorImpl;
    public static final int GLU_FALSE = 0;
    public static final int GLU_TRUE = 1;
    public static final int GLU_VERSION = 100800;
    public static final int GLU_EXTENSIONS = 100801;
    public static final String versionString = "1.3";
    public static final String extensionString = "GLU_EXT_nurbs_tessellator GLU_EXT_object_space_tess ";
    public static final int GLU_INVALID_ENUM = 100900;
    public static final int GLU_INVALID_VALUE = 100901;
    public static final int GLU_OUT_OF_MEMORY = 100902;
    public static final int GLU_INVALID_OPERATION = 100904;
    public static final int GLU_POINT = 100010;
    public static final int GLU_LINE = 100011;
    public static final int GLU_FILL = 100012;
    public static final int GLU_SILHOUETTE = 100013;
    public static final int GLU_SMOOTH = 100000;
    public static final int GLU_FLAT = 100001;
    public static final int GLU_NONE = 100002;
    public static final int GLU_OUTSIDE = 100020;
    public static final int GLU_INSIDE = 100021;
    public static final int GLU_ERROR = 100103;
    public static final int GLU_TESS_BEGIN = 100100;
    public static final int GLU_BEGIN = 100100;
    public static final int GLU_TESS_VERTEX = 100101;
    public static final int GLU_VERTEX = 100101;
    public static final int GLU_TESS_END = 100102;
    public static final int GLU_END = 100102;
    public static final int GLU_TESS_ERROR = 100103;
    public static final int GLU_TESS_EDGE_FLAG = 100104;
    public static final int GLU_EDGE_FLAG = 100104;
    public static final int GLU_TESS_COMBINE = 100105;
    public static final int GLU_TESS_BEGIN_DATA = 100106;
    public static final int GLU_TESS_VERTEX_DATA = 100107;
    public static final int GLU_TESS_END_DATA = 100108;
    public static final int GLU_TESS_ERROR_DATA = 100109;
    public static final int GLU_TESS_EDGE_FLAG_DATA = 100110;
    public static final int GLU_TESS_COMBINE_DATA = 100111;
    public static final int GLU_CW = 100120;
    public static final int GLU_CCW = 100121;
    public static final int GLU_INTERIOR = 100122;
    public static final int GLU_EXTERIOR = 100123;
    public static final int GLU_UNKNOWN = 100124;
    public static final int GLU_TESS_WINDING_RULE = 100140;
    public static final int GLU_TESS_BOUNDARY_ONLY = 100141;
    public static final int GLU_TESS_TOLERANCE = 100142;
    public static final int GLU_TESS_AVOID_DEGENERATE_TRIANGLES = 100149;
    public static final int GLU_TESS_ERROR1 = 100151;
    public static final int GLU_TESS_ERROR2 = 100152;
    public static final int GLU_TESS_ERROR3 = 100153;
    public static final int GLU_TESS_ERROR4 = 100154;
    public static final int GLU_TESS_ERROR5 = 100155;
    public static final int GLU_TESS_ERROR6 = 100156;
    public static final int GLU_TESS_ERROR7 = 100157;
    public static final int GLU_TESS_ERROR8 = 100158;
    public static final int GLU_TESS_MISSING_BEGIN_POLYGON = 100151;
    public static final int GLU_TESS_MISSING_BEGIN_CONTOUR = 100152;
    public static final int GLU_TESS_MISSING_END_POLYGON = 100153;
    public static final int GLU_TESS_MISSING_END_CONTOUR = 100154;
    public static final int GLU_TESS_COORD_TOO_LARGE = 100155;
    public static final int GLU_TESS_NEED_COMBINE_CALLBACK = 100156;
    public static final int GLU_TESS_WINDING_ODD = 100130;
    public static final int GLU_TESS_WINDING_NONZERO = 100131;
    public static final int GLU_TESS_WINDING_POSITIVE = 100132;
    public static final int GLU_TESS_WINDING_NEGATIVE = 100133;
    public static final int GLU_TESS_WINDING_ABS_GEQ_TWO = 100134;
    public static final double GLU_TESS_MAX_COORD = 1.0E150D;
}