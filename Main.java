import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("sample2.json")));
        json = json.replaceAll("\\s+", "");

        int nIndex = json.indexOf("\"n\":");
        int n = Integer.parseInt(json.substring(nIndex + 4, json.indexOf(",", nIndex)));

        int kIndex = json.indexOf("\"k\":");
        int k = Integer.parseInt(json.substring(kIndex + 4, json.indexOf("}", kIndex)));

        ArrayList<Double> xs = new ArrayList<>();
        ArrayList<Double> ys = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            String key = String.valueOf(i);
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1) continue;

            int baseIndex = json.indexOf("\"base\":", keyIndex);
            String baseStr = json.substring(baseIndex + 7, json.indexOf(",", baseIndex)).replace("\"", "");
            int base = Integer.parseInt(baseStr);

            int valueIndex = json.indexOf("\"value\":", keyIndex);
            int endIndex = json.indexOf("}", valueIndex);
            String valueStr = json.substring(valueIndex + 8, endIndex).replace("\"", "");

            BigInteger decimalValue = new BigInteger(valueStr, base);

            xs.add(Double.valueOf(key));
            ys.add(decimalValue.doubleValue());
        }

        int degree = k - 1;
        double[][] A = new double[degree + 1][degree + 2];

        for (int row = 0; row <= degree; row++) {
            for (int col = 0; col <= degree; col++) {
                double sum = 0;
                for (int i = 0; i < xs.size(); i++) {
                    sum += Math.pow(xs.get(i), row + col);
                }
                A[row][col] = sum;
            }
            double sumY = 0;
            for (int i = 0; i < xs.size(); i++) {
                sumY += ys.get(i) * Math.pow(xs.get(i), row);
            }
            A[row][degree + 1] = sumY;
        }

        double[] coeffs = gaussianSolve(A, degree + 1);
        System.out.printf("Constant term (c) of the polynomial = %.12f%n", coeffs[0]);
    }

    static double[] gaussianSolve(double[][] A, int n) {
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) {
                    maxRow = k;
                }
            }
            double[] temp = A[i]; A[i] = A[maxRow]; A[maxRow] = temp;

            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                for (int j = i; j <= n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
            }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = A[i][n] / A[i][i];
            for (int k = i - 1; k >= 0; k--) {
                A[k][n] -= A[k][i] * x[i];
            }
        }
        return x;
    }
}
