package com.ugcs.geohammer.map.provider;

import com.ugcs.geohammer.release.Version;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class MacrostratTileProvider implements XyzTileProvider {

    private static final Logger log = LoggerFactory.getLogger(MacrostratTileProvider.class);

    private static final String TILE_URL_PATTERN = "https://tiles.macrostrat.org/carto/%d/%d/%d.png";

    private static final int CONNECT_TIMEOUT_MS = 5_000;

    private static final int READ_TIMEOUT_MS = 10_000;

    private final String userAgent;

    public MacrostratTileProvider(Version appVersion) {
        userAgent = "GeoHammer/" + Version.toString(appVersion) + " (https://github.com/ugcs/geohammer)";
    }

    @Override
    public int maxConcurrentRequests() {
        return 2;
    }

    @Override
    public int getMaxZoom() {
        return 22;
    }

    @Override
    public String getCachePrefix() {
        return "macrostrat_carto_256";
    }

    @Nullable
    @Override
    public BufferedImage fetchTile(XyzTile tile) {
        String url = String.format(TILE_URL_PATTERN, tile.z(), tile.x(), tile.y());
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.warn("Macrostrat tile server returned HTTP {} for {}", connection.getResponseCode(), url);
                return null;
            }
            try (var input = connection.getInputStream()) {
                return normalizeTile(ImageIO.read(input));
            }
        } catch (IOException | URISyntaxException e) {
            log.warn("Failed to fetch Macrostrat geologic tile {}", url, e);
            return null;
        }
    }

    private static @Nullable BufferedImage normalizeTile(@Nullable BufferedImage image) {
        if (image == null || image.getWidth() == 256 && image.getHeight() == 256) {
            return image;
        }
        BufferedImage normalized = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = normalized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, 256, 256, null);
        graphics.dispose();
        return normalized;
    }
}
