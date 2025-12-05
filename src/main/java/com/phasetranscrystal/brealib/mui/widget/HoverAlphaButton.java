package com.phasetranscrystal.brealib.mui.widget;

import net.minecraft.client.Minecraft;

import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.util.FloatProperty;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;

public class HoverAlphaButton extends Button {

    public static final Runnable CLOSE_SCREEN = () -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    public final ObjectAnimator hoverAnimator;

    public HoverAlphaButton(Context context) {
        super(context);
        hoverAnimator = ObjectAnimator.ofFloat(this, new AlphaProperty("hover_alpha"), 0.6F, 1F);
        hoverAnimator.setDuration(100);

        setClickable(true);
        setFocusable(true);
        setAlpha(0.6F);

        setOnHoverListener((view, event) -> switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER -> post(hoverAnimator::start);
            case MotionEvent.ACTION_HOVER_EXIT -> post(hoverAnimator::reverse);
            default -> false;
        });
    }

    public static class AlphaProperty extends FloatProperty<View> {

        public AlphaProperty(String name) {
            super(name);
        }

        @Override
        public void setValue(View object, float value) {
            object.setAlpha(value);
        }

        @Override
        public Float get(View object) {
            return object.getAlpha();
        }
    }
}
