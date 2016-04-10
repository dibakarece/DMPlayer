/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.uicomponent;

import com.nineoldandroids.view.ViewHelper;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ToolbarHidingOnScrollListener extends RecyclerView.OnScrollListener {

	private final View toolbarContainer;
	private final View toolbar;
	private final View parallaxScrollingView;
	private final View lastToolbarView;
	private int currentDY = 0;

	private float parallaxScrollingFactor = 0.7f;

	public ToolbarHidingOnScrollListener(View toolbarContainer, View toolbar, View lastToolbarView) {
		this(toolbarContainer, toolbar, lastToolbarView, null);
	}

	public ToolbarHidingOnScrollListener(View toolbarContainer, View toolbar, View lastToolbarView, View parallaxScrollingView) {
		this.toolbarContainer = toolbarContainer;
		this.toolbar = toolbar;
		this.lastToolbarView = lastToolbarView;
		this.parallaxScrollingView = parallaxScrollingView;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		super.onScrollStateChanged(recyclerView, newState);

		if (newState == RecyclerView.SCROLL_STATE_IDLE) {
			if (Math.abs(toolbarContainer.getTranslationY()) > toolbar.getHeight()) {
				hideToolbar();
			} else {
				showToolbar();
			}
		} else {
			toolbarContainer.clearAnimation();
		}
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);
		scrollColoredViewParallax(dy);
		if (dy > 0) {
			hideToolbarBy(dy);
			currentDY = dy;
		} else {
			showToolbarBy(dy);
		}
	}

	public void reset() {
		ViewHelper.setY(parallaxScrollingView, 0);
		ViewHelper.setY(toolbarContainer, 0);
	}

	protected void showToolbar() {
		toolbarContainer.clearAnimation();
		toolbarContainer.animate().translationY(0).start();
	}

	private void hideToolbar() {
		toolbarContainer.clearAnimation();
		toolbarContainer.animate().translationY(-lastToolbarView.getBottom()).start();
	}

	private void scrollColoredViewParallax(int dy) {
		if (parallaxScrollingView != null) {
			int absoluteTranslationY = (int) (parallaxScrollingView.getTranslationY() - dy * parallaxScrollingFactor);
			parallaxScrollingView.setTranslationY(Math.min(absoluteTranslationY, 0));
		}
	}

	private void hideToolbarBy(int dy) {
		if (cannotHideMore(dy)) {
			toolbarContainer.setTranslationY(-lastToolbarView.getBottom());
		} else {
			toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
		}
	}

	private boolean cannotHideMore(int dy) {
		return Math.abs(toolbarContainer.getTranslationY() - dy) > lastToolbarView.getBottom();
	}

	protected void showToolbarBy(int dy) {
		if (cannotShowMore(dy)) {
			toolbarContainer.setTranslationY(0);
		} else {
			toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
		}
	}

	private boolean cannotShowMore(int dy) {
		return toolbarContainer.getTranslationY() - dy > 0;
	}

}
