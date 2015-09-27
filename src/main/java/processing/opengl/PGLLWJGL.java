package processing.opengl;

import processing.opengl.glu.tessellator.GLU;
import processing.opengl.glu.tessellator.GLUtessellatorCallback;
import processing.opengl.glu.tessellator.GLUtessellatorImpl;
import swen.adventure.Utilities;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.*;
import java.util.List;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BINDING_RECTANGLE;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 23/09/15.
 */
public class PGLLWJGL extends PGL {

    // Utility arrays to copy projection/modelview matrices to GL

    protected float[] projMatrix;
    protected float[] mvMatrix;


    public PGLLWJGL(PGraphicsOpenGL pg) {
        super(pg);
    }

    ///////////////////////////////////////////////////////////////

    // Public methods to get/set renderer's properties


    @Override
    protected float getPixelScale() {
        return graphics.pixelDensity;
    }


    @Override
    protected void getGL(PGL pgl) {
    }


    @Override
    protected boolean canDraw() { return true; }


    @Override
    protected  void requestFocus() {}


    @Override
    protected  void requestDraw() {}


    @Override
    protected void swapBuffers()  {
    }


    @Override
    protected void initFBOLayer() {

    }


    @Override
    protected void beginGL() {
    }

    ///////////////////////////////////////////////////////////

    // Utility functions


    @Override
    protected void enableTexturing(int target) {
        if (target == TEXTURE_2D) {
            texturingTargets[0] = true;
        } else if (target == TEXTURE_RECTANGLE) {
            texturingTargets[1] = true;
        }
    }


    @Override
    protected void disableTexturing(int target) {
        if (target == TEXTURE_2D) {
            texturingTargets[0] = false;
        } else if (target == TEXTURE_RECTANGLE) {
            texturingTargets[1] = false;
        }
    }


    /**
     * Convenience method to get a legit FontMetrics object. Where possible,
     * override this any renderer subclass so that you're not using what's
     * returned by getDefaultToolkit() to get your metrics.
     */
    @SuppressWarnings("deprecation")
    private FontMetrics getFontMetrics(Font font) {  // ignore
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }


    /**
     * Convenience method to jump through some Java2D hoops and get an FRC.
     */
    private FontRenderContext getFontRenderContext(Font font) {  // ignore
        return getFontMetrics(font).getFontRenderContext();
    }


    @Override
    protected int getFontAscent(Object font) {
        return getFontMetrics((Font) font).getAscent();
    }


    @Override
    protected int getFontDescent(Object font) {
        return getFontMetrics((Font) font).getDescent();
    }


    @Override
    protected int getTextWidth(Object font, char[] buffer, int start, int stop) {
        // maybe should use one of the newer/fancier functions for this?
        int length = stop - start;
        FontMetrics metrics = getFontMetrics((Font) font);
        return metrics.charsWidth(buffer, start, length);
    }


    @Override
    protected Object getDerivedFont(Object font, float size) {
        return ((Font) font).deriveFont(size);
    }


    @Override
    protected String[] loadVertexShader(String filename) {
        return loadVertexShader(filename, getGLSLVersion());
    }


    @Override
    protected String[] loadFragmentShader(String filename) {
        return loadFragmentShader(filename, getGLSLVersion());
    }


    @Override
    protected String[] loadVertexShader(URL url) {
        return loadVertexShader(url, getGLSLVersion());
    }


    @Override
    protected String[] loadFragmentShader(URL url) {
        return loadFragmentShader(url, getGLSLVersion());
    }


    @Override
    protected String[] loadFragmentShader(String filename, int version) {
        try {
            List<String> fragSrc = Utilities.readLinesFromFile(Utilities.pathForResource(filename, null));
            String[] fragSrc0 = fragSrc.toArray(new String[fragSrc.size()]);
            return preprocessFragmentSource(fragSrc0, version);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected String[] loadVertexShader(String filename, int version) {
        try {
            List<String> vertSrc = Utilities.readLinesFromFile(Utilities.pathForResource(filename, null));
            String[] vertSrc0 = vertSrc.toArray(new String[vertSrc.size()]);
            return preprocessVertexSource(vertSrc0, version);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected String[] loadFragmentShader(URL url, int version) {
        try {
            List<String> fragSrc = Utilities.readLinesFromFile(url.toURI().getPath());
            String[] fragSrc0 = fragSrc.toArray(new String[fragSrc.size()]);
            return preprocessFragmentSource(fragSrc0, version);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected String[] loadVertexShader(URL url, int version) {
        try {
            List<String> vertSrc = Utilities.readLinesFromFile(url.toURI().getPath());
            String[] vertSrc0 = vertSrc.toArray(new String[vertSrc.size()]);
            return preprocessVertexSource(vertSrc0, version);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    ///////////////////////////////////////////////////////////

    // Tessellator


    @Override
    protected Tessellator createTessellator(TessellatorCallback callback) {
        return new Tessellator(callback);
    }


    protected static class Tessellator implements PGL.Tessellator {
        protected GLUtessellatorImpl tess;
        protected TessellatorCallback callback;
        protected GLUCallback gluCallback;

        public static final int GLU_TESS_BEGIN = 100100;
        public static final int GLU_TESS_COMBINE = 100105;
        public static final int GLU_TESS_VERTEX = 100101;
        public static final int GLU_TESS_END = 100102;
        public static final int GLU_TESS_ERROR = 100103;

        public Tessellator(TessellatorCallback callback) {
            this.callback = callback;
            tess = GLUtessellatorImpl.gluNewTess();
            gluCallback = new GLUCallback();

            tess.gluTessCallback(GLU_TESS_BEGIN, gluCallback);
            tess.gluTessCallback(GLU_TESS_END, gluCallback);
            tess.gluTessCallback(GLU_TESS_VERTEX, gluCallback);
            tess.gluTessCallback(GLU_TESS_COMBINE, gluCallback);
            tess.gluTessCallback(GLU_TESS_ERROR, gluCallback);
        }

        @Override
        public void beginPolygon() {
            tess.gluTessBeginPolygon(null);
        }

        @Override
        public void endPolygon() {
            tess.gluTessEndPolygon();
        }

        @Override
        public void setWindingRule(int rule) {
            tess.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, rule);
        }

        @Override
        public void beginContour() {
            tess.gluTessBeginContour();
        }

        @Override
        public void endContour() {
            tess.gluTessEndContour();
        }

        @Override
        public void addVertex(double[] v) {
            tess.gluTessVertex(v, 0, v);
        }

        protected class GLUCallback implements GLUtessellatorCallback {
            @Override
            public void begin(int type) {
                callback.begin(type);
            }

            @Override
            public void beginData(final int var1, final Object var2) {

            }

            @Override
            public void edgeFlag(final boolean var1) {

            }

            @Override
            public void edgeFlagData(final boolean var1, final Object var2) {

            }

            @Override
            public void end() {
                callback.end();
            }

            @Override
            public void endData(final Object var1) {

            }

            @Override
            public void vertex(Object data) {
                callback.vertex(data);
            }

            @Override
            public void vertexData(final Object var1, final Object var2) {

            }

            @Override
            public void combine(double[] coords, Object[] data,
                                float[] weight, Object[] outData) {
                callback.combine(coords, data, weight, outData);
            }

            @Override
            public void combineData(final double[] var1, final Object[] var2, final float[] var3, final Object[] var4, final Object var5) {

            }

            @Override
            public void error(int errnum) {
                callback.error(errnum);
            }

            @Override
            public void errorData(final int var1, final Object var2) {

            }
        }
    }


    @Override
    protected String tessError(int err) {
        return processing.opengl.glu.error.Error.gluErrorString(err);
    }


    ///////////////////////////////////////////////////////////

    // Font outline


    static {
        SHAPE_TEXT_SUPPORTED = true;
        SEG_MOVETO  = PathIterator.SEG_MOVETO;
        SEG_LINETO  = PathIterator.SEG_LINETO;
        SEG_QUADTO  = PathIterator.SEG_QUADTO;
        SEG_CUBICTO = PathIterator.SEG_CUBICTO;
        SEG_CLOSE   = PathIterator.SEG_CLOSE;
    }


    @Override
    protected FontOutline createFontOutline(char ch, Object font) {
        return new FontOutline(ch, (Font) font);
    }


    protected class FontOutline implements PGL.FontOutline {
        PathIterator iter;

        public FontOutline(char ch, Font font) {
            char textArray[] = new char[] { ch };
            FontRenderContext frc = getFontRenderContext(font);
            GlyphVector gv = font.createGlyphVector(frc, textArray);
            Shape shp = gv.getOutline();
            iter = shp.getPathIterator(null);
        }

        public boolean isDone() {
            return iter.isDone();
        }

        public int currentSegment(float coords[]) {
            return iter.currentSegment(coords);
        }

        public void next() {
            iter.next();
        }
    }


    ///////////////////////////////////////////////////////////

    // Constants

    static {
        FALSE = GL_FALSE;
        TRUE  = GL_TRUE;

        INT            = GL_INT;
        BYTE           = GL_BYTE;
        SHORT          = GL_SHORT;
        FLOAT          = GL_FLOAT;
        BOOL           = GL_BOOL;
        UNSIGNED_INT   = GL_UNSIGNED_INT;
        UNSIGNED_BYTE  = GL_UNSIGNED_BYTE;
        UNSIGNED_SHORT = GL_UNSIGNED_SHORT;

        RGB             = GL_RGB;
        RGBA            = GL_RGBA;
        ALPHA           = GL_ALPHA;
        LUMINANCE       = GL_LUMINANCE;
        LUMINANCE_ALPHA = GL_LUMINANCE_ALPHA;

        UNSIGNED_SHORT_5_6_5   = GL_UNSIGNED_SHORT_5_6_5;
        UNSIGNED_SHORT_4_4_4_4 = GL_UNSIGNED_SHORT_4_4_4_4;
        UNSIGNED_SHORT_5_5_5_1 = GL_UNSIGNED_SHORT_5_5_5_1;

        RGBA4   = GL_RGBA4;
        RGB5_A1 = GL_RGB5_A1;
        RGB8    = GL_RGB8;
        RGBA8   = GL_RGBA8;
        ALPHA8  = GL_ALPHA8;

        READ_ONLY  = GL_READ_ONLY;
        WRITE_ONLY = GL_WRITE_ONLY;
        READ_WRITE = GL_READ_WRITE;

        TESS_WINDING_NONZERO = GLU.GLU_TESS_WINDING_NONZERO;
        TESS_WINDING_ODD     = GLU.GLU_TESS_WINDING_ODD;

        GENERATE_MIPMAP_HINT = GL_GENERATE_MIPMAP_HINT;
        FASTEST              = GL_FASTEST;
        NICEST               = GL_NICEST;
        DONT_CARE            = GL_DONT_CARE;

        VENDOR                   = GL_VENDOR;
        RENDERER                 = GL_RENDERER;
        VERSION                  = GL_VERSION;
        EXTENSIONS               = GL_EXTENSIONS;
        SHADING_LANGUAGE_VERSION = GL_SHADING_LANGUAGE_VERSION;

        MAX_SAMPLES = GL_MAX_SAMPLES;
        SAMPLES     = GL_SAMPLES;

        ALIASED_LINE_WIDTH_RANGE = GL_ALIASED_LINE_WIDTH_RANGE;
        ALIASED_POINT_SIZE_RANGE = GL_ALIASED_POINT_SIZE_RANGE;

        DEPTH_BITS   = GL_DEPTH_BITS;
        STENCIL_BITS = GL_STENCIL_BITS;

        CCW = GL_CCW;
        CW  = GL_CW;

        VIEWPORT = GL_VIEWPORT;

        ARRAY_BUFFER         = GL_ARRAY_BUFFER;
        ELEMENT_ARRAY_BUFFER = GL_ELEMENT_ARRAY_BUFFER;

        MAX_VERTEX_ATTRIBS  = GL_MAX_VERTEX_ATTRIBS;

        STATIC_DRAW  = GL_STATIC_DRAW;
        DYNAMIC_DRAW = GL_DYNAMIC_DRAW;
        STREAM_DRAW  = GL_STREAM_DRAW;

        ByteBuffer_SIZE  = GL_BUFFER_SIZE;
        ByteBuffer_USAGE = GL_BUFFER_USAGE;

        POINTS         = GL_POINTS;
        LINE_STRIP     = GL_LINE_STRIP;
        LINE_LOOP      = GL_LINE_LOOP;
        LINES          = GL_LINES;
        TRIANGLE_FAN   = GL_TRIANGLE_FAN;
        TRIANGLE_STRIP = GL_TRIANGLE_STRIP;
        TRIANGLES      = GL_TRIANGLES;

        CULL_FACE      = GL_CULL_FACE;
        FRONT          = GL_FRONT;
        BACK           = GL_BACK;
        FRONT_AND_BACK = GL_FRONT_AND_BACK;

        POLYGON_OFFSET_FILL = GL_POLYGON_OFFSET_FILL;

        UNPACK_ALIGNMENT = GL_UNPACK_ALIGNMENT;
        PACK_ALIGNMENT   = GL_PACK_ALIGNMENT;

        TEXTURE_2D        = GL_TEXTURE_2D;
        TEXTURE_RECTANGLE = GL_TEXTURE_RECTANGLE;

        TEXTURE_BINDING_2D        = GL_TEXTURE_BINDING_2D;
        TEXTURE_BINDING_RECTANGLE = GL_TEXTURE_BINDING_RECTANGLE;

        MAX_TEXTURE_SIZE           = GL_MAX_TEXTURE_SIZE;
        TEXTURE_MAX_ANISOTROPY     = GL_TEXTURE_MAX_ANISOTROPY_EXT;
        MAX_TEXTURE_MAX_ANISOTROPY = GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;

        MAX_VERTEX_TEXTURE_IMAGE_UNITS   = GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
        MAX_TEXTURE_IMAGE_UNITS          = GL_MAX_TEXTURE_IMAGE_UNITS;
        MAX_COMBINED_TEXTURE_IMAGE_UNITS = GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

        NUM_COMPRESSED_TEXTURE_FORMATS = GL_NUM_COMPRESSED_TEXTURE_FORMATS;
        COMPRESSED_TEXTURE_FORMATS     = GL_COMPRESSED_TEXTURE_FORMATS;

        NEAREST               = GL_NEAREST;
        LINEAR                = GL_LINEAR;
        LINEAR_MIPMAP_NEAREST = GL_LINEAR_MIPMAP_NEAREST;
        LINEAR_MIPMAP_LINEAR  = GL_LINEAR_MIPMAP_LINEAR;

        CLAMP_TO_EDGE = GL_CLAMP_TO_EDGE;
        REPEAT        = GL_REPEAT;

        TEXTURE0           = GL_TEXTURE0;
        TEXTURE1           = GL_TEXTURE1;
        TEXTURE2           = GL_TEXTURE2;
        TEXTURE3           = GL_TEXTURE3;
        TEXTURE_MIN_FILTER = GL_TEXTURE_MIN_FILTER;
        TEXTURE_MAG_FILTER = GL_TEXTURE_MAG_FILTER;
        TEXTURE_WRAP_S     = GL_TEXTURE_WRAP_S;
        TEXTURE_WRAP_T     = GL_TEXTURE_WRAP_T;
        TEXTURE_WRAP_R     = GL_TEXTURE_WRAP_R;

        TEXTURE_CUBE_MAP = GL_TEXTURE_CUBE_MAP;
        TEXTURE_CUBE_MAP_POSITIVE_X = GL_TEXTURE_CUBE_MAP_POSITIVE_X;
        TEXTURE_CUBE_MAP_POSITIVE_Y = GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
        TEXTURE_CUBE_MAP_POSITIVE_Z = GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
        TEXTURE_CUBE_MAP_NEGATIVE_X = GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
        TEXTURE_CUBE_MAP_NEGATIVE_Y = GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
        TEXTURE_CUBE_MAP_NEGATIVE_Z = GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;

        VERTEX_SHADER        = GL_VERTEX_SHADER;
        FRAGMENT_SHADER      = GL_FRAGMENT_SHADER;
        INFO_LOG_LENGTH      = GL_INFO_LOG_LENGTH;
        SHADER_SOURCE_LENGTH = GL_SHADER_SOURCE_LENGTH;
        COMPILE_STATUS       = GL_COMPILE_STATUS;
        LINK_STATUS          = GL_LINK_STATUS;
        VALIDATE_STATUS      = GL_VALIDATE_STATUS;
        SHADER_TYPE          = GL_SHADER_TYPE;
        DELETE_STATUS        = GL_DELETE_STATUS;

        FLOAT_VEC2   = GL_FLOAT_VEC2;
        FLOAT_VEC3   = GL_FLOAT_VEC3;
        FLOAT_VEC4   = GL_FLOAT_VEC4;
        FLOAT_MAT2   = GL_FLOAT_MAT2;
        FLOAT_MAT3   = GL_FLOAT_MAT3;
        FLOAT_MAT4   = GL_FLOAT_MAT4;
        INT_VEC2     = GL_INT_VEC2;
        INT_VEC3     = GL_INT_VEC3;
        INT_VEC4     = GL_INT_VEC4;
        BOOL_VEC2    = GL_BOOL_VEC2;
        BOOL_VEC3    = GL_BOOL_VEC3;
        BOOL_VEC4    = GL_BOOL_VEC4;
        SAMPLER_2D   = GL_SAMPLER_2D;
        SAMPLER_CUBE = GL_SAMPLER_CUBE;

        CURRENT_VERTEX_ATTRIB = GL_CURRENT_VERTEX_ATTRIB;

        VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
        VERTEX_ATTRIB_ARRAY_ENABLED        = GL_VERTEX_ATTRIB_ARRAY_ENABLED;
        VERTEX_ATTRIB_ARRAY_SIZE           = GL_VERTEX_ATTRIB_ARRAY_SIZE;
        VERTEX_ATTRIB_ARRAY_STRIDE         = GL_VERTEX_ATTRIB_ARRAY_STRIDE;
        VERTEX_ATTRIB_ARRAY_TYPE           = GL_VERTEX_ATTRIB_ARRAY_TYPE;
        VERTEX_ATTRIB_ARRAY_NORMALIZED     = GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
        VERTEX_ATTRIB_ARRAY_POINTER        = GL_VERTEX_ATTRIB_ARRAY_POINTER;

        BLEND               = GL_BLEND;
        ONE                 = GL_ONE;
        ZERO                = GL_ZERO;
        SRC_ALPHA           = GL_SRC_ALPHA;
        DST_ALPHA           = GL_DST_ALPHA;
        ONE_MINUS_SRC_ALPHA = GL_ONE_MINUS_SRC_ALPHA;
        ONE_MINUS_DST_COLOR = GL_ONE_MINUS_DST_COLOR;
        ONE_MINUS_SRC_COLOR = GL_ONE_MINUS_SRC_COLOR;
        DST_COLOR           = GL_DST_COLOR;
        SRC_COLOR           = GL_SRC_COLOR;

        SAMPLE_ALPHA_TO_COVERAGE = GL_SAMPLE_ALPHA_TO_COVERAGE;
        SAMPLE_COVERAGE          = GL_SAMPLE_COVERAGE;

        KEEP      = GL_KEEP;
        REPLACE   = GL_REPLACE;
        INCR      = GL_INCR;
        DECR      = GL_DECR;
        INVERT    = GL_INVERT;
        INCR_WRAP = GL_INCR_WRAP;
        DECR_WRAP = GL_DECR_WRAP;
        NEVER     = GL_NEVER;
        ALWAYS    = GL_ALWAYS;

        EQUAL    = GL_EQUAL;
        LESS     = GL_LESS;
        LEQUAL   = GL_LEQUAL;
        GREATER  = GL_GREATER;
        GEQUAL   = GL_GEQUAL;
        NOTEQUAL = GL_NOTEQUAL;

        FUNC_ADD              = GL_FUNC_ADD;
        FUNC_MIN              = GL_MIN;
        FUNC_MAX              = GL_MAX;
        FUNC_REVERSE_SUBTRACT = GL_FUNC_REVERSE_SUBTRACT;
        FUNC_SUBTRACT         = GL_FUNC_SUBTRACT;

        DITHER = GL_DITHER;

        CONSTANT_COLOR           = GL_CONSTANT_COLOR;
        CONSTANT_ALPHA           = GL_CONSTANT_ALPHA;
        ONE_MINUS_CONSTANT_COLOR = GL_ONE_MINUS_CONSTANT_COLOR;
        ONE_MINUS_CONSTANT_ALPHA = GL_ONE_MINUS_CONSTANT_ALPHA;
        SRC_ALPHA_SATURATE       = GL_SRC_ALPHA_SATURATE;

        SCISSOR_TEST    = GL_SCISSOR_TEST;
        STENCIL_TEST    = GL_STENCIL_TEST;
        DEPTH_TEST      = GL_DEPTH_TEST;
        DEPTH_WRITEMASK = GL_DEPTH_WRITEMASK;

        COLOR_BUFFER_BIT   = GL_COLOR_BUFFER_BIT;
        DEPTH_BUFFER_BIT   = GL_DEPTH_BUFFER_BIT;
        STENCIL_BUFFER_BIT = GL_STENCIL_BUFFER_BIT;

        FRAMEBUFFER        = GL_FRAMEBUFFER;
        COLOR_ATTACHMENT0  = GL_COLOR_ATTACHMENT0;
        COLOR_ATTACHMENT1  = GL_COLOR_ATTACHMENT1;
        COLOR_ATTACHMENT2  = GL_COLOR_ATTACHMENT2;
        COLOR_ATTACHMENT3  = GL_COLOR_ATTACHMENT3;
        RENDERBUFFER       = GL_RENDERBUFFER;
        DEPTH_ATTACHMENT   = GL_DEPTH_ATTACHMENT;
        STENCIL_ATTACHMENT = GL_STENCIL_ATTACHMENT;
        READ_FRAMEBUFFER   = GL_READ_FRAMEBUFFER;
        DRAW_FRAMEBUFFER   = GL_DRAW_FRAMEBUFFER;

        RGBA8            = GL_RGBA8;
        DEPTH24_STENCIL8 = GL_DEPTH24_STENCIL8;

        DEPTH_COMPONENT   = GL_DEPTH_COMPONENT;
        DEPTH_COMPONENT16 = GL_DEPTH_COMPONENT16;
        DEPTH_COMPONENT24 = GL_DEPTH_COMPONENT24;
        DEPTH_COMPONENT32 = GL_DEPTH_COMPONENT32;

        STENCIL_INDEX  = GL_STENCIL_INDEX;
        STENCIL_INDEX1 = GL_STENCIL_INDEX1;
        STENCIL_INDEX4 = GL_STENCIL_INDEX4;
        STENCIL_INDEX8 = GL_STENCIL_INDEX8;

        DEPTH_STENCIL = GL_DEPTH_STENCIL;

        FRAMEBUFFER_COMPLETE                      = GL_FRAMEBUFFER_COMPLETE;
        FRAMEBUFFER_INCOMPLETE_ATTACHMENT         = GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
        FRAMEBUFFER_INCOMPLETE_DIMENSIONS         = GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT;
        FRAMEBUFFER_INCOMPLETE_FORMATS            = GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT;
        FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        = GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
        FRAMEBUFFER_INCOMPLETE_READ_BUFFER        = GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
        FRAMEBUFFER_UNSUPPORTED                   = GL_FRAMEBUFFER_UNSUPPORTED;

        FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE           = GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE;
        FRAMEBUFFER_ATTACHMENT_OBJECT_NAME           = GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL         = GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL;
        FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE;

        RENDERBUFFER_WIDTH           = GL_RENDERBUFFER_WIDTH;
        RENDERBUFFER_HEIGHT          = GL_RENDERBUFFER_HEIGHT;
        RENDERBUFFER_RED_SIZE        = GL_RENDERBUFFER_RED_SIZE;
        RENDERBUFFER_GREEN_SIZE      = GL_RENDERBUFFER_GREEN_SIZE;
        RENDERBUFFER_BLUE_SIZE       = GL_RENDERBUFFER_BLUE_SIZE;
        RENDERBUFFER_ALPHA_SIZE      = GL_RENDERBUFFER_ALPHA_SIZE;
        RENDERBUFFER_DEPTH_SIZE      = GL_RENDERBUFFER_DEPTH_SIZE;
        RENDERBUFFER_STENCIL_SIZE    = GL_RENDERBUFFER_STENCIL_SIZE;
        RENDERBUFFER_INTERNAL_FORMAT = GL_RENDERBUFFER_INTERNAL_FORMAT;

        MULTISAMPLE    = GL_MULTISAMPLE;
        LINE_SMOOTH    = GL_LINE_SMOOTH;
        POLYGON_SMOOTH = GL_POLYGON_SMOOTH;
    }

    ///////////////////////////////////////////////////////////

    // Special Functions

    @Override
    public void flush() {
        glFlush();
    }

    @Override
    public void finish() {
        glFinish();
    }

    @Override
    public void hint(int target, int hint) {
        glHint(target, hint);
    }

    ///////////////////////////////////////////////////////////

    // State and State Requests

    @Override
    public void enable(int value) {
        if (-1 < value) {
            glEnable(value);
        }
    }

    @Override
    public void disable(int value) {
        if (-1 < value) {
            glDisable(value);
        }
    }

    @Override
    public void getBooleanv(int value, IntBuffer data) {
        if (-1 < value) {
            if (byteBuffer.capacity() < data.capacity()) {
                byteBuffer = allocateDirectByteBuffer(data.capacity());
            }
            glGetBooleanv(value, byteBuffer);
            for (int i = 0; i < data.capacity(); i++) {
                data.put(i, byteBuffer.get(i));
            }
        } else {
            fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getIntegerv(int value, IntBuffer data) {
        if (-1 < value) {
            glGetIntegerv(value, data);
        } else {
            fillIntBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public void getFloatv(int value, FloatBuffer data) {
        if (-1 < value) {
            glGetFloatv(value, data);
        } else {
            fillFloatBuffer(data, 0, data.capacity() - 1, 0);
        }
    }

    @Override
    public boolean isEnabled(int value) {
        return glIsEnabled(value);
    }

    @Override
    public String getString(int name) {
        return glGetString(name);
    }

    ///////////////////////////////////////////////////////////

    // Error Handling

    @Override
    public int getError() {
        return glGetError();
    }

    @Override
    public String errorString(int err) {
        return "";
    }

    //////////////////////////////////////////////////////////////////////////////

    // Buffer Objects

    @Override
    public void genBuffers(int n, IntBuffer buffers) {
        //System.out.printf("genBuffers()\n");
        glGenBuffers(buffers);
    }

    @Override
    public void deleteBuffers(int n, IntBuffer buffers) {
        glDeleteBuffers(buffers);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        //System.out.printf("bindBuffer(target = %d, buffer = %d)\n", target, buffer);
        glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, int size, Buffer data, int usage) {
        //System.out.printf("bufferData(target = %d, size = %d, data = %s, usage = %d)\n", target, size, data, usage);
        if (data instanceof FloatBuffer) {
            glBufferData(target, (FloatBuffer)data, usage);
        } else if (data instanceof IntBuffer) {
            glBufferData(target, (IntBuffer)data, usage);
        } else if (data instanceof ShortBuffer) {
            glBufferData(target, (ShortBuffer)data, usage);
        } else if (data instanceof ByteBuffer) {
            glBufferData(target, (ByteBuffer)data, usage);
        } else if (data instanceof DoubleBuffer) {
            glBufferData(target, (DoubleBuffer)data, usage);
        } else {
            glBufferData(target, size, usage);
        }
    }

    @Override
    public void bufferSubData(int target, int offset, int size, Buffer data) {
        //System.out.printf("bufferSubData(target = %d, offset = %d, size = %d, data = %s)\n", target, offset, size, data);
        if (data instanceof FloatBuffer) {
            glBufferSubData(target, offset, (FloatBuffer) data);
        } else if (data instanceof IntBuffer) {
            glBufferSubData(target, offset, (IntBuffer) data);
        } else if (data instanceof ShortBuffer) {
            glBufferSubData(target, offset, (ShortBuffer) data);
        } else if (data instanceof ByteBuffer) {
            glBufferSubData(target, offset, (ByteBuffer) data);
        } else if (data instanceof DoubleBuffer) {
            glBufferSubData(target, offset, (DoubleBuffer) data);
        }
    }

    @Override
    public void isBuffer(int buffer) {
        glIsBuffer(buffer);
    }

    @Override
    public void getBufferParameteriv(int target, int value, IntBuffer data) {
        //System.out.printf("getBufferParameteriv(target = %d, value = %d, data = %s)\n", target, value, data);
        glGetBufferParameteriv(target, value, data);
    }

    @Override
    public ByteBuffer mapBuffer(int target, int access) {
        //System.out.printf("mapData(target = %d, access = %d)\n", target, access);
        return glMapBuffer(target, access);
    }

    @Override
    public ByteBuffer mapBufferRange(int target, int offset, int length, int access) {
        //System.out.printf("mapData(target = %d, offset = %d, length = %d, access = %d)\n", target, offset, length, access);
        return glMapBufferRange(target, offset, length, access);

    }

    @Override
    public void unmapBuffer(int target) {
        //System.out.printf("unmapBuffer(target = %d)\n", target);
        glUnmapBuffer(target);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Viewport and Clipping

    @Override
    public void depthRangef(float n, float f) {
        glDepthRange(n, f);
    }

    @Override
    public void viewport(int x, int y, int w, int h) {
        float scale = getPixelScale();
        viewportImpl((int)scale * x, (int)(scale * y), (int)(scale * w), (int)(scale * h));
    }

    @Override
    protected void viewportImpl(int x, int y, int w, int h) {
        glViewport(x, y, w, h);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Reading Pixels

    @Override
    protected void readPixelsImpl(int x, int y, int width, int height, int format, int type, ByteBuffer buffer) {
        glReadPixels(x, y, width, height, format, type, buffer);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Vertices

    @Override
    public void vertexAttrib1f(int index, float value) {
        glVertexAttrib1f(index, value);
    }

    @Override
    public void vertexAttrib2f(int index, float value0, float value1) {
        glVertexAttrib2f(index, value0, value1);
    }

    @Override
    public void vertexAttrib3f(int index, float value0, float value1, float value2) {
        glVertexAttrib3f(index, value0, value1, value2);
    }

    @Override
    public void vertexAttrib4f(int index, float value0, float value1, float value2, float value3) {
        glVertexAttrib4f(index, value0, value1, value2, value3);
    }

    @Override
    public void vertexAttrib1fv(int index, FloatBuffer values) {
        glVertexAttrib1fv(index, values);
    }

    @Override
    public void vertexAttrib2fv(int index, FloatBuffer values) {
        glVertexAttrib2fv(index, values);
    }

    @Override
    public void vertexAttrib3fv(int index, FloatBuffer values) {
        glVertexAttrib3fv(index, values);
    }

    @Override
    public void vertexAttrib4fv(int index, FloatBuffer values) {
        glVertexAttrib4fv(index, values);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        //System.out.printf("vertexAttribPointer(index = %d, size = %d, type = %d, normalised = %b, stride = %d, offset = %d)\n", index, size, type, normalized, stride, offset);
        glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void enableVertexAttribArray(int index) {
        //System.out.printf("enableVertexAttribArray(index = %d)\n", index);
        glEnableVertexAttribArray(index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        //System.out.printf("disableVertexAttribArray(index = %d)\n", index);
        glDisableVertexAttribArray(index);
    }

    @Override
    public void drawArraysImpl(int mode, int first, int count) {
        //System.out.printf("drawArrays(mode = %d, first = %d, count = %d)\n", mode, first, count);
        glDrawArrays(mode, first, count);
    }

    @Override
    public void drawElementsImpl(int mode, int count, int type, int offset) {
        //System.out.printf("drawElements(mode = %d, count = %d, type = %d, offset = %d)\n", mode, count, type, offset);
        glDrawElements(mode, count, type, offset);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Rasterization

    @Override
    public void lineWidth(float width) {
        glLineWidth(width);
    }

    @Override
    public void frontFace(int dir) {
        glFrontFace(dir);
    }

    @Override
    public void cullFace(int mode) {
        glCullFace(mode);
    }

    @Override
    public void polygonOffset(float factor, float units) {
        glPolygonOffset(factor, units);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Pixel Rectangles

    @Override
    public void pixelStorei(int pname, int param) {
        glPixelStorei(pname, param);
    }

    ///////////////////////////////////////////////////////////

    // Texturing

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer data) {
        glTexImage2D(target, level, internalFormat, width, height, border, format, type, data);
    }

    @Override
    public void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
    }

    @Override
    public void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, IntBuffer data) {
        glTexSubImage2D(target, level, xOffset, yOffset, width, height, format, type, data);
    }

    @Override
    public void copyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        glCopyTexSubImage2D(target, level, x, y, xOffset, yOffset, width, height);
    }

    @Override
    public void compressedTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int imageSize, ByteBuffer data) {
        glCompressedTexImage2D(target, level, internalFormat, width, height, border, imageSize, data);
    }

    @Override
    public void compressedTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int imageSize, ByteBuffer data) {
        glCompressedTexSubImage2D(target, level, xOffset, yOffset, width, height, format, imageSize, data);
    }

    @Override
    public void texParameteri(int target, int pname, int param) {
        glTexParameteri(target, pname, param);
    }

    @Override
    public void texParameterf(int target, int pname, float param) {
        glTexParameterf(target, pname, param);
    }

    @Override
    public void texParameteriv(int target, int pname, IntBuffer params) {
        glTexParameteriv(target, pname, params);
    }

    @Override
    public void texParameterfv(int target, int pname, FloatBuffer params) {
        glTexParameterfv(target, pname, params);
    }

    @Override
    public void generateMipmap(int target) {
        glGenerateMipmap(target);
    }

    @Override
    public void genTextures(int n, IntBuffer textures) {
        glGenTextures(textures);
    }

    @Override
    public void deleteTextures(int n, IntBuffer textures) {
        glDeleteTextures(textures);
    }

    @Override
    public void getTexParameteriv(int target, int pname, IntBuffer params) {
        glGetTexParameteriv(target, pname, params);
    }

    @Override
    public void getTexParameterfv(int target, int pname, FloatBuffer params) {
        glGetTexParameterfv(target, pname, params);
    }

    @Override
    public boolean isTexture(int texture) {
        return glIsTexture(texture);
    }

    @Override
    protected void activeTextureImpl(int texture) {
        glActiveTexture(texture);
    }

    @Override
    protected void bindTextureImpl(int target, int texture) {
        glBindTexture(target, texture);
    }

    ///////////////////////////////////////////////////////////

    // Shaders and Programs

    @Override
    public int createShader(int type) {
        return glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        glShaderSource(shader, source);
    }

    @Override
    public void compileShader(int shader) {
        glCompileShader(shader);
    }


    @Override
    public void deleteShader(int shader) {
        glDeleteShader(shader);
    }

    @Override
    public int createProgram() {
        return glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        glAttachShader(program, shader);
    }

    @Override
    public void detachShader(int program, int shader) {
        glDetachShader(program, shader);
    }

    @Override
    public void linkProgram(int program) {
        glLinkProgram(program);
    }

    @Override
    public void useProgram(int program) {
        glUseProgram(program);
    }

    @Override
    public void deleteProgram(int program) {
        glDeleteProgram(program);
    }

    @Override
    public String getActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        return glGetActiveAttrib(program, index, size, type);
    }

    @Override
    public int getAttribLocation(int program, String name) {
        return glGetAttribLocation(program, name);
    }

    @Override
    public void bindAttribLocation(int program, int index, String name) {
        glBindAttribLocation(program, index, name);
    }

    @Override
    public int getUniformLocation(int program, String name) {
        return glGetUniformLocation(program, name);
    }

    @Override
    public String getActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        return glGetActiveUniform(program, index, size, type);
    }

    @Override
    public void uniform1i(int location, int value) {
        glUniform1i(location, value);
    }

    @Override
    public void uniform2i(int location, int value0, int value1) {
        glUniform2i(location, value0, value1);
    }

    @Override
    public void uniform3i(int location, int value0, int value1, int value2) {
        glUniform3i(location, value0, value1, value2);
    }

    @Override
    public void uniform4i(int location, int value0, int value1, int value2, int value3) {
        glUniform4i(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1f(int location, float value) {
        glUniform1f(location, value);
    }

    @Override
    public void uniform2f(int location, float value0, float value1) {
        glUniform2f(location, value0, value1);
    }

    @Override
    public void uniform3f(int location, float value0, float value1, float value2) {
        glUniform3f(location, value0, value1, value2);
    }

    @Override
    public void uniform4f(int location, float value0, float value1, float value2, float value3) {
        glUniform4f(location, value0, value1, value2, value3);
    }

    @Override
    public void uniform1iv(int location, int count, IntBuffer v) {
        glUniform1iv(location, v);
    }

    @Override
    public void uniform2iv(int location, int count, IntBuffer v) {
        glUniform2iv(location, v);
    }

    @Override
    public void uniform3iv(int location, int count, IntBuffer v) {
        glUniform3iv(location, v);
    }

    @Override
    public void uniform4iv(int location, int count, IntBuffer v) {
        glUniform4iv(location, v);
    }

    @Override
    public void uniform1fv(int location, int count, FloatBuffer v) {
        glUniform1fv(location, v);
    }

    @Override
    public void uniform2fv(int location, int count, FloatBuffer v) {
        glUniform2fv(location, v);
    }

    @Override
    public void uniform3fv(int location, int count, FloatBuffer v) {
        glUniform3fv(location, v);
    }

    @Override
    public void uniform4fv(int location, int count, FloatBuffer v) {
        glUniform4fv(location, v);
    }

    @Override
    public void uniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer mat) {
        glUniformMatrix2fv(location, transpose, mat);
    }

    @Override
    public void uniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer mat) {
        glUniformMatrix3fv(location, transpose, mat);
    }

    @Override
    public void uniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer mat) {
        glUniformMatrix4fv(location, transpose, mat);
    }

    @Override
    public void validateProgram(int program) {
        glValidateProgram(program);
    }

    @Override
    public boolean isShader(int shader) {
        return glIsShader(shader);
    }

    @Override
    public void getShaderiv(int shader, int pname, IntBuffer params) {
        glGetShaderiv(shader, pname, params);
    }

    @Override
    public void getAttachedShaders(int program, int maxCount, IntBuffer count, IntBuffer shaders) {
        glGetAttachedShaders(program, count, shaders);
    }

    @Override
    public String getShaderInfoLog(int shader) {
        return glGetShaderInfoLog(shader);
    }

    @Override
    public String getShaderSource(int shader) {
        return glGetShaderSource(shader);
    }

    @Override
    public void getVertexAttribfv(int index, int pname, FloatBuffer params) {
        glGetVertexAttribfv(index, pname, params);
    }

    @Override
    public void getVertexAttribiv(int index, int pname, IntBuffer params) {
        glGetVertexAttribiv(index, pname, params);
    }

    @Override
    public void getVertexAttribPointerv(int index, int pname, ByteBuffer data) {
        throw new RuntimeException(String.format(MISSING_GLFUNC_ERROR, "glGetVertexAttribPointerv()"));
    }

    @Override
    public void getUniformfv(int program, int location, FloatBuffer params) {
        glGetUniformfv(program, location, params);
    }

    @Override
    public void getUniformiv(int program, int location, IntBuffer params) {
        glGetUniformiv(program, location, params);
    }

    @Override
    public boolean isProgram(int program) {
        return glIsProgram(program);
    }

    @Override
    public void getProgramiv(int program, int pname, IntBuffer params) {
        glGetProgramiv(program, pname, params);
    }

    @Override
    public String getProgramInfoLog(int program) {
        return glGetProgramInfoLog(program);
    }

    ///////////////////////////////////////////////////////////

    // Per-Fragment Operations

    @Override
    public void scissor(int x, int y, int w, int h) {
        float scale = getPixelScale();
        glScissor((int)scale * x, (int)(scale * y), (int)(scale * w), (int)(scale * h));
//    glScissor(x, y, w, h);
    }

    @Override
    public void sampleCoverage(float value, boolean invert) {
        glSampleCoverage(value, invert);
    }

    @Override
    public void stencilFunc(int func, int ref, int mask) {
        glStencilFunc(func, ref, mask);
    }

    @Override
    public void stencilFuncSeparate(int face, int func, int ref, int mask) {
        glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void stencilOp(int sfail, int dpfail, int dppass) {
        glStencilOp(sfail, dpfail, dppass);
    }

    @Override
    public void stencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    @Override
    public void depthFunc(int func) {
        glDepthFunc(func);
    }

    @Override
    public void blendEquation(int mode) {
        glBlendEquation(mode);
    }

    @Override
    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void blendFunc(int src, int dst) {
        glBlendFunc(src, dst);
    }

    @Override
    public void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void blendColor(float red, float green, float blue, float alpha) {
        glBlendColor(red, green, blue, alpha);
    }

    ///////////////////////////////////////////////////////////

    // Whole Framebuffer Operations

    @Override
    public void colorMask(boolean r, boolean g, boolean b, boolean a) {
        glColorMask(r, g, b, a);
    }

    @Override
    public void depthMask(boolean mask) {
        glDepthMask(mask);
    }

    @Override
    public void stencilMask(int mask) {
        glStencilMask(mask);
    }

    @Override
    public void stencilMaskSeparate(int face, int mask) {
        glStencilMaskSeparate(face, mask);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    @Override
    public void clearDepth(float d) {
        glClearDepth(d);
    }

    @Override
    public void clearStencil(int s) {
        glClearStencil(s);
    }

    @Override
    public void clear(int buf) {
        glClear(buf);
    }

    ///////////////////////////////////////////////////////////

    // Framebuffers Objects

    @Override
    protected void bindFramebufferImpl(int target, int framebuffer) {
        glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void deleteFramebuffers(int n, IntBuffer framebuffers) {
        glDeleteFramebuffers(framebuffers);
    }

    @Override
    public void genFramebuffers(int n, IntBuffer framebuffers) {
        glGenFramebuffers(framebuffers);
    }

    @Override
    public void bindRenderbuffer(int target, int renderbuffer) {
        glBindRenderbuffer(target, renderbuffer);
    }

    @Override
    public void deleteRenderbuffers(int n, IntBuffer renderbuffers) {
        glDeleteRenderbuffers(renderbuffers);
    }

    @Override
    public void genRenderbuffers(int n, IntBuffer renderbuffers) {
        glGenRenderbuffers(renderbuffers);
    }

    @Override
    public void renderbufferStorage(int target, int internalFormat, int width, int height) {
        glRenderbufferStorage(target, internalFormat, width, height);
    }

    @Override
    public void framebufferRenderbuffer(int target, int attachment, int rendbuferfTarget, int renderbuffer) {
        glFramebufferRenderbuffer(target, attachment, rendbuferfTarget, renderbuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int texTarget, int texture, int level) {
        glFramebufferTexture2D(target, attachment, texTarget, texture, level);
    }

    @Override
    public int checkFramebufferStatus(int target) {
        return glCheckFramebufferStatus(target);
    }

    @Override
    public boolean isFramebuffer(int framebuffer) {
        return glIsFramebuffer(framebuffer);
    }

    @Override
    public void getFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    @Override
    public boolean isRenderbuffer(int renderbuffer) {
        return glIsRenderbuffer(renderbuffer);
    }

    @Override
    public void getRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        glGetRenderbufferParameteriv(target, pname, params);
    }

    @Override
    public void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public void renderbufferStorageMultisample(int target, int samples, int format, int width, int height) {
        glRenderbufferStorageMultisample(target, samples, format, width, height);
    }

    @Override
    public void readBuffer(int buf) {
        glReadBuffer(buf);
    }

    @Override
    public void drawBuffer(int buf) {
        glDrawBuffer(buf);
    }
}
