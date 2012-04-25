package husacct.graphics.task;

import husacct.graphics.presentation.figures.BaseFigure;

public interface MouseClickListener {

	public void moduleZoom(BaseFigure zoomedModuleFigure);
	
	public void moduleZoomOut();

	public void figureSelected(BaseFigure clickedFigure);
}
