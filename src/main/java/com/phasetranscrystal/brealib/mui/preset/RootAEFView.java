package com.phasetranscrystal.brealib.mui.preset;

import com.phasetranscrystal.brealib.mui.MuiHelper;
import com.phasetranscrystal.brealib.mui.PublicTexture;
import com.phasetranscrystal.brealib.mui.widget.HoverAlphaButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.*;
import icyllis.modernui.graphics.drawable.ImageDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.*;
import org.jetbrains.annotations.NotNull;

public abstract class RootAEFView extends RelativeLayout {

    public static final int COLOR_ICON_BACKGROUND = 0xFFC1C1C4;
    public static final int COLOR_DIVIDING_LINE = 0xFF9F9F9F;
    public static final int COLOR_TOP = 0xFFD3D3D3;
    public static final int COLOR_CENTER = 0x605C5C5C;
    public static final int COLOR_LOWER = 0xB82A2A2A;
    public static final int COLOR_BOTTOM = 0xDF222222;

    public static final int[] GRADIENT_COLOR = new int[] { COLOR_TOP, COLOR_CENTER, COLOR_LOWER, COLOR_LOWER };
    public static final float[] GRADIENT_POSITON = new float[] { 0F, 0.15F, 0.6F, 1F };

    public static final int COLOR_TITLE_TEXT = 0xFF4A4C4B;

    public static final int TOP_BOTTOM_HEIGHT = 40;
    public static final int EDGE_R = 10;

    public final ResourceLocation iconPath;
    public final String titleKey;
    public final Image icon;
    protected final Paint paint;
    protected final Paint gradientPaint;

    public final int tbHeight;
    public final int r;

    protected final CloseButton closeButton;
    protected final LinearLayout titleLayout;
    protected final RelativeLayout centerLayout;

    public RootAEFView(Context context, ResourceLocation icon, String title) {
        super(context);
        this.iconPath = icon;
        this.titleKey = title;
        this.icon = icon == null ? null : Image.create(icon.getNamespace(), icon.getPath());
        this.paint = new Paint();
        this.gradientPaint = new Paint();

        setWillNotDraw(false);

        tbHeight = dp(TOP_BOTTOM_HEIGHT);
        r = dp(EDGE_R);

        init();

        // 关闭按钮
        LayoutParams closeButtonParams = new LayoutParams((int) (tbHeight * 0.6F), (int) (tbHeight * 0.6F));
        closeButtonParams.setMarginsRelative(0, (int) (tbHeight * 0.2F), (int) (tbHeight * 0.2F), 0);
        closeButtonParams.addRule(ALIGN_PARENT_RIGHT);
        closeButtonParams.addRule(ALIGN_PARENT_TOP);
        closeButton = new CloseButton(getContext());
        addView(closeButton, closeButtonParams);

        // 顶部标题条
        LayoutParams titleLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleLayoutParams.setMargins((int) (tbHeight * 1.2F), (int) (tbHeight * 0.3F), 0, 0);
        titleLayoutParams.addRule(ALIGN_PARENT_LEFT);
        titleLayoutParams.addRule(ALIGN_PARENT_TOP);
        titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setDividerPadding((int) (tbHeight * 0.15F));
        titleLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleLayout.setGravity(Gravity.BOTTOM);
        ShapeDrawable divider = new ShapeDrawable();
        divider.setShape(ShapeDrawable.VLINE);
        divider.setColor(COLOR_DIVIDING_LINE);
        divider.setSize(dp(1), (int) (tbHeight * 0.7F));
        titleLayout.setDividerDrawable(divider);
        addView(titleLayout, titleLayoutParams);
        {
            TextView mainTitleView = new TextView(getContext());
            mainTitleView.setText(I18n.get(titleKey));
            mainTitleView.setTextColor(COLOR_TITLE_TEXT);
            mainTitleView.setTextSize(18);
            mainTitleView.setPadding(0, 0, (int) (tbHeight * 0.1F), (int) (tbHeight * 0.075F));
            titleLayout.addView(mainTitleView);

            TextView secondTitleView = new TextView(getContext());
            secondTitleView.setText("TEST TEXT");
            secondTitleView.setTextColor(COLOR_DIVIDING_LINE);
            secondTitleView.setTextSize(12);
            secondTitleView.setPadding((int) (tbHeight * 0.1F), 0, (int) (tbHeight * 0.1F), (int) (tbHeight * 0.075F));
            titleLayout.addView(secondTitleView);
        }

        // 中部布局
        centerLayout = createCenterLayout();
        MuiHelper.setTestingBoarder(centerLayout);
        LayoutParams centerOuterLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        centerOuterLayoutParams.setMargins(0, tbHeight, 0, tbHeight);
        addView(centerLayout, centerOuterLayoutParams);
    }

    public void init() {}

    @Override
    @SuppressWarnings("all")
    public void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);

        // 绘制左上角区域
        paint.setColor(COLOR_ICON_BACKGROUND);
        canvas.drawRoundRect(0, 0, tbHeight, tbHeight, r, 0, 0, 0, paint);
        if (icon != null) {
            canvas.drawImage(icon, null, new RectF(0, 0, tbHeight, tbHeight), null);
        }

        // 绘制顶部区域
        paint.setColor(COLOR_TOP);
        canvas.drawRoundRect(tbHeight, 0, getWidth(), tbHeight, 0, r, 0, 0, paint);

        // 绘制标题内容
        // paint.setColor(COLOR_TITLE_TEXT);
        // paint.setTextSize(0.5F * tbHeight);
        // canvas.drawSimpleText(I18n.get(titleKey),
        // FontFamily.getSystemFontWithAlias("微软雅黑").getClosestMatch(FontPaint.NORMAL),
        // tbHeight * 1.2F, tbHeight * 0.8F, paint);
        canvas.drawImage(PublicTexture.getPublicImage(PublicTexture.ROOT_NAME_DEC), null,
                new RectF(tbHeight * 1.2F, tbHeight * 0.15F, tbHeight * 2.2F, tbHeight * 0.35F), null);

        // 绘制中部渐变区域
        canvas.drawRect(0, tbHeight, getWidth(), getHeight() - tbHeight, gradientPaint);

        // //绘制左侧修饰纹理
        // MuiHelper.imageMesh(PublicTexture.getPublicImage(PublicTexture.TEXTURE_LEFT_DEC), canvas,
        // 0, tbHeight, leftWidth, leftWidth, leftWidth, getHeight() - 2 * tbHeight);

        // 绘制底部区域
        paint.setColor(COLOR_BOTTOM);
        canvas.drawRoundRect(0, getHeight() - tbHeight, getWidth(), getHeight(), 0, 0, r, r, paint);

        // 绘制区域分界线
        paint.setColor(COLOR_DIVIDING_LINE);
        canvas.drawLine(0, tbHeight, getWidth(), tbHeight, paint);
        canvas.drawLine(tbHeight, 0, tbHeight, tbHeight, paint);
        canvas.drawLine(0, getHeight() - tbHeight, getWidth(), getHeight() - tbHeight, paint);
    }

    @Override
    protected void onSizeChanged(int width, int height, int prevWidth, int prevHeight) {
        super.onSizeChanged(width, height, prevWidth, prevHeight);
        gradientPaint.setShader(new LinearGradient(0, tbHeight, 0, getHeight() - tbHeight, GRADIENT_COLOR, GRADIENT_POSITON, Shader.TileMode.CLAMP, null));
    }

    public abstract RelativeLayout createCenterLayout();

    public static class CloseButton extends HoverAlphaButton {

        public static final OnClickListener CLOSE_SCREEN = v -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));

        public CloseButton(Context context) {
            this(context, CLOSE_SCREEN);
        }

        public CloseButton(Context context, Runnable behavior) {
            this(context, v -> behavior.run());
        }

        public CloseButton(Context context, OnClickListener behavior) {
            super(context);

            setOnClickListener(behavior);

            setBackground(new ImageDrawable(null, PublicTexture.getPublicImage(PublicTexture.ROOT_CLOSE_BUTTON)));
        }
    }
}
