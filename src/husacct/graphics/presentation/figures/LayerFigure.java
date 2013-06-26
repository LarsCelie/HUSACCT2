package husacct.graphics.presentation.figures;

import husacct.common.Resource;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.ImageFigure;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.TextFigure;

public class LayerFigure extends BaseFigure {
	private static final long	serialVersionUID	= 101138923385231941L;
	private RectangleFigure		body;
	private TextFigure			text;
	private BufferedImage		compIcon;
	private ImageFigure			compIconFig;
	
	public int					MIN_WIDTH			= 150;
	public int					MIN_HEIGHT			= 50;
	
	public LayerFigure(String name) {
		super(name);
		
		body = new RectangleFigure();
		text = new TextFigure(name);
		text.set(AttributeKeys.FONT_BOLD, true);
		children.add(body);
		children.add(text);
		
		compIconFig = new ImageFigure();
		compIconFig.set(AttributeKeys.STROKE_WIDTH, 0.0);
		compIconFig.set(AttributeKeys.FILL_COLOR, defaultBackgroundColor);
		
		try {
			// TODO There needs to be a icon for Projects
			URL componentImageURL = Resource.get(Resource.ICON_LAYER);
			compIcon = ImageIO.read(componentImageURL);
			compIconFig.setImage(null, compIcon);
			children.add(compIconFig);
		} catch (Exception e) {
			compIconFig = null;
			Logger.getLogger(this.getClass()).warn(
					"failed to load component icon image file");
		}
		
		body.set(AttributeKeys.FILL_COLOR, defaultBackgroundColor);
	}
	
	@Override
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
		if ((lead.x - anchor.x) < MIN_WIDTH) {
			lead.x = anchor.x + MIN_WIDTH;
		}
		if ((lead.y - anchor.y) < MIN_HEIGHT) {
			lead.y = anchor.y + MIN_HEIGHT;
		}
		
		body.setBounds(anchor, lead);
		
		// textbox centralising
		double plusX = (((lead.x - anchor.x) - text.getBounds().width) / 2);
		double plusY = (((lead.y - anchor.y) - text.getBounds().height) / 2);
		
		Point2D.Double textAnchor = (Double) anchor.clone();
		textAnchor.x += plusX;
		textAnchor.y += plusY;
		text.setBounds(textAnchor, null);
		
		if (compIconFig != null) {
			double iconAnchorX = lead.x - 6 - compIcon.getWidth();
			double iconAnchorY = anchor.y + 6;
			double iconLeadX = iconAnchorX + compIcon.getWidth();
			double iconLeadY = iconAnchorY + compIcon.getHeight();
			compIconFig.setBounds(new Point2D.Double(iconAnchorX, iconAnchorY),
					new Point2D.Double(iconLeadX, iconLeadY));
		}
		
		invalidate();
	}
	
	@Override
	public LayerFigure clone() {
		
		LayerFigure other = (LayerFigure) super.clone();
		other.body = body.clone();
		other.text = text.clone();
		other.compIconFig = compIconFig.clone();
		
		other.children = new ArrayList<Figure>();
		other.children.add(other.body);
		other.children.add(other.text);
		if (compIconFig != null) {
			other.children.add(other.compIconFig);
		}
		return other;
	}
	
	@Override
	public boolean isModule() {
		return true;
	}
}
