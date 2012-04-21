package org.fit.vips;

/**
 * Class that represents visual separator
 */
public class Separator {
	public int startPoint;
	public int endPoint;
	public int weight = 10;
	public boolean horizontal = true;

	public Separator(int start, int end) {
		this.startPoint = start;
		this.endPoint = end;
	}

	public Separator(int start, int end, boolean horizontal) {
		this.startPoint = start;
		this.endPoint = end;
		this.horizontal = horizontal;
	}

	public Separator(int start, int end, int weight) {
		this.startPoint = start;
		this.endPoint = end;
		this.weight = weight;
	}
}
