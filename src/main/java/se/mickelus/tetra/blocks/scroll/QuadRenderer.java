package se.mickelus.tetra.blocks.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class QuadRenderer {
    public final Vertex[] vertexPositions;
    public final Vector3f normal;

    public QuadRenderer(float x, float y, float z, float w, float h, float u, float v, float texWidth, float texHeight, boolean mirror, Direction direction) {
        float u1 = u / texWidth;
        float u2 = (u + w) / texWidth;
        float v1 = v / texHeight;
        float v2 = (v + h) / texHeight;

        if (mirror) {
            float temp = u1;
            u1 = u2;
            u2 = temp;
        }


        switch (direction) {
            default:
            case DOWN:
                vertexPositions = new Vertex[] {
                        new Vertex(x + 0, y + 0, z + 0, u1, v1),
                        new Vertex(x + w, y + 0, z + 0, u2, v1),
                        new Vertex(x + w, y + 0, z + h, u2, v2),
                        new Vertex(x + 0, y + 0, z + h, u1, v2)
                };
                break;
            case UP:
                vertexPositions = new Vertex[] {
                        new Vertex(x + w, y + 0, z + 0, u1, v1),
                        new Vertex(x + 0, y + 0, z + 0, u2, v1),
                        new Vertex(x + 0, y + 0, z + h, u2, v2),
                        new Vertex(x + w, y + 0, z + h, u1, v2)
                };
                break;
            case WEST:
                vertexPositions = new Vertex[] {
                        new Vertex(x + 0, y + 0, z + 0, u1, v1),
                        new Vertex(x + 0, y + 0, z + w, u2, v1),
                        new Vertex(x + 0, y + h, z + w, u2, v2),
                        new Vertex(x + 0, y + h, z + 0, u1, v2)
                };
                break;
            case NORTH:
                vertexPositions = new Vertex[] {
                        new Vertex(x + w, y + 0, z + 0, u1, v1),
                        new Vertex(x + 0, y + 0, z + 0, u2, v1),
                        new Vertex(x + 0, y + h, z + 0, u2, v2),
                        new Vertex(x + w, y + h, z + 0, u1, v2)
                };
                break;
            case EAST:
                vertexPositions = new Vertex[] {
                        new Vertex(x + 0, y + 0, z + w, u1, v1),
                        new Vertex(x + 0, y + 0, z + 0, u2, v1),
                        new Vertex(x + 0, y + h, z + 0, u2, v2),
                        new Vertex(x + 0, y + h, z + w, u1, v2)
                };
                break;
            case SOUTH:
                vertexPositions = new Vertex[] {
                        new Vertex(x + 0, y + 0, z + 0, u1, v1),
                        new Vertex(x + w, y + 0, z + 0, u2, v1),
                        new Vertex(x + w, y + h, z + 0, u2, v2),
                        new Vertex(x + 0, y + h, z + 0, u1, v2)
                };
                break;
        }

        this.normal = direction.toVector3f();
        if (mirror) {
            this.normal.mul(-1.0F, 1.0F, 1.0F);
        }

    }

    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.push();
        MatrixStack.Entry last = matrixStack.getLast();
        Matrix4f matrix = last.getMatrix();
        Matrix3f normal = last.getNormal();

        Vector3f vector3f = this.normal.copy();
        vector3f.transform(normal);
        float originX = vector3f.getX();
        float originY = vector3f.getY();
        float originZ = vector3f.getZ();

        for (Vertex vertex : vertexPositions) {
            Vector4f pos = new Vector4f(vertex.pos.getX() / 16.0F, vertex.pos.getY() / 16.0F, vertex.pos.getZ() / 16.0F, 1.0F);
            pos.transform(matrix);
            buffer.addVertex(pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha, vertex.u, vertex.v, packedOverlay,
                    packedLight, originX, originY, originZ);
        }
        matrixStack.pop();
    }

    static class Vertex {
        final Vector3f pos;
        final float u;
        final float v;

        public Vertex(float x, float y, float z, float texU, float texV) {
            this(new Vector3f(x, y, z), texU, texV);
        }

        public Vertex(Vector3f pos, float u, float v) {
            this.pos = pos;
            this.u = u;
            this.v = v;
        }
    }
}
