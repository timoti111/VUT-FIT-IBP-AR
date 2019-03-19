package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Route {
    public Route() {
    }

    static public ModelInstance createRoute(Vector3 v1, Vector3 v2) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("line", 1, 3, new Material());
        builder.setColor(Color.RED);
        builder.line(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
        Model lineModel = modelBuilder.end();
        return new ModelInstance(lineModel);
    }
}
