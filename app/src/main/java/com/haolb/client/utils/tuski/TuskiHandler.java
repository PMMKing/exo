package com.haolb.client.utils.tuski;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.haolb.client.utils.QLog;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author zitian.zhang
 * @since 2013-1-18 下午3:24:16
 * 
 */
final class TuskiHandler extends Handler {
    private static final class Messages {
        private Messages() {
        }

        public static final int DISPLAY_TUSKI = 0x3820916;
        public static final int ADD_TUSKI_TO_VIEW = 0x1334832;
        public static final int REMOVE_TUSKI = 0x437943;
    }

    private static TuskiHandler INSTANCE;

    private final Queue<Tuski> tuskiQueue;

    private TuskiHandler() {
        tuskiQueue = new LinkedBlockingQueue<Tuski>();
    }

    /**
     * @return The currently used instance of the {@link }.
     */
    static synchronized TuskiHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TuskiHandler();
        }

        return INSTANCE;
    }

    /**
     * Inserts a {@link Tuski} to be displayed.
     *
     * @param tuski The {@link Tuski} to be displayed.
     */
    void add(Tuski tuski) {
        tuskiQueue.add(tuski);
        displayTuski();
    }

    /**
     * Displays the next {@link Tuski} within the queue.
     */
    private void displayTuski() {
        if (tuskiQueue.isEmpty()) {
            return;
        }

        // First peek whether the Tuski has an activity.
        final Tuski currentTuski = tuskiQueue.peek();

        // If the activity is null we poll the Tuski off the queue.
        if (currentTuski.getActivity() == null) {
            tuskiQueue.poll();
        }

        if (!currentTuski.isShowing()) {
            // Display the T
            sendMessage(currentTuski, Messages.ADD_TUSKI_TO_VIEW);
            if (currentTuski.listener != null) {
                currentTuski.listener.onDisplayed();
            }
        } else {
            sendMessageDelayed(currentTuski, Messages.DISPLAY_TUSKI, calculateTuskiDuration(currentTuski));
        }
    }

    private long calculateTuskiDuration(Tuski tuski) {
        long tuskiDuration = tuski.getStyle().duration;
        tuskiDuration += tuski.getInAnimation().getDuration();
        tuskiDuration += tuski.getOutAnimation().getDuration();
        return tuskiDuration;
    }

    /**
     * Sends a {@link Tuski} within a {@link android.os.Message}.
     *
     * @param tuski The {@link Tuski} that should be sent.
     * @param messageId The {@link android.os.Message} id.
     */
    private void sendMessage(Tuski tuski, final int messageId) {
        final Message message = obtainMessage(messageId);
        message.obj = tuski;
        sendMessage(message);
    }

    /**
     * Sends a {@link Tuski} within a delayed {@link android.os.Message}.
     *
     * @param tuski The {@link Tuski} that should be sent.
     * @param messageId The {@link android.os.Message} id.
     * @param delay The delay in milliseconds.
     */
    private void sendMessageDelayed(Tuski tuski, final int messageId, final long delay) {
        Message message = obtainMessage(messageId);
        message.obj = tuski;
        sendMessageDelayed(message, delay);
    }

    @Override
    public void handleMessage(Message message) {
        final Tuski tuski = (Tuski) message.obj;

        switch (message.what) {
        case Messages.DISPLAY_TUSKI: {
            displayTuski();
            break;
        }

        case Messages.ADD_TUSKI_TO_VIEW: {
            addTuskiToView(tuski);
            break;
        }

        case Messages.REMOVE_TUSKI: {
            removeTuski(tuski);
            if (tuski.listener != null) {
                tuski.listener.onRemoved();
            }
            break;
        }

        default: {
            super.handleMessage(message);
            break;
        }
        }
    }

    /**
     * Adds a {@link Tuski} to the {@link android.view.ViewParent} of it's {@link android.app.Activity}.
     *
     * @param tuski The {@link Tuski} that should be added.
     */
    private void addTuskiToView(Tuski tuski) {
        // don't add if it is already showing
        if (tuski.isShowing()) {
            return;
        }

        View contentView = tuski.getView();
        if (contentView.getParent() == null) {
            ViewGroup.LayoutParams params = contentView.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            try {
                Field f = params.getClass().getField("gravity");
                f.setInt(params, tuski.getStyle().gravity);
            } catch (Exception e) {
                QLog.e("", e.getLocalizedMessage(), e);//.getLogger(getClass()).d("target container do not support gravity");
            }

            // display Tuski in ViewGroup is it has been supplied
            if (tuski.getViewGroup() != null) {
                tuski.getViewGroup().addView(contentView, tuski.indexAddToViewGroup, params);
            } else {
                Activity activity = tuski.getActivity();
                if (null == activity || activity.isFinishing()) {
                    return;
                }
                activity.addContentView(contentView, params);
            }
        }
        contentView.startAnimation(tuski.getInAnimation());
        announceForAccessibilityCompat(tuski.getActivity(), tuski.getText());
        sendMessageDelayed(tuski, Messages.REMOVE_TUSKI, tuski.getStyle().duration
                + tuski.getInAnimation().getDuration());
    }

    /**
     * Removes the {@link Tuski}'s view after it's display durationInMilliseconds.
     *
     * @param tuski The {@link Tuski} added to a {@link android.view.ViewGroup} and should be removed.
     */
    protected void removeTuski(Tuski tuski) {
        View contentView = tuski.getView();
        ViewGroup container = (ViewGroup) contentView.getParent();

        if (container != null) {
            contentView.startAnimation(tuski.getOutAnimation());

            // Remove the Tuski from the queue.
            Tuski removed = tuskiQueue.poll();

            // Remove the Tuski from the view's parent.
            container.removeView(contentView);
            if (removed != null) {
                removed.detachActivity();
                removed.detachViewGroup();
                if (removed.listener != null) {
                    removed.listener.onRemoved();
                }
                removed.detachListener();
            }

            // Send a message to display the next tuski but delay it by the out
            // animation duration to make sure it finishes
            sendMessageDelayed(tuski, Messages.DISPLAY_TUSKI, tuski.getOutAnimation().getDuration());
        }
    }

    /**
     * Removes a {@link Tuski} immediately, even when it's currently being displayed.
     *
     * @param tuski The {@link Tuski} that should be removed.
     */
    void removeTuskiImmediately(Tuski tuski) {
        // if Tuski has already been displayed then it may not be in the queue (because it was popped).
        // This ensures the displayed Tuski is removed from its parent immediately, whether another instance
        // of it exists in the queue or not.
        // Note: Tuski.isShowing() is false here even if it really is showing, as tuskiView object in
        // Tuski seems to be out of sync with reality!
        if (tuski.getActivity() != null && tuski.getView() != null && tuski.getView().getParent() != null) {
            ((ViewGroup) tuski.getView().getParent()).removeView(tuski.getView());

            // remove any messages pending for the Tuski
            removeAllMessagesForTuski(tuski);
        }
        // remove any matching Tuskies from queue
        if (tuskiQueue != null) {
            final Iterator<Tuski> iterator = tuskiQueue.iterator();
            while (iterator.hasNext()) {
                final Tuski c = iterator.next();
                if (c.equals(tuski) && c.getActivity() != null) {
                    // remove the Tuski from the content view
                    if (tuski.isShowing()) {
                        ((ViewGroup) c.getView().getParent()).removeView(c.getView());
                    }

                    // remove any messages pending for the Tuski
                    removeAllMessagesForTuski(c);

                    // remove the Tuski from the queue
                    iterator.remove();

                    // we have found our Tuski so just break
                    break;
                }
            }
        }
    }

    /**
     * Removes all {@link Tuski}s from the queue.
     */
    void clearTuskiQueue() {
        removeAllMessages();

        if (tuskiQueue != null) {
            for (Tuski tuski : tuskiQueue) {
                if (tuski.isShowing()) {
                    ((ViewGroup) tuski.getView().getParent()).removeView(tuski.getView());
                }
            }
            tuskiQueue.clear();
        }
    }

    /**
     * Removes all {@link Tuski}s for the provided activity. This will remove tuski from {@link android.app.Activity}s content view
     * immediately.
     */
    void clearTuskisForActivity(Activity activity) {
        if (tuskiQueue != null) {
            Iterator<Tuski> iterator = tuskiQueue.iterator();
            while (iterator.hasNext()) {
                Tuski tuski = iterator.next();
                if (tuski.getActivity() != null && tuski.getActivity().equals(activity)) {
                    // remove the tuski from the content view
                    if (tuski.isShowing()) {
                        ((ViewGroup) tuski.getView().getParent()).removeView(tuski.getView());
                    }

                    removeAllMessagesForTuski(tuski);

                    // remove the tuski from the queue
                    iterator.remove();
                }
            }
        }
    }

    private void removeAllMessages() {
        removeMessages(Messages.ADD_TUSKI_TO_VIEW);
        removeMessages(Messages.DISPLAY_TUSKI);
        removeMessages(Messages.REMOVE_TUSKI);
    }

    private void removeAllMessagesForTuski(Tuski tuski) {
        removeMessages(Messages.ADD_TUSKI_TO_VIEW, tuski);
        removeMessages(Messages.DISPLAY_TUSKI, tuski);
        removeMessages(Messages.REMOVE_TUSKI, tuski);

    }

    /**
     * Generates and dispatches an SDK-specific spoken announcement.
     * <p>
     * For backwards compatibility, we're constructing an event from scratch using the appropriate event type. If your
     * application only targets SDK 16+, you can just call View.announceForAccessibility(CharSequence).
     * </p>
     *
     * note: AccessibilityManager is only available from API lvl 4.
     *
     * Adapted from https://http://eyes-free.googlecode.com/files/accessibility_codelab_demos_v2_src.zip via
     * https://github.com/coreform/android-formidable-validation
     *
     * @param context Used to get {@link android.view.accessibility.AccessibilityManager}
     * @param text The text to announce.
     */
    public static void announceForAccessibilityCompat(Context context, CharSequence text) {
        if (Build.VERSION.SDK_INT >= 4) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) context
                    .getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (!accessibilityManager.isEnabled()) {
                return;
            }

            // Prior to SDK 16, announcements could only be made through FOCUSED
            // events. Jelly Bean (SDK 16) added support for speaking text verbatim
            // using the ANNOUNCEMENT event type.
            final int eventType;
            if (Build.VERSION.SDK_INT < 16) {
                eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
            } else {
                eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
            }

            // Construct an accessibility event with the minimum recommended
            // attributes. An event without a class name or package may be dropped.
            final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            event.getText().add(text);
            event.setClassName(TuskiHandler.class.getName());
            event.setPackageName(context.getPackageName());

            // Sends the event directly through the accessibility manager. If your
            // application only targets SDK 14+, you should just call
            // getParent().requestSendAccessibilityEvent(this, event);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }
}
