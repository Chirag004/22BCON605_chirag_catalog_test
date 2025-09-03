import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;

public class PolynomialConstantSolver {

    /**
     * A simple Fraction class to handle rational number arithmetic with BigIntegers
     * for perfect precision.
     */
    static class Fraction {
        BigInteger num; // Numerator
        BigInteger den; // Denominator

        public Fraction(BigInteger numerator, BigInteger denominator) {
            if (denominator.equals(BigInteger.ZERO)) {
                throw new IllegalArgumentException("Denominator cannot be zero.");
            }
            // Simplify the fraction by dividing by the greatest common divisor
            BigInteger gcd = numerator.gcd(denominator);
            this.num = numerator.divide(gcd);
            this.den = denominator.divide(gcd);

            // Ensure the denominator is always positive for a standard representation
            if (this.den.compareTo(BigInteger.ZERO) < 0) {
                this.num = this.num.negate();
                this.den = this.den.negate();
            }
        }
        
        // Constructor for whole numbers (denominator = 1)
        public Fraction(BigInteger numerator) {
            this(numerator, BigInteger.ONE);
        }

        // Method to add two fractions: a/b + c/d = (ad + bc) / bd
        public Fraction add(Fraction other) {
            BigInteger newNum = this.num.multiply(other.den).add(other.num.multiply(this.den));
            BigInteger newDen = this.den.multiply(other.den);
            return new Fraction(newNum, newDen);
        }

        // Method to multiply two fractions: a/b * c/d = ac / bd
        public Fraction multiply(Fraction other) {
            BigInteger newNum = this.num.multiply(other.num);
            BigInteger newDen = this.den.multiply(other.den);
            return new Fraction(newNum, newDen);
        }
    }

    public static void main(String[] args) {
        // Read the entire JSON input from standard input
        Scanner scanner = new Scanner(System.in);
        StringBuilder jsonInputBuilder = new StringBuilder();
        while(scanner.hasNextLine()){
            jsonInputBuilder.append(scanner.nextLine());
        }
        scanner.close();
        
        String jsonInput = jsonInputBuilder.toString();
        
        // Parse the JSON object
        JSONObject obj = new JSONObject(jsonInput);

        // The degree of the polynomial is k-1. We need k points to define it.
        int k = obj.getJSONObject("keys").getInt("k");
        
        // --- Data Extraction ---
        List<BigInteger> x_points = new ArrayList<>();
        List<BigInteger> y_points = new ArrayList<>();

        // Get all data point keys (e.g., "1", "2", "6") and sort them numerically
        // to ensure we deterministically select the first k points.
        List<String> sortedKeys = new ArrayList<>();
        for (String key : obj.keySet()) {
            if (!key.equals("keys")) {
                sortedKeys.add(key);
            }
        }
        Collections.sort(sortedKeys, (s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));

        // Populate the x and y points lists from the sorted keys
        for (String key : sortedKeys) {
            JSONObject pointData = obj.getJSONObject(key);
            int base = Integer.parseInt(pointData.getString("base"));
            String valueStr = pointData.getString("value");

            x_points.add(new BigInteger(key));
            y_points.add(new BigInteger(valueStr, base));
        }

        // We only need the first k points to perform the interpolation
        List<BigInteger> x = x_points.subList(0, k);
        List<BigInteger> y = y_points.subList(0, k);

        // --- Lagrange Interpolation to find P(0) ---
        Fraction totalSum = new Fraction(BigInteger.ZERO);

        for (int j = 0; j < k; j++) {
            BigInteger y_j = y.get(j);
            Fraction lagrangeBasis = new Fraction(BigInteger.ONE); // Start with 1/1

            for (int m = 0; m < k; m++) {
                if (j != m) {
                    BigInteger x_m = x.get(m);
                    BigInteger x_j = x.get(j);
                    
                    // Numerator for the basis term: x_m
                    BigInteger num = x_m;
                    // Denominator for the basis term: (x_m - x_j)
                    BigInteger den = x_m.subtract(x_j);
                    
                    lagrangeBasis = lagrangeBasis.multiply(new Fraction(num, den));
                }
            }
            
            Fraction currentTerm = lagrangeBasis.multiply(new Fraction(y_j));
            totalSum = totalSum.add(currentTerm);
        }

        // The final result 'c' must be an integer, so the fraction's denominator will be 1.
        // We print the numerator, which is our constant term.
        System.out.println(totalSum.num);
    }
}