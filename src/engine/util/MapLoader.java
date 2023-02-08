/*
 * Copyright MagicBane 2013
 */

package engine.util;

import engine.InterestManagement.RealmMap;
import engine.server.MBServerStatics;
import engine.server.world.WorldServer;
import org.pmw.tinylog.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public enum MapLoader {

    MAPLOADER;

    public static int[][] loadMap() {

        BufferedImage image;
        int[][] realmMap;
        long timeToLoad = System.currentTimeMillis();
        long bytesRead = 0;
        long realmsWritten = 0;
        int realmUUID;
        
        // Load image from disk
        
        try {
            image = ImageIO.read(new File(MBServerStatics.DEFAULT_DATA_DIR + "realmmap.png"));

            // Array size determined by image size
            MBServerStatics.SPATIAL_HASH_BUCKETSX = image.getWidth();
            MBServerStatics.SPATIAL_HASH_BUCKETSY = image.getHeight();
            realmMap = new int[MBServerStatics.SPATIAL_HASH_BUCKETSX][MBServerStatics.SPATIAL_HASH_BUCKETSY];
        } catch (IOException e) {
            Logger.error( "Error loading realm map: " + e.toString());
            return null;
        }

        // Flip image on the y axis
        
        image = flipImage(image);

        // Load spatial imageMap with color data from file

        for (int i = 0; i < MBServerStatics.SPATIAL_HASH_BUCKETSY; i++) {
            for (int j = 0; j < MBServerStatics.SPATIAL_HASH_BUCKETSX; j++) {
				try {
					int rgb = image.getRGB(j, i);
					realmUUID = RealmMap.getRealmIDByRGB(rgb);
                
                realmMap[j][i] = realmUUID;
                bytesRead++;

                if (realmUUID != 0)
                    realmsWritten++;

				}catch (Exception e){
					continue;
				}


            }
        }
        timeToLoad = System.currentTimeMillis() - timeToLoad;

        Logger.info( bytesRead + "Realm imageMNap pixels processed in " + timeToLoad / 1000 + " seconds");
        return realmMap;
    }

    public static BufferedImage flipImage(BufferedImage img) {
        
        int w = img.getWidth();
        int h = img.getHeight();
        
        BufferedImage dimg = new BufferedImage(w, h, img.getColorModel()
                .getTransparency());
        
        Graphics2D g = dimg.createGraphics();
        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
        g.dispose();
        return dimg;
    }
}
