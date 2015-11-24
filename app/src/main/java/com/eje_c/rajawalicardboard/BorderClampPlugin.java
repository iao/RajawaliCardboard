package com.eje_c.rajawalicardboard;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

/**
 * Created by iao on 13/11/15.
 */
public class BorderClampPlugin implements IMaterialPlugin {
    private IShaderFragment mFragmentShader;

    public BorderClampPlugin() {
        mFragmentShader = new BorderClampShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.POST_TRANSFORM;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return null;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return mFragmentShader;
    }

    @Override
    public void bindTextures(int nextIndex) {

    }

    @Override
    public void unbindTextures() {

    }

    private class BorderClampShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "BORDER_CLAMP_FRAGMENT_SHADER_FRAGMENT";

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {

        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);

            startif(new Condition(textureCoord.t(), Operator.LESS_THAN, 0), new Condition(Operator.OR, textureCoord.t(), Operator.GREATER_THAN, 1),
                    new Condition(Operator.OR, textureCoord.s(), Operator.LESS_THAN, 0), new Condition(Operator.OR, textureCoord.s(), Operator.GREATER_THAN, 1));
            {
                discard();
            }
            endif();
        }
    }
}
