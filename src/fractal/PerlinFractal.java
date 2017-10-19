/* This class represents the Perlin Fractal generation code

 It provides two different ways of fractal implementation:  The "real" perlin way
 and the fake way.  Both methods can be referenced by the included .html files.

 The real Perlin way generates a fractal image by using somewhat complex trigonometric
 math involving vectors.
 
 The fake way involves taking a noisy, snowy base image and selecting pixels from that.
 
 Then you interpolate between those pixels.  You then take a different set of pixels from
 the same noisy image and interpolate again.  You then add the two images together.
 
 The amount of images generated for blending together is selectable via the blending
 slider in the UI.

*/

package fractal;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Date;
import java.util.Random;

public class PerlinFractal {
    private Random rand;

    private int size;

    private double[][] noise, xNoise, yNoise;

    private static final int LINEAR = 0, COSINE = 1, CUBIC = 2, STANDARD = 3;

    public class DoubleVector {
        public double x, y;

        public DoubleVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class PerlinSettings {
        // This helper class contains a bunch of settings that
        // are passed between the User Interface panel and this fractal generation class.
        // The settings are also transferred between private methods to avoid the use
        // of too many method arguments
        public int blends, interp, maxBright, freqReduc;

        public boolean preSmooth, postSmooth;

		/*  blends - how many different fractals should be created and blended together
		 * 		to create the final image
		 * 
		 * interp - The type of interpolation used to generate this image, review 
		 * the static final int declaration at the top for more info
		 * 
		 * maxBright - a multiplier scalar used to change the final image and increase
		 * 	or decrease its brightness
		 * 
		 * freqReduc - Normally, when you make an image, with say blend set at 8, you will
		 * get four images made:  8, 4, 2, and 1.  If you mess with freqReduc it will
		 * cut out the "high frequency" images, like the 1, 2 pixel wide noise alignments
		 *  	Essentially, it reduces the amount of blends made by cutting out the higher
		 * 		frequency blends, the ones that make the terrain more rough.
		 * 
		 * preSmooth - This only effects the main interpolation types, not the "standard"
		 * type.  Anyway, the Perlin image is generated by first making a noisy, snowy
		 * image.  If preSmooth is true, this noisy snowy image will be smoothed out
		 * before being read by the various interpolation functions.
		 * 
		 * postSmooth - This option, if true, simply smooths the final result image.
		 * 
		 * 
		 */
    }

    public PerlinFractal(int size) {
        this.size = size;
        long time = (new Date()).getTime();
        rand = new Random(time);

        // this "seeds" the fractal
        noise = new double[size][size];  // Starting noise image for main interpolation types

        xNoise = new double[size][size]; // These two noise images are required for Mr. Perlin's
        yNoise = new double[size][size]; // own implementation.  Two are required because they
        // represent vectors.
    }

    public WritableImage render(PerlinSettings settings) {
        // This method starts everything going.

        seedNoise();  // Fill the noise arrays with random junk

        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();

		/* take maxBright from the settings and turn it into a double (with a ratio)
		 that can be used as a multiplier to change the final pixel color value
		
		 *** Note:  This variable has a very unfortunate name from earlier on in the
		 programming of this code.  It has nothing to do with maximum anything.
		 All it does is change the final pixel color values.  If it is higher 
		 than 1.0 it will increase the brightness of the pixel, and if it is less than 1.0
		 it will decrease the brightness.
		 */

        double maxBright = (double) 100.0 / settings.maxBright;

        // Use array "arr" and store an intermediate copy of the perlin noise values
        // so that smoothed values can be made out of the final perlin noise array

        double[][] arr = new double[size][size];

        if (settings.postSmooth) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    arr[j][i] = perlinNoise(j, i, settings);
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int aColor = 0;
                if (settings.interp == STANDARD) {
                    // Perlin's standard method results in doubles between -1 and 1
                    aColor = (int) (perlinNoise(j, i, settings) * 127 + 127);

                    // now take the color value and modify it with maxBright
                    aColor = (int) ((double) aColor * maxBright);
                } else {
                    if (settings.postSmooth)
                        aColor = (int) (255.0 * smoothNoise(j, i, arr) * (double) maxBright);
                    else
                        aColor = (int) (255.0 * perlinNoise(j, i, settings) * (double) maxBright);
                }

                if (aColor > 255)
                    aColor = 255;

                if (aColor < 0)
                    aColor = 0;

                writer.setColor(j, i, Color.grayRgb(aColor));

            }
        }

        return image;
    }

    private double linearInterpolate(double a, double b, double x) {
        //	 Perform linear interpolation on the points (this code came from an html site)
        return a * (1 - x) + b * x;
    }

    private double cosineInterpolate(double a, double b, double x) {
        //	 Perform cosine interpolation on the points (this code came from an html site)
        double ft = x * Math.PI;
        double f = (1 - Math.cos(ft)) * 0.5;
        return a * (1 - f) + b * f;
    }

    private double cubicInterpolate(double v0, double v1, double v2, double v3, double x) {
        // Perform cubic interpolation on the points (this code came from an html site)
        double p = (v3 - v2) - (v0 - v1);
        double q = (v0 - v1) - p;
        double r = v2 - v0;
        double s = v1;

        return p * x * x * x + q * x * x + r * x + s;
    }

    private void seedNoise() {
        // Fill the three noise arrays with random junk using Java's built in Random class

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                noise[j][i] = rand.nextDouble();

        // Now seed the noise for the "original" Perlin method

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                xNoise[j][i] = rand.nextDouble();

                if (rand.nextDouble() < 0.5)
                    xNoise[j][i] = -xNoise[j][i];

                yNoise[j][i] = rand.nextDouble();

                if (rand.nextDouble() < 0.5)
                    yNoise[j][i] = -yNoise[j][i];
            }

    }

    private DoubleVector randVector(int x, int y) {
		/* This method is used by the code that implements Mr. Perlins original method
		 * of random image making.  His method requires the generation of random
		 * 2d vectors.  These vectors are generated and stored in arrays xNoise, and yNoise
		 * at another point in the program.  So, the x coordinate for a vector at point
		 * x, y is at (xNoise[x][y]) and the y coordinate for the vector is at  (yNoise[x][y])
		 * 
		 * Basically, for every array point there needs to be two values associated with that 
		 * point.
		 * 
		 * Anyway, all this method looks at the vector values in the two arrays and normalizes them
		 * 
		 * so they have a length of one.  It then returns the normalized vector value.
		 * 
		 * This step could have easily been done at a point earlier in the program.
		 * 
		 * This is an unfortunate use of code in this program.
		 */

        double xx = xNoise[x][y];
        double yy = yNoise[x][y];

        double hyp = Math.sqrt(xx * xx + yy * yy);

        xx = xx / hyp;

        yy = yy / hyp;

        return new DoubleVector(xx, yy);

    }

    private double perlinNoise(int x, int y, PerlinSettings settings) {
        // This function calculates the color of the final image for
        // coordinate x, y of the image

        double divide = 2;

        int blends = (int) (Math.pow(2.0, (double) settings.blends));

        double stopper;

        if (settings.freqReduc == 0)
            stopper = 0;
        else if (settings.freqReduc == 1)
            stopper = 1.0 / 32;
        else if (settings.freqReduc == 2)
            stopper = 1.0 / 16;
        else if (settings.freqReduc == 3)
            stopper = 1.0 / 8;
        else if (settings.freqReduc == 4)
            stopper = 1.0 / 4;
        else
            stopper = 1.0 / 2;

        int stopLoop = (int) (stopper * (double) blends);

        double value = 0; // return value (color)

        while (blends > stopLoop) {
            if (settings.interp == STANDARD)
                value += (originalNoise((double) x / blends, (double) y
                        / blends) / divide);
            else
                value += (interpolate((double) x / blends, (double) y / blends,
                        settings) / divide);

            blends /= 2;
            divide *= 2;
        }

        return value;
    }

    private double interpolate(double x, double y, PerlinSettings settings) {

        // The cubic interpolation code is so large that it had to be put in its own
        // method
        if (settings.interp == CUBIC)
            return cubicCalculations(x, y, settings);

        // Take the double x,y, and convert to integer array coordinates.

        // Make sure that the array coords do not fall off the end of the array
        // with the modulus % operator

        int x1 = ((int) x + size) % size;
        int y1 = ((int) y + size) % size;

        int x2 = (x1 + size + 1) % size;
        int y2 = (y1 + size + 1) % size;

        double fractx = x - Math.floor(x);
        double fracty = y - Math.floor(y);

        double n1, n2, n3, n4;

        if (settings.preSmooth) {
            n1 = smoothNoise(x1, y1, noise);
            n2 = smoothNoise(x2, y1, noise);
            n3 = smoothNoise(x1, y2, noise);
            n4 = smoothNoise(x2, y2, noise);
        } else {
            n1 = noise[x1][y1];
            n2 = noise[x2][y1];
            n3 = noise[x1][y2];
            n4 = noise[x2][y2];
        }

        double i1, i2;

        switch (settings.interp) {
            case LINEAR:
                i1 = linearInterpolate(n1, n2, fractx);
                i2 = linearInterpolate(n3, n4, fractx);
                return linearInterpolate(i1, i2, fracty);

            case COSINE:
                i1 = cosineInterpolate(n1, n2, fractx);
                i2 = cosineInterpolate(n3, n4, fractx);
                return cosineInterpolate(i1, i2, fracty);
        }

        return 0;
    }

    private double cubicCalculations(double x, double y, PerlinSettings settings) {
		/*  Perform cubic interpolation on the point x, y from the noise[][] array
		 * 
		 * It uses the cubic interpolation function from the html website that comes
		 * 
		 * with this code.  The exact implementation of which points are used to perform
		 * 
		 * the interpolation is too complicated to be explained.  It was figured out
		 * 
		 * by using the same pattern of interpolation points used for the linear
		 * 
		 * and cosine interpolation methods on the html info
		 * 
		 */

        double fractx = x - Math.floor(x);
        double fracty = y - Math.floor(y);

        int x1 = ((int) x + size - 1) % size;
        int x2 = ((int) x + size) % size;
        int x3 = ((int) x + size + 1) % size;
        int x4 = ((int) x + size + 2) % size;

        int y1 = ((int) y + size - 1) % size;
        int y2 = ((int) y + size) % size;
        int y3 = ((int) y + size + 1) % size;
        int y4 = ((int) y + size + 2) % size;

        // Cover the y-1 line x grouping

        double n1, n2, n3, n4;

        if (settings.preSmooth) {
            n1 = smoothNoise(x1, y1, noise);
            n2 = smoothNoise(x2, y1, noise);
            n3 = smoothNoise(x3, y1, noise);
            n4 = smoothNoise(x4, y3, noise);
        } else {
            n1 = noise[x1][y1];
            n2 = noise[x2][y1];
            n3 = noise[x3][y1];
            n4 = noise[x4][y1];
        }

        // Cover the y line x grouping

        double n5, n6, n7, n8;

        if (settings.preSmooth) {
            n5 = smoothNoise(x1, y2, noise);
            n6 = smoothNoise(x2, y2, noise);
            n7 = smoothNoise(x3, y2, noise);
            n8 = smoothNoise(x4, y2, noise);
        } else {
            n5 = noise[x1][y2];
            n6 = noise[x2][y2];
            n7 = noise[x3][y2];
            n8 = noise[x4][y2];
        }

        // Cover the y+1 line x grouping

        double n9, n10, n11, n12;

        if (settings.preSmooth) {
            n9 = smoothNoise(x1, y3, noise);
            n10 = smoothNoise(x2, y3, noise);
            n11 = smoothNoise(x3, y3, noise);
            n12 = smoothNoise(x4, y3, noise);
        } else {
            n9 = noise[x1][y3];
            n10 = noise[x2][y3];
            n11 = noise[x3][y3];
            n12 = noise[x4][y3];
        }

        // Cover the y+2 line x grouping

        double n13, n14, n15, n16;

        if (settings.preSmooth) {
            n13 = smoothNoise(x1, y4, noise);
            n14 = smoothNoise(x2, y4, noise);
            n15 = smoothNoise(x3, y4, noise);
            n16 = smoothNoise(x4, y4, noise);
        } else {
            n13 = noise[x1][y4];
            n14 = noise[x2][y4];
            n15 = noise[x3][y4];
            n16 = noise[x4][y4];
        }

        double i1 = cubicInterpolate(n1, n2, n3, n4, fractx);
        double i2 = cubicInterpolate(n5, n6, n7, n8, fractx);
        double i3 = cubicInterpolate(n9, n10, n11, n12, fractx);
        double i4 = cubicInterpolate(n13, n14, n15, n16, fractx);

        return cubicInterpolate(i1, i2, i3, i4, fracty);
    }

    private double smoothNoise(int x, int y, double[][] noise) {
        // Smooth array point x, y, in the array noise[][]

        // It is important to note that the argument noise has the same name
        // as a class variable, noise, of the exact same type.  This is on accident.

        // The noise argument here overrides the class variable noise

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

    private double originalNoise(double x, double y) {
        // This code calculates PerlinNoise using the original implementation
        // by Mr. Perlin - refer to the website html that comes with this program

        // ---------------------------------------

        // Make the four pseudo-random gradients and coordinate point
        // Subtraction
        // Vectors

        int xInt = (int) x;
        int yInt = (int) y;

        // Make left top gradient
        DoubleVector bottomLeft = randVector(xInt, yInt);
        double x0 = x - xInt;
        double y0 = y - yInt;

        // Make top right gradient (which effectively rounds up the xdouble)
        xInt = (xInt + size + 1) % size;
        DoubleVector bottomRight = randVector(xInt, yInt);
        // Make subtractive vector
        double x1 = x - xInt;

        // Make bottom right gradient (which rounds up ydouble
        yInt = (yInt + size + 1) % size;
        DoubleVector topRight = randVector(xInt, yInt);
        // Make subtractive vector
        double y1 = y - yInt;

        // Make bottom left gradient
        xInt = (xInt + size - 1) % size;
        DoubleVector topLeft = randVector(xInt, yInt);
        // Make subtractive vector

        // ------------------------------------------
        // Compute dot products

        double s, t, u, v;

        s = bottomLeft.x * x0 + bottomLeft.y * y0;
        t = bottomRight.x * x1 + bottomRight.y * y0;
        u = topLeft.x * x0 + topLeft.y * y1;
        v = topRight.x * x1 + topRight.y * y1;

        // ---------------------------------------

        // Compute weighted averages

        double sX = (3 * x0 * x0) - 2 * x0 * x0 * x0;
        double a = s + sX * (t - s);
        double b = u + sX * (v - u);

        double sY = (3 * y0 * y0) - 2 * y0 * y0 * y0;

        return a + sY * (b - a);
    }

    public void setSize(int size) {
        this.size = size;
        noise = new double[size][size];
        xNoise = new double[size][size];
        yNoise = new double[size][size];
    }

}