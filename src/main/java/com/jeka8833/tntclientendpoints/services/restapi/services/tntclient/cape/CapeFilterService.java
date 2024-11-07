package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.cape;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Iterator;

@Service
public class CapeFilterService {
    public byte[] loadTNTClientResource(String data, int limitFileSizeInBytes) throws IOException {
        if (data.startsWith("http")) {
            try (InputStream connectionStream = URI.create(data).toURL().openStream()) {
                byte[] imageByteArray = connectionStream.readNBytes(limitFileSizeInBytes + 1);
                if (imageByteArray.length > limitFileSizeInBytes) {
                    throw new IllegalArgumentException(
                            "Image size exceeded limit of " + limitFileSizeInBytes + " bytes");
                }

                return imageByteArray;
            }
        } else if (data.startsWith("data:image/")) {
            String base64Image = getBase64Part(data, limitFileSizeInBytes);

            byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
            if (imageByteArray.length > limitFileSizeInBytes) {
                throw new IllegalArgumentException("Image size exceeded limit of " + limitFileSizeInBytes + " bytes");
            }

            return imageByteArray;
        }

        throw new IllegalArgumentException("Invalid data stream format");
    }

    public boolean isValidImageParameters(byte[] image, String[] formats, int maxWidth, int maxHeight)
            throws IOException {
        try (var stream = new ByteArrayInputStream(image);
             ImageInputStream imageInputStream = ImageIO.createImageInputStream(stream)) {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) return false;

            ImageReader reader = iter.next();
            reader.setInput(imageInputStream, true, true);

            String formatName = reader.getFormatName();
            for (String format : formats) {
                if (format.equalsIgnoreCase(formatName)) {
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);

                    return width > 0 && width <= maxWidth && height > 0 && height <= maxHeight;
                }
            }
        }

        return false;
    }

    public byte @NotNull [] repackPlayerCape(byte @NotNull [] imageByteArray) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageByteArray));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int newWidth = 64;
        int newHeight = 32;
        while (width > newWidth || height > newHeight) {
            newWidth *= 2;
            newHeight *= 2;
        }

        width = Math.min(width, 22 * (newWidth / 64));
        height = Math.min(height, 17 * (newHeight / 32));

        var newBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        boolean hasAlpha = false;

        for (int y = 0; y < height; y++) {
            boolean yZone = y < newHeight / 32;

            for (int x = 0; x < width; x++) {
                int toMinX = x / (newWidth / 64);
                if (yZone && (toMinX == 0 || toMinX == 21)) continue;

                int pixel = bufferedImage.getRGB(x, y);
                if ((pixel & 0xFF000000) != 0xFF000000) {
                    hasAlpha = true;

                    if ((pixel & 0xFF000000) == 0) continue;
                }

                newBufferedImage.setRGB(x, y, pixel);
            }
        }

        return getImageAsByteArray(newBufferedImage, hasAlpha);
    }

    private static String getBase64Part(String data, int limitFileSizeInBytes) {
        int start = data.indexOf(',') + 1;
        if (start == 0 || start >= data.length()) {
            throw new IllegalArgumentException("Invalid data stream encoding");
        }

        if (data.length() - start >= limitFileSizeInBytes * 5) {
            throw new IllegalArgumentException("Image size exceeded limit of " + limitFileSizeInBytes + " bytes");
        }
        return data.substring(start);
    }

    private static byte[] getImageAsByteArray(BufferedImage image, boolean hasAlpha) throws IOException {
        BufferedImage outImage = image;
        if (!hasAlpha) {
            var tempImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = tempImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            outImage = tempImage;
        }

        var stream = new ByteArrayOutputStream();
        if (!ImageIO.write(outImage, "png", stream)) throw new NullPointerException("PNG writer not found");

        return stream.toByteArray();
    }
}
