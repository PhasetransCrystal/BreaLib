package com.phasetranscrystal.brealib.mui;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.mui.preset.NarrowPaginationAEFView;

import net.minecraft.resources.ResourceLocation;

import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;

public class TestingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        FrameLayout layout = new FrameLayout(requireContext());

        View centerBox = new NarrowPaginationAEFView(requireContext(), ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/icon/test.png"), "Testing Mac");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layout.dp(800), layout.dp(400));
        params.gravity = Gravity.CENTER;
        centerBox.setLayoutParams(params);

        layout.addView(centerBox);

        return layout;
    }
}
