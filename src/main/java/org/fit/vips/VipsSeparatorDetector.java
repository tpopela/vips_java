/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorDetector.java
 */

package org.fit.vips;

import java.util.List;
/**
 * Common interface for separators detectors.
 * @author Tomas Popela
 *
 */
public interface VipsSeparatorDetector {

	public void fillPool();

	public void setVipsBlock(VipsBlock vipsBlock);

	public VipsBlock getVipsBlock();

	public void setVisualBlocks(List<VipsBlock> visualBlocks);

	public List<VipsBlock> getVisualBlocks();

	public void detectHorizontalSeparators();

	public void detectVerticalSeparators();

	public List<Separator> getHorizontalSeparators();

	public void setHorizontalSeparators(List<Separator> separators);

	public void setVerticalSeparators(List<Separator> separators);

	public List<Separator> getVerticalSeparators();

	public void setCleanUpSeparators(int treshold);

	public boolean isCleanUpEnabled();

}
