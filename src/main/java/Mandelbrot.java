import java.awt.*;

public class Mandelbrot {

    // return number of iterations to check if c = a + ib is in Mandelbrot set
    public static int mand(Complex z0, int max) {
        Complex z = z0;
        for (int t = 0; t < max; t++) {
            if (z.abs() > 2.0) return t;
            z = z.times(z).plus(z0);
        }
        return max;
    }


//    public static int m3(Complex z0, int max) {
//        Complex z = new Complex(z0.r(), z0.i());
//        for (int t = 0; t < max; t++) {
//            if (z.module() > 2.0) return t;
//            z = z.sq().$plus(z0);
//        }
//        return max;
//    }

    public static void main(String[] args) {
        double xc = 0;//Double.parseDouble(args[0]);
        double yc = 0;//Double.parseDouble(args[1]);
        double size = 3;//Double.parseDouble(args[2]);

        int n = 512;   // create n-by-n image
        int max = 255;   // maximum number of iterations

        Picture picture = new Picture(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x0 = xc - size / 2 + size * i / n;
                double y0 = yc - size / 2 + size * j / n;
                Complex z0 = new Complex(x0, y0);
                int gray = max - mand(z0, max);
//                int gray = max - m3(new Mandelbrot.Complex(x0, y0), max);
                Color color = new Color(gray, gray, gray);
                picture.set(i, n - 1 - j, color);
            }
        }
        picture.show();
    }
}