package uristqwerty.CraftGuide.client.ui;

import uristqwerty.gui.components.GuiElement;
import uristqwerty.gui.minecraft.Text;

public class GuiText extends GuiElement
{
	private Text text;

	public GuiText(int x, int y, String text, int color)
	{
		super(x, y, 0, 0);
		
		this.text = new Text(0, 0, text, color);
	}

	public GuiText(int x, int y, String text)
	{
		this(x, y, text, 0xffffffff);
	}

	public void setText(String text)
	{
		this.text.setText(text);
	}
	
	@Override
	public void draw()
	{
		render(text);
		
		super.draw();
	}
}
