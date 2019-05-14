package com.ugcs.gprvisualizer.gpr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class AmpMapDrawer {

	private Model model;
	private DblArray dblArray;
	private double[][] scaleArray;
	private ArrayBuilder scaleArrayBuilder;
	private ArrayBuilder autoArrayBuilder;

	public AmpMapDrawer(Model model) {
		this.model = model;

		scaleArrayBuilder = new ScaleArrayBuilder(model.getSettings());
		autoArrayBuilder = new AutomaticScaleBuilder(model);

	}

	private ArrayBuilder getArrayBuilder() {
		if (model.getSettings().autogain) {
			return autoArrayBuilder;
		} else {
			return scaleArrayBuilder;
		}

	}

	public BufferedImage render(final RecalculationLevel level) {

		if (level == RecalculationLevel.COORDINATES || level == RecalculationLevel.BUFFERED_IMAGE) {

			scaleArray = getArrayBuilder().build();
			dblArray = calculateDblArray();
		}

		BufferedImage img = dblArray.toImg();

		Graphics2D g2 = (Graphics2D) img.getGraphics();

		if (model.getSettings().showpath) {
			drawGPSPath(g2);
		}

		drawSelection(g2);

		drawScale(g2);

		//imgProfile(g2);

		g2.dispose();

		return img;
	}

	private void drawScale(Graphics2D g2) {

		int width = model.getSettings().getWidth() / 4;
		int x = width / 4;
		int topmrg = 20;
		double dist = width / model.getSettings().kf / 100;
		String text = String.format("%.2f m", dist);

		g2.setColor(Color.LIGHT_GRAY);
		g2.setStroke(new BasicStroke(4.0f));
		g2.drawLine(x, topmrg, x + width, topmrg);

		g2.setStroke(new BasicStroke(2.0f));
		g2.setColor(Color.BLACK);
		g2.drawLine(x, topmrg, x + width, topmrg);

		///
		int textboxwidth = 70;
		int textboxheight = 46;
		Rectangle rect = new Rectangle(x + (width - textboxwidth) / 2, topmrg - textboxheight / 2, textboxwidth,
				textboxheight);

		Font font = new Font("Verdana", Font.BOLD, 10);
		FontMetrics metrics = g2.getFontMetrics(font);

		int txtwidth = metrics.stringWidth(text);
		int txtheight = metrics.getHeight();
		int txtx = rect.x + (rect.width - txtwidth) / 2;
		int txty = rect.y + ((rect.height - txtheight) / 2) + metrics.getAscent();

		rect.grow((txtwidth - rect.width) / 2 + 3, (txtheight - rect.height) / 2);
		///
		g2.setColor(Color.WHITE);
		g2.fillRect(rect.x, rect.y, rect.width, rect.height);
		g2.setFont(font);
		g2.setColor(Color.BLACK);
		g2.drawString(text, txtx, txty);
	}

	private void drawSelection(Graphics2D g2) {
		Scan scan = getSelectedScan();
		if (scan != null) {
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(4.0f));
			int r = 20;
			// g2.drawOval(scan.localX-10, scan.localY-10, 20, 20);
			g2.drawLine(scan.localX - r, scan.localY, scan.localX + r, scan.localY);
			g2.drawLine(scan.localX, scan.localY - r, scan.localX, scan.localY + r);

			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(2.0f));
			// g2.drawOval(scan.localX-10, scan.localY-10, 20, 20);
			g2.drawLine(scan.localX - r, scan.localY, scan.localX + r, scan.localY);
			g2.drawLine(scan.localX, scan.localY - r, scan.localX, scan.localY + r);

		}
	}

	private void drawGPSPath(Graphics2D g2) {
		g2.setStroke(new BasicStroke(1.1f));

		g2.setColor(Color.GRAY);

		Integer x = null;
		Integer y = null;
		for (Scan scan : model.getScans()) {

			if (x != null) {

				g2.drawLine(x, y, scan.localX, scan.localY);
			}

			x = scan.localX;
			y = scan.localY;
		}
	}

	private DblArray calculateDblArray() {
		
		DblArray da = new DblArray(model.getSettings().getWidth(), model.getSettings().getHeight());
		if(model.getScans() == null) {
			return da;
		}
		
		int start = norm(model.getSettings().layer, 0, model.getSettings().maxsamples);
		int finish = norm(model.getSettings().layer + model.getSettings().hpage, 0, model.getSettings().maxsamples);

		for (Scan scan : model.getScans()) {
			int dx = scan.localX;
			int dy = scan.localY;

			double alpha = calcAlpha(scan.values, start, finish);

			da.drawCircle(dx, dy, model.getSettings().radius, alpha);

		}
		return da;
	}

	private double calcAlpha(float[] values, int start, int finish) {
		double mx = 0;
		double threshold = scaleArray[0][start];
		double factor = scaleArray[1][start];

		for (int i = start; i < finish; i++) {

			mx = Math.max(mx, Math.abs(values[i]));
		}

		double val = Math.max(0, mx - threshold) * factor;

		return Math.max(0, Math.min(val, 200));

	}

	private int norm(int i, int min, int max) {

		return Math.min(Math.max(i, min), max - 1);
	}

	public Scan getSelectedScan() {
		if (model.getSettings().selectedScanIndex >= 0) {
			return model.getScans().get(model.getSettings().selectedScanIndex);
		}
		return null;
	}

	static float kfy = 20;
	static int kfx = 3;

	public void imgProfile(Graphics2D g2d) {
		if (getSelectedScan() == null) {
			return;
		}

		// int middle = model.getSettings().getWidth()/2;
		// int width = 2048;
		int height = model.getSettings().getHeight() / 2;

		float[] values = getSelectedScan().values;
		float[] avgvalues = avgabs(getSelectedScan().values);

		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1.0f));

		int x1 = 0;
		int y1 = (int) height;
		int x2 = values.length * kfx;
		int y2 = (int) height;

		g2d.drawLine(x1, y1, x2, y2);

		drawGraphic(g2d, height, values);

		drawGraphic(g2d, height, avgvalues);
	}

	private void drawGraphic(Graphics2D g2d, int height, float[] values) {
		int x1;
		int y1;
		int x2;
		int y2;
		for (int i = 1; i < values.length; i++) {
			int i0 = i - 1;

			x1 = i0 * kfx;
			y1 = (int) (values[i0] / kfy) + height;
			x2 = i * kfx;
			y2 = (int) (values[i] / kfy) + height;

			g2d.drawLine(x1, y1, x2, y2);

		}
	}

	private float[] avgabs(float[] values) {
		for (int c = 0; c < 10; c++) {
			float[] r = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				r[i] = 0.1f * Math.abs(values[norm(i - 1, values.length)]) + 0.8f * Math.abs(values[i]) + 0.1f * Math.abs(values[norm(i + 1, values.length)]);
			}
			values = r;
		}

		return values;
	}

	int norm(int i, int max){
		return i < 0 ? 0 : (i >= max ? max-1 : i);
	}

	public void clear() {
		
		autoArrayBuilder.clear();
		scaleArrayBuilder.clear();
		
	}

}
