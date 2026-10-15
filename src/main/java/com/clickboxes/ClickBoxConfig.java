package com.clickboxes;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("clickboxes")
public interface ClickBoxConfig extends Config
{
	enum BoxShape
	{
		ELLIPSE,
		RECTANGLE
	}

	@ConfigItem(keyName = "showInventory", name = "Inventory", description = "Outline inventory items", position = 1)
	default boolean showInventory()
	{
		return true;
	}

	@ConfigItem(keyName = "showPrayers", name = "Prayers", description = "Outline prayers", position = 2)
	default boolean showPrayers()
	{
		return true;
	}

	@ConfigItem(keyName = "showSpells", name = "Spells", description = "Outline spellbook spells", position = 3)
	default boolean showSpells()
	{
		return true;
	}

	@ConfigItem(keyName = "shape", name = "Prayer/spell shape", description = "Shape drawn for prayers and spells. Inventory items are always rectangles.", position = 4)
	default BoxShape shape()
	{
		return BoxShape.ELLIPSE;
	}

	@Alpha
	@ConfigItem(keyName = "color", name = "Color", description = "Outline color", position = 5)
	default Color color()
	{
		return new Color(255, 255, 255, 140);
	}

	@Alpha
	@ConfigItem(keyName = "fillColor", name = "Fill color", description = "Background fill color. Set opacity to 0 for no fill.", position = 6)
	default Color fillColor()
	{
		return new Color(255, 255, 255, 40);
	}

	@Range(min = 1, max = 5)
	@ConfigItem(keyName = "strokeWidth", name = "Line width", description = "Outline thickness", position = 7)
	default int strokeWidth()
	{
		return 1;
	}
}
