package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.tab;

import com.jeka8833.tntclientendpoints.services.general.minecraft.ChatColor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TabFilterService {
    private static final Pattern STRIP_COLOR =
            Pattern.compile("(?i)[&" + ChatColor.COLOR_CHAR + "][0-9A-FK-OR]");

    public boolean isValidAnimation(String[] animation) {
        if (animation == null || animation.length == 0 || animation.length > 16) return false;

        boolean hasChar = false;

        for (String s : animation) {
            if (s == null) continue;

            if (s.length() > 32) return false;
            if (stripColor(s).length() > 8) return false;

            hasChar = true;
        }

        return hasChar;
    }

    public String[] normalizeAnimation(String[] animation) {
        if (animation == null) return null;

        String[] animationCopy = new String[animation.length];

        for (int i = 0; i < animation.length; i++) {
            if (animation[i] == null) {
                animationCopy[i] = "";
            } else {
                animationCopy[i] = normalizeColorCodes(animation[i]);

                if (!animationCopy[i].endsWith(ChatColor.RESET)) {
                    animationCopy[i] += ChatColor.RESET;
                }
            }
        }

        return animationCopy;
    }

    private static String stripColor(String text) {
        if (text == null) return null;

        return STRIP_COLOR.matcher(text).replaceAll("");
    }

    private static String normalizeColorCodes(String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if ((b[i] == '&' || b[i] == ChatColor.COLOR_CHAR) &&
                    "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
