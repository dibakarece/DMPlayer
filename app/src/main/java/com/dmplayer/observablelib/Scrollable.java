

package com.dmplayer.observablelib;

import android.view.ViewGroup;

/**
 * Interface for providing common API for observable and scrollable widgets.
 */
public interface Scrollable {
    /**
     * Set a callback listener.<br>
     * Developers should use {@link #addScrollViewCallbacks(ObservableScrollViewCallbacks)}
     * and {@link #removeScrollViewCallbacks(ObservableScrollViewCallbacks)}.
     *
     * @param listener Listener to set.
     */
    @Deprecated
    void setScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    /**
     * Add a callback listener.
     *
     * @param listener Listener to add.
     * @since 1.7.0
     */
    void addScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    /**
     * Remove a callback listener.
     *
     * @param listener Listener to remove.
     * @since 1.7.0
     */
    void removeScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    /**
     * Clear callback listeners.
     *
     * @since 1.7.0
     */
    void clearScrollViewCallbacks();

    /**
     * Scroll vertically to the absolute Y.<br>
     * Implemented classes are expected to scroll to the exact Y pixels from the top,
     * but it depends on the type of the widget.
     *
     * @param y Vertical position to scroll to.
     */
    void scrollVerticallyTo(int y);

    /**
     * Return the current Y of the scrollable view.
     *
     * @return Current Y pixel.
     */
    int getCurrentScrollY();

    /**
     * Set a touch motion event delegation ViewGroup.<br>
     * This is used to pass motion events back to parent view.
     * It's up to the implementation classes whether or not it works.
     *
     * @param viewGroup ViewGroup object to dispatch motion events.
     */
    void setTouchInterceptionViewGroup(ViewGroup viewGroup);
}
