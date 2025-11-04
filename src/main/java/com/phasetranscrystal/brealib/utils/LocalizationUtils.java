package com.phasetranscrystal.brealib.utils;

import net.minecraft.client.resources.language.I18n;

import com.phasetranscrystal.brealib.BreaUtility;

public class LocalizationUtils {

    /**
     * This function calls `net.minecraft.client.resources.I18n.hasKey` when called on client
     * or `net.minecraft.util.text.translation.I18n.canTranslate` when called on server.
     * <ul>
     * <li>It is intended that translations should be done using `I18n` on the client.</li>
     * <li>For setting up translations on the server you should use `TextComponentTranslatable`.</li>
     * <li>`LocalisationUtils` is only for cases where some kind of translation is required on the server and there is
     * no client/player in context.</li>
     * <li>`LocalisationUtils` is "best effort" and will probably only work properly with en-us.</li>
     * </ul>
     *
     * @param localisationKey the localisation key passed to the underlying hasKey function
     * @return a boolean indicating if the given localisation key has localisations
     */
    public static boolean exist(String localisationKey) {
        if (BreaUtility.isClientSide()) {
            return I18n.exists(localisationKey);
        } else {
            return false;
        }
    }
}
