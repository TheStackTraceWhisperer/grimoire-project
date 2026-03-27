package com.grimoire.clientv2.render;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Camera class for managing view and projection matrices.
 * Supports both orthographic (2D) and perspective (3D) projections.
 */
public class Camera {
    
    @Getter
    private final Vector3f position;
    
    @Getter
    private final Vector3f rotation;
    
    @Getter
    @Setter
    private float zoom = 1.0f;
    
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewProjectionMatrix;
    
    private boolean viewDirty = true;
    private boolean projectionDirty = true;
    
    // Projection settings
    private float fov = (float) Math.toRadians(60.0f);
    private float aspectRatio = 16.0f / 9.0f;
    private float nearPlane = 0.01f;
    private float farPlane = 1000.0f;
    
    // Orthographic settings
    private boolean orthographic = false;
    private float orthoWidth = 800.0f;
    private float orthoHeight = 600.0f;
    
    /**
     * Creates a new camera at the origin.
     */
    public Camera() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.viewProjectionMatrix = new Matrix4f();
    }
    
    /**
     * Creates a camera at the specified position.
     */
    public Camera(float x, float y, float z) {
        this();
        position.set(x, y, z);
    }
    
    /**
     * Sets up a perspective projection.
     *
     * @param fov         Field of view in radians
     * @param aspectRatio Aspect ratio (width/height)
     * @param nearPlane   Near clipping plane
     * @param farPlane    Far clipping plane
     */
    public void setPerspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.orthographic = false;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.projectionDirty = true;
    }
    
    /**
     * Sets up an orthographic projection.
     *
     * @param width  View width
     * @param height View height
     */
    public void setOrthographic(float width, float height) {
        this.orthographic = true;
        this.orthoWidth = width;
        this.orthoHeight = height;
        this.projectionDirty = true;
    }
    
    /**
     * Updates the aspect ratio (for window resize).
     */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        if (!orthographic) {
            this.projectionDirty = true;
        }
    }
    
    /**
     * Sets the camera position.
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        viewDirty = true;
    }
    
    /**
     * Moves the camera by the specified offset.
     */
    public void move(float dx, float dy, float dz) {
        position.add(dx, dy, dz);
        viewDirty = true;
    }
    
    /**
     * Sets the camera rotation.
     *
     * @param pitch Rotation around X axis (radians)
     * @param yaw   Rotation around Y axis (radians)
     * @param roll  Rotation around Z axis (radians)
     */
    public void setRotation(float pitch, float yaw, float roll) {
        rotation.set(pitch, yaw, roll);
        viewDirty = true;
    }
    
    /**
     * Rotates the camera by the specified angles.
     */
    public void rotate(float dPitch, float dYaw, float dRoll) {
        rotation.add(dPitch, dYaw, dRoll);
        viewDirty = true;
    }
    
    /**
     * Returns the view matrix.
     */
    public Matrix4f getViewMatrix() {
        if (viewDirty) {
            updateViewMatrix();
        }
        return viewMatrix;
    }
    
    /**
     * Returns the projection matrix.
     */
    public Matrix4f getProjectionMatrix() {
        if (projectionDirty) {
            updateProjectionMatrix();
        }
        return projectionMatrix;
    }
    
    /**
     * Returns the combined view-projection matrix.
     */
    public Matrix4f getViewProjectionMatrix() {
        if (viewDirty || projectionDirty) {
            if (viewDirty) updateViewMatrix();
            if (projectionDirty) updateProjectionMatrix();
            projectionMatrix.mul(viewMatrix, viewProjectionMatrix);
        }
        return viewProjectionMatrix;
    }
    
    private void updateViewMatrix() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .translate(-position.x, -position.y, -position.z);
        viewDirty = false;
    }
    
    private void updateProjectionMatrix() {
        if (orthographic) {
            float halfWidth = (orthoWidth / 2.0f) / zoom;
            float halfHeight = (orthoHeight / 2.0f) / zoom;
            projectionMatrix.identity().ortho(-halfWidth, halfWidth, -halfHeight, halfHeight, nearPlane, farPlane);
        } else {
            projectionMatrix.identity().perspective(fov, aspectRatio, nearPlane, farPlane);
        }
        projectionDirty = false;
    }
    
    /**
     * Creates a standard orthographic camera for 2D UI rendering.
     *
     * @param width  Screen width
     * @param height Screen height
     */
    public static Camera createOrthoCamera(float width, float height) {
        Camera camera = new Camera(0, 0, 1);
        camera.setOrthographic(width, height);
        return camera;
    }
    
    /**
     * Creates a screen-space camera (origin at top-left).
     *
     * @param width  Screen width
     * @param height Screen height
     */
    public static Camera createScreenCamera(float width, float height) {
        Camera camera = new Camera(width / 2.0f, height / 2.0f, 1);
        camera.setOrthographic(width, height);
        return camera;
    }
}
