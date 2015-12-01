package com.eje_c.rajawalicardboard;

import org.rajawali3d.materials.shaders.fragments.texture.DiffuseTextureFragmentShaderFragment;
import org.rajawali3d.materials.textures.ATexture;

import java.util.List;

/**
 * Created by iao on 16/11/15.
 */
public class TiledShader extends DiffuseTextureFragmentShaderFragment {
    private int width;
    private int height;


    TiledShader(List<ATexture> textures, int width, int height) {
        super(textures);
        this.width = width;
        this.height = height;
    }

    @Override
    public void main() {
        RVec4 color = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
        RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
        RVec4 texColor = new RVec4("texColor");
        RVec2 texCoord = new RVec2("texCoord", textureCoord);
        if(width != 1)
            texCoord.s().assignMultiply(width);
        if(height != 1)
            texCoord.t().assignMultiply(height);

        boolean first = true;
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                int index = i + j * width;
                int widthNext = i + 1;
                int heightNext = j + 1;

                if(first) {
                    if(widthNext < width && heightNext < height) {
                        startif(new Condition(texCoord.s(), Operator.LESS_THAN_EQUALS, 1), new Condition(Operator.AND, texCoord.t(), Operator.LESS_THAN_EQUALS, 1));
                    } else if(widthNext < width) {
                        startif(new Condition(texCoord.s(), Operator.LESS_THAN_EQUALS, 1));
                    } else if(heightNext < height) {
                        startif(new Condition(texCoord.t(), Operator.LESS_THAN_EQUALS, 1));
                    } else {

                    }
                } else {
                    if(widthNext < width && heightNext < height) {
                        ifelseif(new Condition(texCoord.s(), Operator.LESS_THAN_EQUALS, widthNext), new Condition(Operator.AND, texCoord.t(), Operator.LESS_THAN_EQUALS, heightNext));
                    } else if(widthNext < width) {
                        ifelseif(new Condition(texCoord.s(), Operator.LESS_THAN_EQUALS, widthNext));
                    } else if(heightNext < height) {
                        ifelseif(new Condition(texCoord.t(), Operator.LESS_THAN_EQUALS, heightNext));
                    } else {
                        ifelse();
                    }
                }
                if(i != 0)
                    texCoord.s().assignSubtract(i);
                if(j != 0)
                    texCoord.t().assignSubtract(j);
                color.assign(texture2D(muTextures[index], texCoord));
                color.assignMultiply(muInfluence[index]);
                first = false;
            }
        }

        if(width != 1 || height != 1)
            endif();

    }
}
