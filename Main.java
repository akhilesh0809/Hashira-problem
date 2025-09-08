import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
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
        ArrayList<BigDecimal> xs = new ArrayList<>();
        ArrayList<BigDecimal> ys = new ArrayList<>();

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

            xs.add(BigDecimal.valueOf(i));
            ys.add(new BigDecimal(decimalValue));
        }

        int degree = k - 1;
        BigDecimal[][] A = new BigDecimal[degree + 1][degree + 2];
        MathContext mc = new MathContext(50); 
        for (int row = 0; row <= degree; row++) {
            for (int col = 0; col <= degree; col++) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int i = 0; i < xs.size(); i++) {
                    sum = sum.add(xs.get(i).pow(row + col, mc), mc);
                }
                A[row][col] = sum;
            }
            BigDecimal sumY = BigDecimal.ZERO;
            for (int i = 0; i < xs.size(); i++) {
                sumY = sumY.add(ys.get(i).multiply(xs.get(i).pow(row, mc), mc), mc);
            }
            A[row][degree + 1] = sumY;
        }

        BigDecimal[] coeffs = gaussianSolveBig(A, degree + 1, mc);
        System.out.println("Constant term (c) of the polynomial = " + coeffs[0].toPlainString());
    }


    static BigDecimal[] gaussianSolveBig(BigDecimal[][] A, int n, MathContext mc) {
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (A[k][i].abs().compareTo(A[maxRow][i].abs()) > 0) {
                    maxRow = k;
                }
            }
            BigDecimal[] temp = A[i]; A[i] = A[maxRow]; A[maxRow] = temp;

            for (int k = i + 1; k < n; k++) {
                if (A[i][i].compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal factor = A[k][i].divide(A[i][i], mc);
                for (int j = i; j <= n; j++) {
                    A[k][j] = A[k][j].subtract(factor.multiply(A[i][j], mc), mc);
                }
            }
        }
        BigDecimal[] x = new BigDecimal[n];
        for (int i = n - 1; i >= 0; i--) {
            BigDecimal sum = A[i][n];
            for (int j = i + 1; j < n; j++) {
                sum = sum.subtract(A[i][j].multiply(x[j], mc), mc);
            }
            x[i] = sum.divide(A[i][i], mc);
        }
        return x;
    }
}
