package com.phasetranscrystal.brealib.mui.preset;

import com.phasetranscrystal.brealib.mui.MuiHelper;
import com.phasetranscrystal.brealib.mui.PublicTexture;

import net.minecraft.resources.ResourceLocation;

import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.PropertyValuesHolder;
import icyllis.modernui.animation.TimeInterpolator;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.util.IntProperty;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;

public class NarrowPaginationAEFView extends RootAEFView {

    public static final int PAGINATION_WIDTH = (int) (RootAEFView.TOP_BOTTOM_HEIGHT * 0.625F);
    public static final int PAGINATION_BUTTON_HEIGHT = PAGINATION_WIDTH * 3;
    public static final int PAGINATION_EDGE_DIST = 1;

    public NarrowPaginationAEFView(Context context, ResourceLocation icon, String title) {
        super(context, icon, title);
    }

    @Override
    public void init() {
        super.init();
        paginationWidth = dp(PAGINATION_WIDTH);
        paginationButtonHeight = dp(PAGINATION_BUTTON_HEIGHT);
        paginationEdgeDist = dp(PAGINATION_EDGE_DIST);
        paginationCR = paginationWidth / 2 - paginationEdgeDist;
    }

    private int paginationWidth;
    private int paginationButtonHeight;
    private int paginationEdgeDist;
    private int paginationCR;

    protected PaginationButtonGroup centerRadioGroup;

    @Override
    public RelativeLayout createCenterLayout() {
        RelativeLayout centerLayout = new RelativeLayout(getContext());

        centerRadioGroup = new PaginationButtonGroup(getContext());
        centerRadioGroup.setOrientation(LinearLayout.VERTICAL);
        centerRadioGroup.setGravity(Gravity.CENTER);
        MuiHelper.setTestingBoarder(centerRadioGroup);
        centerLayout.addView(centerRadioGroup, new LinearLayout.LayoutParams(paginationWidth, ViewGroup.LayoutParams.MATCH_PARENT));

        centerRadioGroup.addView(new PageButton(getContext(), 1001, PublicTexture.getPublicImage(PublicTexture.ICON_BACKPACK)));
        centerRadioGroup.addView(new PageButton(getContext(), 1002, PublicTexture.getPublicImage(PublicTexture.ICON_BACKPACK)));
        centerRadioGroup.preCheck(1001);

        return centerLayout;
    }

    public class PaginationButtonGroup extends RadioGroup {

        public static final int CHECKED_BG_COLOR = 0xFF2C2C2A;
        private final Paint paint = new Paint();
        private int flagY;
        private final ObjectAnimator animator;

        {
            paint.setColor(CHECKED_BG_COLOR);
        }

        public PaginationButtonGroup(Context context) {
            super(context);
            setWillNotDraw(false);
            animator = ObjectAnimator.ofInt(this, property, 0);
            animator.setDuration(250);
            animator.setInterpolator(TimeInterpolator.DECELERATE);

            setOnCheckedChangeListener((group, id) -> {
                if (animator.isRunning()) animator.cancel();
                View view = findViewById(id);
                animator.setValues(PropertyValuesHolder.ofInt(flagY, view.getTop() + view.getHeight() / 2));
                animator.start();
            });
        }

        @Override
        protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
            return new LinearLayout.LayoutParams(paginationWidth, paginationButtonHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            MuiHelper.imageMesh(PublicTexture.getPublicImage(PublicTexture.ROOT_LEFT_DEC), canvas,
                    0, 0, getWidth(), getWidth(), getWidth(), getHeight(), false);
            if (this.getChildCount() >= 1 && this.getCheckedId() != NO_ID) {
                canvas.drawRoundRect(paginationEdgeDist, flagY - paginationButtonHeight / 2, getWidth() - paginationEdgeDist, flagY + paginationButtonHeight / 2,
                        paginationCR, paginationCR, paginationCR, paginationCR, paint);
            }
        }

        @Override
        protected void onSizeChanged(int width, int height, int prevWidth, int prevHeight) {
            super.onSizeChanged(width, height, prevWidth, prevHeight);
            if (this.getCheckedId() != NO_ID) {
                View view = findViewById(getCheckedId());
                this.flagY = view.getHeight() / 2 + view.getTop();
            } else {
                this.flagY = height / 2;
            }
        }

        public void preCheck(int id) {
            this.check(id);
            if (findViewById(id) instanceof PageButton b) {
                b.color = PageButton.COLOR_CHECKED;
            }
        }

        public static final PosProperty property = new PosProperty();

        public static class PosProperty extends IntProperty<PaginationButtonGroup> {

            public PosProperty() {
                super("pagination_moving_animator");
            }

            @Override
            public void setValue(PaginationButtonGroup object, int value) {
                object.flagY = value;
            }

            @Override
            public Integer get(PaginationButtonGroup object) {
                return object.flagY;
            }
        }
    }

    public static class PageButton extends CompoundButton {

        public static final int COLOR_CHECKED = 0xFFD5D3CF;
        public static final int COLOR_UNCHECKED = 0xBFAFB1B3;

        public final Image icon;
        public final ObjectAnimator textureColorAnimator;
        public int color = COLOR_UNCHECKED;
        private final Paint paint = new Paint();

        public PageButton(Context context, int id, ResourceLocation imgLoc) {
            this(context, id, Image.create(imgLoc.getNamespace(), imgLoc.getPath()));
        }

        public PageButton(Context context, int id, Image icon) {
            super(context);
            setId(id);
            setFocusable(true);
            setClickable(true);
            this.icon = icon;

            // paint.setColorFilter();

            textureColorAnimator = ObjectAnimator.ofArgb(this, colorProperty, COLOR_UNCHECKED, COLOR_CHECKED);
            setOnHoverListener((view, event) -> switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER -> post(textureColorAnimator::start);
                case MotionEvent.ACTION_HOVER_EXIT -> !isChecked() && post(textureColorAnimator::reverse);
                default -> false;
            });

            setOnCheckedChangeListener((view, check) -> {
                if (!check) post(textureColorAnimator::reverse);
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int v = (getHeight() - getWidth()) / 2;
            canvas.drawImage(icon, null, new Rect(0, v, getWidth(), v + getWidth()), paint);
        }

        private static final ColorProperty colorProperty = new ColorProperty();

        public static class ColorProperty extends IntProperty<PageButton> {

            public ColorProperty() {
                super("button_texture_color");
            }

            @Override
            public void setValue(PageButton object, int value) {
                object.paint.setColor(value);
            }

            @Override
            public Integer get(PageButton object) {
                return object.paint.getColor();
            }
        }
    }
}
