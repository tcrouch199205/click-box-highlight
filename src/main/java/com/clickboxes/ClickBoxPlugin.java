package com.clickboxes;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Click Boxes",
		description = "Permanently outlines the clickable area of inventory items, prayers and spells",
		tags = {"inventory", "prayer", "spell", "click", "outline", "accessibility"}
)
public class ClickBoxPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClickBoxOverlay overlay;

	@Override
	protected void startUp()
	{
		overlay.refresh();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("clickboxes".equals(event.getGroup()))
		{
			overlay.refresh();
		}
	}

	@Provides
	ClickBoxConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClickBoxConfig.class);
	}
}
