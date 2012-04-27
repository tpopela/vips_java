/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Separator.java
 */

package org.fit.vips;

/**
 * Class that represents visual separator
 */
public class Separator implements Comparable<Separator> {
	public int startPoint;
	public int endPoint;
	public int weight = 10;

	public Separator(int start, int end) {
		this.startPoint = start;
		this.endPoint = end;
	}

	public Separator(int start, int end, int weight) {
		this.startPoint = start;
		this.endPoint = end;
		this.weight = weight;
	}

	@Override
	public int compareTo(Separator otherSeparator)
	{
		return this.weight - otherSeparator.weight;
	}
}
