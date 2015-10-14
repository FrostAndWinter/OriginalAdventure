/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.PickerShader;
import swen.adventure.engine.scenegraph.MeshNode;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 1/10/15.
 *
 * The PickerRenderer is a specific GL renderer that renders each mesh in a different colour.
 * It then reads back the colour at a specific location and returns which MeshNode that is associated with.
 * This technique is known as 'Colour Picking'.
 */
public class PickerRenderer {

    private PickerShader _pickerShader;
    private float _currentFOV = 0.0001f; //Set an extremely narrow field of view, since we're basically only interested in the centre pixel.

    private WeakReference<MeshNode>[] _idsToNodes = (WeakReference<MeshNode>[]) new WeakReference[0xFFFFFF];

    private int _frameBufferObject, _colourRenderBuffer, _depthStencilBuffer;
    private int _pixelBufferObject;

    public PickerRenderer() {
        _pickerShader = new PickerShader();

        this.initGL();
    }

    /**
     * Initialise all the OpenGL objects – specifically, the framebuffers to use for off-screen drawing.
     */
    private void initGL() {
        _frameBufferObject = glGenFramebuffers();
        _colourRenderBuffer = glGenRenderbuffers();
        _depthStencilBuffer = glGenRenderbuffers();

        glBindRenderbuffer(GL_RENDERBUFFER, _colourRenderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, 1, 1);

        glBindRenderbuffer(GL_RENDERBUFFER, _depthStencilBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, 1, 1);

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, _frameBufferObject);
        glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, _colourRenderBuffer);
        glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, _depthStencilBuffer);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

        _pixelBufferObject = glGenBuffers();
        glBindBuffer(GL_PIXEL_PACK_BUFFER, _pixelBufferObject);
        glBufferData(GL_PIXEL_PACK_BUFFER, 4, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }

    /**
     * Remove the frame-buffers for this picker from the context.
     */
    private void deinitGL() {
        glDeleteFramebuffers(_frameBufferObject);
        glDeleteRenderbuffers(_colourRenderBuffer);
        glDeleteRenderbuffers(_depthStencilBuffer);
    }

    /**
     * Generates a perspective matrix with a 1:1 aspect ratio.
     * Uses 1 for zNear and 10000 for zFar.
     * @param fieldOfView The field of view to use
     * @return The perspective matrix with the specified field of view.
     */
    private Matrix4 perspectiveMatrix(float fieldOfView) {;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;

        return Matrix4.makePerspective(fieldOfView, 1.f, cameraNear, cameraFar);
    }

    /**
     * Setup GL state for rendering.
     */
    private void preRender() {

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);

        glBindFramebuffer(GL_FRAMEBUFFER, _frameBufferObject);

        glClearColor(0.f, 0.f, 0.f, 1.f);
        glClearDepth(1.0);
    }

    /**
     * Revert changed GL state.
     */
    private void postRender() {
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        glReadBuffer(GL_COLOR_ATTACHMENT0);

        glBindBuffer(GL_PIXEL_PACK_BUFFER, _pixelBufferObject);

        glReadPixels(0, 0, 1, 1, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    /**
     * Returns the mesh that was in the centre of the screen the last time this was rendered.
     * For best performance, this should be called some time after render() is called.
     * @return The MeshNode that is situated in the centre of the screen, if it is present.
     */
    public Optional<MeshNode> selectedNode() {
        // Map the pixel buffer object to be processed by the CPU
        glBindBuffer(GL_PIXEL_PACK_BUFFER, _pixelBufferObject);
        ByteBuffer mapBuffer = glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY);

        MeshNode highlightedMesh = null;
        if (mapBuffer != null) {
            int colour = mapBuffer.asIntBuffer().get();

            int id = PickerShader.colourToID(colour);
            highlightedMesh = _idsToNodes[id] != null ? _idsToNodes[id].get() : null;
            glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
        }

        // back to conventional pixel operations
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

        return Optional.ofNullable(highlightedMesh);
    }

    /**
     * Renders the scene to be used in colour picking.
     * @param meshNodes The meshes to render.
     * @param worldToCameraMatrix The transformation matrix to convert from world space to camera space.
     */
    public void render(List<MeshNode> meshNodes, Matrix4 worldToCameraMatrix) {
        final int[] currentNodeId = {1}; //NOTE: The maximum number of nodes is 2^24, and this will break if we go over that. But that's fine, because with that many nodes lots of other things will break first.
                                            //This also starts at 1, since the id 0 corresponds to no object being selected.
        this.preRender();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        _pickerShader.useProgram();

        meshNodes.forEach(node -> {
            Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
            Matrix4 nodeToClipSpaceTransform = this.perspectiveMatrix(_currentFOV).multiply(nodeToCameraSpaceTransform);

            int id = currentNodeId[0]++;

            _idsToNodes[id] = new WeakReference<>(node);

            _pickerShader.setModelToClipMatrix(nodeToClipSpaceTransform);
            _pickerShader.setID(id);

            node.render(GLMesh.VertexArrayObject.Positions);
        });

        _pickerShader.endUseProgram();

        this.postRender();
    }
}