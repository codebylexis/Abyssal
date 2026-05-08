package components;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import core.Texture;

/**
 * Slices a texture atlas into individual Sprite objects using a uniform grid.
 * Sprites are ordered left-to-right, top-to-bottom. UV coordinates are
 * computed in normalized [0,1] space at construction time.
 */
public class Spritesheet {

	private Texture texture;
	private List<Sprite> sprites;
	
	public Spritesheet(Texture texture, int spriteWidth, int spriteHeight, int numSprites, int spacing) {
		this.sprites = new ArrayList<Sprite>();
		
		this.texture = texture;
		int currentX = 0;
		int currentY = texture.getHeight() - spriteHeight;
		for(int i = 0; i < numSprites; i++) {
			float topY = (currentY + spriteHeight) / (float)texture.getHeight();
			float rightX = (currentX + spriteWidth) / (float)texture.getWidth();
			float leftX = currentX / (float)texture.getWidth();
			float bottomY = currentY / (float)texture.getHeight();
			
			Vector2f[] texCoords = {
					new Vector2f(rightX, topY),
					new Vector2f(rightX, bottomY),
					new Vector2f(leftX, bottomY),
					new Vector2f(leftX, topY),	
			};
			
			Sprite sprite = new Sprite();
			sprite.setTexture(this.texture);
			sprite.setTexCoords(texCoords);
			sprite.setWidth(spriteWidth);
			sprite.setHeight(spriteHeight);
			this.sprites.add(sprite);
			
			currentX += spriteWidth + spacing;
			if(currentX >= texture.getWidth()) {
				currentX = 0;
				currentY -= spriteHeight + spacing;
			}
		}
	}
	
	public Sprite getSprite(int index) {
		return this.sprites.get(index);
	}
	
	public int size() {
		return sprites.size();
	}
}
