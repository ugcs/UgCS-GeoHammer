package com.ugcs.geohammer.map.provider;

import com.ugcs.geohammer.release.Version;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class Usgs3depTileProvider implements XyzTileProvider {

    private static final Logger log = LoggerFactory.getLogger(Usgs3depTileProvider.class);

    private static final double WEB_MERCATOR_HALF_WORLD = 20_037_508.342789244;

    private static final String IMAGE_SERVICE_URL = "https://elevation.nationalmap.gov/arcgis/rest/services/"
            + "3DEPElevation/ImageServer/exportImage";

    private static final String MULTIDIRECTIONAL_HILLSHADE_RULE = "%7B%22rasterFunction%22%3A"
            + "%22Hillshade%20Multidirectional%22%7D";

    private static final int CONNECT_TIMEOUT_MS = 5_000;

    private static final int READ_TIMEOUT_MS = 10_000;

    private final String userAgent;

    public Usgs3depTileProvider(Version appVersion) {
        userAgent = "GeoHammer/" + Version.toString(appVersion) + " (https://github.com/ugcs/geohammer)";
    }

    @Override
    public int maxConcurrentRequests() {
        return 2;
    }

    @Override
    public int getMaxZoom() {
        return 18;
    }

    @Override
    public String getCachePrefix() {
        return "usgs_3dep_multidirectional_hillshade";
    }

    @Nullable
    @Override
    public BufferedImage fetchTile(XyzTile tile) {
        String url = createUrl(tile);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.warn("USGS 3DEP hillshade service returned HTTP {} for {}", connection.getResponseCode(), url);
                return null;
            }
            try (var input = connection.getInputStream()) {
                return ImageIO.read(input);
            }
        } catch (IOException | URISyntaxException e) {
            log.warn("Failed to fetch USGS 3DEP hillshade tile {}", url, e);
            return null;
        }
    }

    private static String createUrl(XyzTile tile) {
        double tileWidth = 2.0 * WEB_MERCATOR_HALF_WORLD / (1 << tile.z());
        double minX = -WEB_MERCATOR_HALF_WORLD + tile.x() * tileWidth;
        double maxX = minX + tileWidth;
        double maxY = WEB_MERCATOR_HALF_WORLD - tile.y() * tileWidth;
        double minY = maxY - tileWidth;
        return String.format(Locale.ROOT, "%s?bbox=%.3f,%.3f,%.3f,%.3f&bboxSR=3857&size=256,256&imageSR=3857"
                        + "&format=png32&transparent=false&renderingRule=%s&f=image",
                IMAGE_SERVICE_URL, minX, minY, maxX, maxY, MULTIDIRECTIONAL_HILLSHADE_RULE);
    }
}
