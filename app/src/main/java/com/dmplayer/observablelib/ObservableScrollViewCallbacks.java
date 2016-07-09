

package com.dmplayer.observablelib;

/**
 * Callbacks for Scrollable widgets.
 */
public interface ObservableScrollViewCallbacks {
    /**
     * Called when the scroll change events occurred.
     * <p>This won't be called just after the view is laid out, so if you'd like to
     * initialize the position of your views with this method, you should call this manually
     * or invoke scroll as appropriate.</p>
     *
     * @param scrollY     Scroll position in Y axis.
     * @param firstScroll True when this is called for the first time in the consecutive motion events.
     * @param dragging    True when the view is dragged and false when the view is scrolled in the inertia.
     */
    void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging);

    /**
     * Called when the down motion event occurred.
     */
    void onDownMotionEvent();

    /**
     * Called when the dragging ended or canceled.
     *
     * @param scrollState State to indicate the scroll direction.
     */
    void onUpOrCancelMotionEvent(ScrollState scrollState);
}
