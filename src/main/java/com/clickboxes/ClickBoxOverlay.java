package com.clickboxes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class ClickBoxOverlay extends Overlay
{
	// Interface group IDs. Verify with the Widget Inspector if something stops drawing
	// after a game update.
	private static final int GROUP_INVENTORY = 149;
	private static final int GROUP_PRAYER = 541;
	private static final int GROUP_SPELLBOOK_STANDARD = 218;
	private static final int GROUP_SPELLBOOK_ANCIENT = 193;
	private static final int GROUP_SPELLBOOK_LUNAR = 430;
	private static final int GROUP_SPELLBOOK_ARCEUUS = 183;

	// Child ID of the scrolling spell container (MagicSpellbook.SPELLLAYER, 218:3).
	private static final int SPELL_LAYER_CHILD_STANDARD = 3;

	private final Client client;
	private final ClickBoxConfig config;

	// Config values cached so we don't hit the config proxy every frame.
	// volatile because setting changes can arrive on a different thread than rendering.
	private volatile boolean showInventory;
	private volatile boolean showPrayers;
	private volatile boolean showSpells;
	private volatile boolean useEllipse;
	private volatile Color outlineColor;
	private volatile Color fillColor;
	private volatile BasicStroke stroke;

	// Reused to avoid allocating a new shape for every widget every frame.
	private final Ellipse2D.Double ellipse = new Ellipse2D.Double();

	@Inject
	private ClickBoxOverlay(Client client, ClickBoxConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		refresh();
	}

	/** Re-reads settings. Called on startup and whenever a plugin setting changes. */
	public void refresh()
	{
		showInventory = config.showInventory();
		showPrayers = config.showPrayers();
		showSpells = config.showSpells();
		useEllipse = config.shape() == ClickBoxConfig.BoxShape.ELLIPSE;
		outlineColor = config.color();
		fillColor = config.fillColor();
		stroke = new BasicStroke(config.strokeWidth());
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}

		g.setStroke(stroke);

		if (showInventory)
		{
			draw(g, GROUP_INVENTORY, false);
		}
		if (showPrayers)
		{
			draw(g, GROUP_PRAYER, true);
		}
		if (showSpells)
		{
			drawLayer(g, GROUP_SPELLBOOK_STANDARD, SPELL_LAYER_CHILD_STANDARD, true);
			draw(g, GROUP_SPELLBOOK_ANCIENT, true);
			draw(g, GROUP_SPELLBOOK_LUNAR, true);
			draw(g, GROUP_SPELLBOOK_ARCEUUS, true);
		}
		return null;
	}

	private void draw(Graphics2D g, int group, boolean allowEllipse)
	{
		walk(g, client.getWidget(group, 0), allowEllipse);
	}

	/** Walks a scrolling container, clipped to its own area so off-screen entries don't draw. */
	private void drawLayer(Graphics2D g, int group, int child, boolean allowEllipse)
	{
		Widget layer = client.getWidget(group, child);
		if (layer == null || layer.isHidden())
		{
			return;
		}

		Shape oldClip = g.getClip();
		try
		{
			g.clip(layer.getBounds());
			walk(g, layer, allowEllipse);
		}
		finally
		{
			g.setClip(oldClip);
		}
	}

	private void walk(Graphics2D g, Widget widget, boolean allowEllipse)
	{
		// Hidden widgets (including tabs that aren't open) end the walk immediately.
		if (widget == null || widget.isHidden())
		{
			return;
		}

		if (hasAction(widget))
		{
			Rectangle b = widget.getBounds();
			if (b != null && b.width > 0 && b.height > 0)
			{
				Shape shape;
				if (allowEllipse && useEllipse)
				{
					ellipse.setFrame(b.x, b.y, b.width, b.height);
					shape = ellipse;
				}
				else
				{
					shape = b;
				}

				if (fillColor.getAlpha() > 0)
				{
					g.setColor(fillColor);
					g.fill(shape);
				}
				if (outlineColor.getAlpha() > 0)
				{
					g.setColor(outlineColor);
					g.draw(shape);
				}
			}
		}

		recurse(g, widget.getStaticChildren(), allowEllipse);
		recurse(g, widget.getDynamicChildren(), allowEllipse);
		recurse(g, widget.getNestedChildren(), allowEllipse);
	}

	private void recurse(Graphics2D g, Widget[] children, boolean allowEllipse)
	{
		if (children == null)
		{
			return;
		}
		for (Widget child : children)
		{
			walk(g, child, allowEllipse);
		}
	}

	/**
	 * A widget counts as clickable if it has a non-empty action, or a target verb.
	 * Targeted spells (Wind Strike etc.) carry "Cast" in the target verb, not the actions list.
	 */
	private static boolean hasAction(Widget widget)
	{
		String targetVerb = widget.getTargetVerb();
		if (targetVerb != null && !targetVerb.isEmpty())
		{
			return true;
		}

		String[] actions = widget.getActions();
		if (actions == null)
		{
			return false;
		}
		for (String action : actions)
		{
			if (action != null && !action.isEmpty())
			{
				return true;
			}
		}
		return false;
	}
}
