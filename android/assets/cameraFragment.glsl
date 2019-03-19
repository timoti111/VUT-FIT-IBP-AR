#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES diffuseTex;
varying vec2 v_texCoord;

void main() {
    gl_FragColor  = texture2D(diffuseTex, v_texCoord);
}