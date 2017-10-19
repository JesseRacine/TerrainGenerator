/*
 * This class is responsible for generating a divide-and-displace random terrain image
 * using the Diamond Square algorithm.  See the other diamond square implementation
 * for better commenting. 
 * 
 * Note, the array that this algorithm works is setup to produce doubles from
 * 	-1 to 1
 * 
 */

package fractal;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.util.Date;
import java.util.Random;

public class DisplaceFractal {
    private int size;

    private Random rand;

    public DisplaceFractal(int size) {
        long time = (new Date()).getTime();
        rand = new Random(time);

        this.size = size;
    }

    public static class DisplaceSettings {
        public int roughness, mountainSize, contrast;
        public boolean postSmooth;

		/* This is a helper class that is used to pass some variables around in this
		 * class and also between the User Interface panel and this class.
		 * 
		 * 
		 * roughness - controls the "H" parameter as some references call it.  This controls
		 * the value of the parameter that decreases the amount of variation in the terrain
		 * heights through each round of iteration.
		 * 
		 * mountainSize - controls the initial starting parameter of the value that is used
		 * to "perturb" the heights in the system.  Essentially, this puts another restriction
		 * on the value of the randomization on the points on terrain.
		 * 
		 * contrast - this number is turned into a multiplier that effects the final image
		 * only.  It is used to brighten up or darken down the final pixels that make
		 * up the image.
		 */
    }

    public WritableImage render(DisplaceSettings settings) {
        long time = (new Date()).getTime();
        rand = new Random(time);

        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();

        //BufferedImage image = new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);


        // Init the heightfield with numbers used to represent that it is empty

        double[][] heightField = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                heightField[i][j] = 500; // Arbitrary blank indicaor
            }
        }

        // Read the mountainSize variable from the settings structure and then turn
        // it into a double which forms a nice multiplier.

        double maxHeight = (double) settings.mountainSize / 20.0;

        // Now, intialize the four corners of the array with random heights, taking
        // care to limit them with the maxHeight value

        heightField[0][0] = rand.nextDouble() * maxHeight;
        heightField[0][size - 1] = rand.nextDouble() * maxHeight;
        heightField[size - 1][0] = rand.nextDouble() * maxHeight;
        heightField[size - 1][size - 1] = rand.nextDouble() * maxHeight;


        // Now, run the recursive method (although actually tail-recursion) that starts
        // the generation of the image
        makeSession(size - 1, heightField, 1, maxHeight, settings);

        // Image now made, check to see if it should be smoothed

        if (settings.postSmooth) {
            double[][] smooth = new double[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    smooth[j][i] = smoothNoise(j, i, heightField);
                }
            }
            heightField = smooth;
        }

        double color;

        // Read in the contrast variable from settings and turn it into a double
        // that will be used as a multiplier (divider actually)
        // on the final pixel values in the array

        double colorMultiplier = (double) (settings.contrast / 100.0);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // The color values in the array are between -1 and 1
                color = heightField[j][i] / colorMultiplier;

                color = color * 127 + 127;

                int aColor = (int) color;

                if (aColor > 255)
                    aColor = 255;
                else if (aColor < 0)
                    aColor = 0;

                // Set the pixel value in the image

                writer.setColor(j, i, javafx.scene.paint.Color.grayRgb(aColor));
            }
        }
        return image;
    }

    private void makeSession(int width, double[][] heightField, int repeats, double level, DisplaceSettings settings) {
        // For each iteration, make a set of center-square points, then make a set
        // of diamond points (edge-centers), and then recurse again.  Each time it recurses
        // the number of points made will increase by a factor of 4 (represented by
        // the integer repeats

        if (width == 1) // iterations can stop
            return;

        int left = 0, top = 0;

        for (int i = 0; i < repeats; i++) {
            makeSquareHeight(new Point(left, top), width, heightField, level);

            left += width;

            if (left == size - 1) {
                left = 0;
                top += width;
            }
        }

        left = 0;
        top = 0;
        for (int i = 0; i < repeats; i++) {
            makeDiamonds(new Point(left, top), width, heightField, level);

            left += width;

            if (left == size - 1) {
                left = 0;
                top += width;
            }
        }

        level = level * ((double) settings.roughness / 20.0);

        makeSession(width / 2, heightField, repeats * 4, level, settings);
    }

    private void makeSquareHeight(Point p, int width, double[][] heightField, double level) {
        // This averages the heights of the four points of the square represented by the
        // corner at Point p and the int width.  It then takes the average, adds a random
        // perturbation and then sets the value into the array at the midpoint
        // of the square

        if (heightField[p.x + width / 2][p.y + width / 2] < 2) // if position already has number
            return;

        double height = heightField[p.x][p.y] + heightField[p.x + width][p.y]
                + heightField[p.x + width][p.y + width] + heightField[p.x][p.y + width];

        height = height / 4;

        heightField[p.x + width / 2][p.y + width / 2] = randomColor(height, level);
    }

    private void makeDiamondHeight(Point p, int width, double[][] heightField, double level) {
        // This method takes in the coordinate Point p which represents a spot in the array
        // that a height value has to be put it.

        // It then locates the four surrounding edges (the diamond points) and averages them
        // up to figure out what height value should be put at Point p.

        // If an average point falls off the edge of the array it is ignored, and not
        // wrapped around.

        if (heightField[p.x][p.y] < 2) // This is true if there is a value
            return;                    // already in the array location P, so no value
        // needs to be put there

        double height = 0;
        int total = 0;

        int xc, yc;

        xc = p.x - width / 2;
        yc = p.y;

        if (xc > -1) {
            total++;
            height += heightField[xc][yc];
        }

        xc = p.x;
        yc = p.y - width / 2;

        if (yc > -1) {
            total++;
            height += heightField[xc][yc];
        }

        xc = p.x + width / 2;
        yc = p.y;

        if (xc < size) {
            total++;
            height += heightField[xc][yc];
        }

        xc = p.x;
        yc = p.y + width / 2;

        if (yc < size) {
            total++;
            height += heightField[xc][yc];
        }

        // the diamond average
        height = height / total;

        // Place the diamond average, plus a random perturbation, in the array location P
        heightField[p.x][p.y] = randomColor(height, level);
    }

    private void makeDiamonds(Point p, int width, double[][] heightField, double level) {
        // This method calls the makeDiamondHeight four times, although this is
        // not recursive since each makeDiamondHeight run finishes up the first time through.

        // This method locates the four diamond points, indentified by Point p
        // and the int width.  The diamond points are the four edge-centers of this
        // square.  It then tells each makeDiamondHeight to find the averages for those
        // four edge-centers and put the proper values in the array

        int dx, dy;

        dx = p.x + width / 2;
        dy = p.y;

        makeDiamondHeight(new Point(dx, dy), width, heightField, level);

        dx = p.x + width;
        dy = p.y + width / 2;

        makeDiamondHeight(new Point(dx, dy), width, heightField, level);

        dx = p.x + width / 2;
        dy = p.y + width;

        makeDiamondHeight(new Point(dx, dy), width, heightField, level);

        dx = p.x;
        dy = p.y + width / 2;

        makeDiamondHeight(new Point(dx, dy), width, heightField, level);

    }

    private double randomColor(double heightColor, double level) {
        // This method takes in the double heightColor, which represents a height value
        // that is about to be put in a location in the array.  It generates a random
        // perturbation, adds it to the heightColor, and then returns the final value

        // double level is the maximum random value that should be generated

        // normally this would be -1 to 1 but as the algorithm progresses this range
        // gets smaller and smaller


        double change = rand.nextDouble();

        change = change * level;     // multiply by level to reduce the range of randomness

        if (rand.nextInt(100) < 50)
            change = -change;

        change = heightColor + change;

        if (change < -1)
            change = -1;
        if (change > 1)
            change = 1;

        return change;
    }

    private double smoothNoise(int x, int y, double[][] noise) {

        // This simply takes the array coordinates surrounding x, y, and adds them all
        // together and make a weighted average.  This basically smooths out the
        // pixel x,y and returns the new smoothed out value

        int x1 = (x + size) % size;
        int y1 = (y + size) % size;

        int x2 = (x1 + size - 1) % size;
        int y2 = (y1 + size - 1) % size;

        int x3 = (x1 + size + 1) % size;
        int y3 = (y1 + size + 1) % size;

        int smooth1 = 4;
        int smooth2 = 8;
        int smooth3 = 16;

        double corners = (noise[x2][y2] + noise[x3][y2] + noise[x2][y3] + noise[x3][y3])
                / smooth3;
        double sides = (noise[x2][y1] + noise[x3][y1] + noise[x1][y2] + noise[x1][y3])
                / smooth2;
        double center = noise[x1][y1] / smooth1;

        return corners + sides + center;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
