package com.phasetranscrystal.brealib.mui;

import com.phasetranscrystal.brealib.BreaLib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import icyllis.modernui.ModernUI;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.annotation.Nonnull;

@EventBusSubscriber
public class AMImageStore {

    private static final AMImageStore INSTANCE = new AMImageStore();

    private static final BitmapFactory.Options ALPHA_MASK = new BitmapFactory.Options();

    static {
        ALPHA_MASK.inPreferredFormat = Bitmap.Format.ALPHA_8;
        ALPHA_MASK.outFormat = Bitmap.Format.ALPHA_8;
    }

    private final Object mLock = new Object();
    private HashMap<String, HashMap<String, WeakReference<Image>>> images = new HashMap<>();

    private AMImageStore() {}

    /**
     * @return the global texture manager instance
     */
    public static AMImageStore getInstance() {
        return INSTANCE;
    }

    // internal use
    public void clear() {
        synchronized (mLock) {
            for (var cache : images.values()) {
                for (var entry : cache.values()) {
                    var image = entry.get();
                    if (image != null) {
                        image.close();
                    }
                }
            }
            images = new HashMap<>();
        }
    }

    public static Image get(ResourceLocation imgLoc) {
        return get(imgLoc.getNamespace(), imgLoc.getPath());
    }

    public static Image get(@NonNull String namespace, @NonNull String path) {
        return getInstance().getOrCreate(namespace, path);
    }

    @Nullable
    public Image getOrCreate(@NonNull String namespace, @NonNull String path) {
        synchronized (mLock) {
            var imageRef = images.computeIfAbsent(namespace, __ -> new HashMap<>()).get(path);
            Image image;
            if (imageRef != null && (image = imageRef.get()) != null && !image.isClosed()) {
                return image;
            }
        }
        try (var stream = ModernUI.getInstance().getResourceStream(namespace, path);
                var bitmap = BitmapFactory.decodeStream(stream, ALPHA_MASK)) {
            var newImage = Image.createTextureFromBitmap(bitmap);
            synchronized (mLock) {
                var cache = images.computeIfAbsent(namespace, __ -> new HashMap<>());
                var imageRef = cache.get(path);
                Image image;
                if (imageRef != null && (image = imageRef.get()) != null && !image.isClosed()) {
                    // race
                    if (newImage != null) {
                        newImage.close();
                    }
                    return image;
                }
                if (newImage != null) {
                    cache.put(path, new WeakReference<>(newImage));
                    return newImage;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SubscribeEvent
    public static void registerResourceListener(@Nonnull AddClientReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "mui/alpha_mask_pic_cache"), (ResourceManagerReloadListener) manager -> {
            AMImageStore.getInstance().clear();
        });
    }

    public static void onGameClose(GameShuttingDownEvent event) {
        AMImageStore.getInstance().clear();
    }
}
