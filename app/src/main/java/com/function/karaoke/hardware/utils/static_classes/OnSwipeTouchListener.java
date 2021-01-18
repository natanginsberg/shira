package com.function.karaoke.hardware.utils.static_classes;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {
    private float x1;

//    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
//        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = e.getX();
//                if (mListener != null)
//                {
//                    mListener.OnItemClick(v, Position);
//                }
                break;
            case MotionEvent.ACTION_UP:
                float x2 = e.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > 5) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        onSwipeRight();
                    }

                    // Right to left swipe action
                    else {
                        onSwipeLeft();
                    }
                }
                break;
        }
        return false;
    }

//    private final class GestureListener extends SimpleOnGestureListener {
//
//        private static final int SWIPE_THRESHOLD = 100;
//        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
//
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return true;
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            boolean result = false;
//            try {
//                float diffY = e2.getY() - e1.getY();
//                float diffX = e2.getX() - e1.getX();
//                if (Math.abs(diffX) > Math.abs(diffY)) {
//                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffX > 0) {
//                            onSwipeRight();
//                        } else {
//                            onSwipeLeft();
//                        }
//                        result = true;
//                    }
//                }
//                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                    if (diffY > 0) {
//                        onSwipeBottom();
//                    } else {
//                        onSwipeTop();
//                    }
//                    result = true;
//                }
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//            return result;
//        }
//
//
//    }


    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}