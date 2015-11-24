package com.eje_c.rajawalicardboard;

import org.rajawali3d.materials.shaders.fragments.texture.DiffuseTextureFragmentShaderFragment;
import org.rajawali3d.materials.textures.ATexture;

import java.util.List;

/**
 * Created by iao on 16/11/15.
 */
public class TiledShader extends DiffuseTextureFragmentShaderFragment {
    TiledShader(List<ATexture> textures) {
        super(textures);
    }

    @Override
    public void main() {
        RVec4 color = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
        RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
        RVec4 texColor = new RVec4("texColor");
        RVec2 texCoord = new RVec2("texCoord", textureCoord);
        texCoord.s().assignMultiply(2.0f);

        /*for(int i=0; i<mTextures.size(); i++)
        {
            ATexture texture = mTextures.get(i);
            if(texture.offsetEnabled())
                textureCoord.assignAdd(getGlobal(DefaultShaderVar.U_OFFSET, i));
            if(texture.getWrapType() == ATexture.WrapType.REPEAT)
                textureCoord.assignMultiply(getGlobal(DefaultShaderVar.U_REPEAT, i));

            if(texture.getTextureType() == ATexture.TextureType.VIDEO_TEXTURE)
                texColor.assign(texture2D(muVideoTextures[i], texCoord));
            else
                texColor.assign(texture2D(muTextures[i], texCoord));
            texColor.assignMultiply(muInfluence[i]);
            color.assignAdd(texColor);
        }*/
        startif(new Condition(texCoord.s(), Operator.LESS_THAN_EQUALS, 1));
        color.assign(texture2D(muTextures[0], texCoord));
        color.assignMultiply(muInfluence[0]);
        ifelse();
        texCoord.s().assignSubtract(1.0f);
        color.assign(texture2D(muTextures[1], texCoord));
        color.assignMultiply(muInfluence[1]);
        endif();
        //color.assignAdd(texColor);

    }
}
