package cz.vutbr.fit.xhalas10.bp.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.ObjectInputStream;

// http://earth-info.nga.mil/GandG/wgs84/gravitymod/egm96/egm96.html
// https://stackoverflow.com/questions/22196714/wgs84-geoid-height-altitude-offset-for-external-gps-data-on-ios
public class GeoidCalculator {
    private final static int l_value = 65341;
    private final static int _361 = 361;
    private static int nmax = 360;
    private static double currentHeight;
    private static double[] cc/* = new double[l_value + 1]*/;
    private static double[] cs/* = new double[l_value + 1]*/;
    private static double[] hc/* = new double[l_value + 1]*/;
    private static double[] hs/* = new double[l_value + 1]*/;
    private static double[] p = new double[l_value + 1];
    private static double[] sinml = new double[_361 + 1];
    private static double[] cosml = new double[_361 + 1];
    private static double[] rleg = new double[_361 + 1];

    /*constants for wgs84(g873);gm in units of m**3/s**2*/
    private final static double gm = .3986004418e15, ae = 6378137.;

    /*the even degree zonal coefficients given below were computed for the
     wgs84(g873) system of constants and are identical to those values
     used in the NIMA gridding procedure. computed using subroutine
     grs written by N.K. PAVLIS*/
    private final static double j2 = 0.108262982131e-2, j4 = -.237091120053e-05, j6 = 0.608346498882e-8, j8 = -0.142681087920e-10, j10 = 0.121439275882e-13;


    private static double[] drts = new double[1301];
    private static double[] dirt = new double[1301];
    private static double[] rlnn = new double[_361+ 1];
    static double cothet, sithet;
    static int ir = 0;


    private final static double a = 6378137., e2 = .00669437999013, geqt = 9.7803253359, k = .00193185265246;

    private static final GeoidCalculator ourInstance = new GeoidCalculator();

    public static GeoidCalculator getInstance() {
        return ourInstance;
    }

    private GeoidCalculator() {
        synchronized(this) {
            try {
                // Try to load before generated data with init_arrays() and then serialized to *.dat files.
                FileHandle file = Gdx.files.internal("geoid_data/cc.dat");
                ObjectInputStream ois = new ObjectInputStream(file.read());
                cc = (double[])ois.readObject();

                file = Gdx.files.internal("geoid_data/cs.dat");
                ois = new ObjectInputStream(file.read());
                cs = (double[])ois.readObject();

                file = Gdx.files.internal("geoid_data/hc.dat");
                ois = new ObjectInputStream(file.read());
                hc = (double[])ois.readObject();

                file = Gdx.files.internal("geoid_data/hs.dat");
                ois = new ObjectInputStream(file.read());
                hs = (double[])ois.readObject();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double getHeightFromLatAndLon(double lat, double lon) {
        updatePositionWithLatitudeAndLongitude(lat, lon);
        return getCurrentHeightOffset();
    }

    private double getCurrentHeightOffset() {
        return currentHeight;
    }

    private void updatePositionWithLatitudeAndLongitude(double lat, double lon) {
        /*compute the geocentric latitude,geocentric radius,normal gravity*/
        double u = undulation(Math.toRadians(lat), Math.toRadians(lon), nmax, nmax + 1);

        /*u is the geoid undulation from the egm96 potential coefficient model
           including the height anomaly to geoid undulation correction term
           and a correction term to have the undulations refer to the
           wgs84 ellipsoid. the geoid undulation unit is meters.*/
        currentHeight = u;
    }

    private double hundu(long nmax, double gr, double re) {
        double arn, ar, ac, a, b, sum, sumc, sum2, tempc, temp;
        int k, n, m;
        ar = ae / re;
        arn = ar;
        ac = a = b = 0;
        k = 3;
        for (n = 2; n <= nmax; n++) {
            arn *= ar;
            k++;
            sum = p[k] * hc[k];
            sumc = p[k] * cc[k];
            sum2 = 0;
            for (m = 1; m <= n; m++) {
                k++;
                tempc = cc[k] * cosml[m] + cs[k] * sinml[m];
                temp = hc[k] * cosml[m] + hs[k] * sinml[m];
                sumc += p[k] * tempc;
                sum += p[k] * temp;
            }
            ac += sumc;
            a += sum * arn;
        }
        ac += cc[1] + p[2] * cc[2] + p[3] * (cc[3] * cosml[1] + cs[3] * sinml[1]);
/*add haco=ac/100 to convert height anomaly on the ellipsoid to the undulation
add -0.53m to make undulation refer to the wgs84 ellipsoid.*/
        return a * gm / (gr * re) + ac / 100 - .53;
    }

    private void dscml(double rlon, long nmax) {
        double a, b;
        int m;
        a = Math.sin(rlon);
        b = Math.cos(rlon);
        sinml[1] = a;
        cosml[1] = b;
        sinml[2] = 2 * b * a;
        cosml[2] = 2 * b * b - 1;
        for (m = 3; m <= nmax; m++) {
            sinml[m] = 2 * b * sinml[m - 1] - sinml[m - 2];
            cosml[m] = 2 * b * cosml[m - 1] - cosml[m - 2];
        }
    }

    private void legfdn(long m, double theta, long nmx)
/*this subroutine computes  all normalized legendre function
in "rleg". order is always
m, and colatitude is always theta  (radians). maximum deg
is  nmx. all calculations in double precision.
ir  must be set to zero before the first call to this sub.
the dimensions of arrays  rleg must be at least equal to  nmx+1.
Original programmer :Oscar L. Colombo, Dept. of Geodetic Science
the Ohio State University, August 1980
ineiev: I removed the derivatives, for they are never computed here*/
    {
        long nmx1 = nmx + 1, nmx2p = 2 * nmx + 1, m1 = m + 1, m2 = m + 2, m3 = m + 3, n, n1, n2;
        if (ir == 0) {
            ir = 1;
            for (n = 1; n <= nmx2p; n++) {
                drts[(int)n] = Math.sqrt(n);
                dirt[(int)n] = 1 / drts[(int)n];
            }
        }
        cothet = Math.cos(theta);
        sithet = Math.sin(theta);
        /*compute the legendre functions*/
        rlnn[1] = 1;
        rlnn[2] = sithet * drts[3];
        for (n1 = 3; n1 <= m1; n1++) {
            n = n1 - 1;
            n2 = 2 * n;
            rlnn[(int)n1] = drts[(int)n2 + 1] * dirt[(int)n2] * sithet * rlnn[(int)n];
        }
        switch ((int)m) {
            case 1:
                rleg[2] = rlnn[2];
                rleg[3] = drts[5] * cothet * rleg[2];
                break;
            case 0:
                rleg[1] = 1;
                rleg[2] = cothet * drts[3];
                break;
        }
        rleg[(int)m1] = rlnn[(int)m1];
        if (m2 <= nmx1) {
            rleg[(int)m2] = drts[(int)m1 * 2 + 1] * cothet * rleg[(int)m1];
            if (m3 <= nmx1)
                for (n1 = m3; n1 <= nmx1; n1++) {
                    n = n1 - 1;
                    if ((m == 0 && n < 2) || (m == 1 && n < 3))
                        continue;
                    n2 = 2 * n;
                    rleg[(int)n1] = drts[(int)n2 + 1] * dirt[(int)n + (int)m] * dirt[(int)n - (int)m] *
                            (drts[(int)n2 - 1] * cothet * rleg[(int)n1 - 1] - drts[(int)n + (int)m - 1] * drts[(int)n - (int)m - 1] * dirt[(int)n2 - 3] * rleg[(int)n1 - 2]);
                }
        }
    }

    private void radgra(double lat, double lon, double[] rlat, double[] gr, double[] re)
/*this subroutine computes geocentric distance to the point,
the geocentric latitude,and
an approximate value of normal gravity at the point based
the constants of the wgs84(g873) system are used*/
    {
        double n, t1 = Math.sin(lat) * Math.sin(lat), t2, x, y, z;
        n = a / Math.sqrt(1 - e2 * t1);
        t2 = n * Math.cos(lat);
        x = t2 * Math.cos(lon);
        y = t2 * Math.sin(lon);
        z = (n * (1 - e2)) * Math.sin(lat);
        re[0] = Math.sqrt(x * x + y * y + z * z);/*compute the geocentric radius*/
        rlat[0] = Math.atan(z / Math.sqrt(x * x + y * y));/*compute the geocentric latitude*/
        gr[0] = geqt * (1 + k * t1) / Math.sqrt(1 - e2 * t1);/*compute normal gravity:units are m/sec**2*/
    }


    private double undulation(double lat, double lon, int nmax, int k) {
        double[] rlat = new double[1];
        double[] gr = new double[1];
        double[] re = new double[1];
        int i, j, m;
        radgra(lat, lon, rlat, gr, re);
        rlat[0] = Math.PI / 2.0 - rlat[0];
        for (j = 1; j <= k; j++) {
            m = j - 1;
            legfdn(m, rlat[0], nmax);
            for (i = j; i <= k; i++)
                p[(i - 1) * i / 2 + m + 1] = rleg[i];
        }
        dscml(lon, nmax);
        return hundu(nmax, gr[0], re[0]);
    }
}
