package com.phasetranscrystal.brealib.mui;

import icyllis.modernui.graphics.*;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;

public class MuiHelper {

    public static void imageMesh(Image image, Canvas canvas, int x, int y,
                                 int picWidth, int picHeight, int meshWidth, int meshHeight, boolean clip) {
        // 1. 参数有效性检查
        if (image == null || canvas == null || picWidth <= 0 || picHeight <= 0 || meshWidth <= 0 || meshHeight <= 0) {
            return;
        }

        if (clip) {
            // 2. 保存当前画布状态（包括矩阵和裁剪区域）
            canvas.save();

            // 3. 设置裁剪区域为指定的目标矩形
            // 这将确保后续的所有绘制操作都不会超出这个区域
            canvas.clipRect(x, y, x + meshWidth, y + meshHeight);
        }

        // 4. 计算平铺所需的行列数（使用进一法确保完全覆盖）
        int cols = (int) Math.ceil((double) meshWidth / picWidth) + 1; // 多铺一行一列确保覆盖
        int rows = (int) Math.ceil((double) meshHeight / picHeight) + 1;

        // 5. 平铺绘制（现在超出 mesh 区域的部分会自动被裁剪）
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                int drawX = x + i * picWidth;
                int drawY = y + j * picHeight;

                // 创建目标矩形
                Rect dst = new Rect(drawX, drawY, drawX + picWidth, drawY + picHeight);

                // 绘制图片（超出裁剪区域的部分不会显示）
                canvas.drawImage(image, null, dst, null);
            }
        }

        if (clip) {
            // 6. 恢复画布状态，移除裁剪区域的影响
            canvas.restore();
        }
    }

    public static void setTestingBoarder(View view) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setShape(ShapeDrawable.RECTANGLE);
        drawable.setCornerRadius(16f);
        drawable.setStroke(2, Color.CYAN);
        drawable.setColor(Color.TRANSPARENT);
        view.setBackground(drawable);
    }
}
