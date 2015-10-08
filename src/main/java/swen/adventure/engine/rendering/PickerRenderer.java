package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.PickerShader;
import swen.adventure.engine.scenegraph.CameraNode;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 1/10/15.
 */
public class PickerRenderer {

    private PickerShader _pickerShader;
    private int _width, _height;
    private float _currentFOV = 0.0001f; //Set an extremely narrow field of view, since we're basically only interested in the centre pixel.

    private WeakReference<MeshNode>[] _idsToNodes = (WeakReference<MeshNode>[]) new WeakReference[0xFFFFFF];

    private int _frameBufferObject, _colourRenderBuffer, _depthStencilBuffer;

    private MeshNode _highlightedMesh = null;

    public PickerRenderer() {
        _pickerShader = new PickerShader();

        this.initGL();
    }

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
    }

    private void deinitGL() {
        glDeleteFramebuffers(_frameBufferObject);
        glDeleteRenderbuffers(_colourRenderBuffer);
        glDeleteRenderbuffers(_depthStencilBuffer);
    }

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

        ByteBuffer idBuffer = BufferUtils.createByteBuffer(4);
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        glReadPixels(0, 0, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, idBuffer);

        int id = PickerShader.colourToID(idBuffer.get(), idBuffer.get(), idBuffer.get());
        _highlightedMesh = _idsToNodes[id] != null ? _idsToNodes[id].get() : null;

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Optional<MeshNode> selectedNode(List<MeshNode> meshNodes, Matrix4 worldToCameraMatrix) {
        this.render(meshNodes, worldToCameraMatrix);
        return Optional.ofNullable(_highlightedMesh);
    }

    private void render(List<MeshNode> meshNodes, Matrix4 worldToCameraMatrix) {
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

            node.render(ObjMesh.VAOPositions);
        });

        _pickerShader.endUseProgram();

        this.postRender();
    }
}
