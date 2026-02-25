package au.com.thoughtpatterns.core.util;

/**
 * Represents a numerical amount of australian dollars
 */
public class AusDollar extends Money implements Comparable<AusDollar> {

    private static final long serialVersionUID = 1L;
    private int cents;

    public static AusDollar new_cents(int aCents) {
        AusDollar dollar = new AusDollar();
        dollar.setCents(aCents);
        return dollar;
    }

    public static AusDollar new_dollar(int aDollars) {
        AusDollar dollar = new AusDollar();
        dollar.setCents(aDollars * 100);
        return dollar;
    }

    public int getCents() {
        return cents;
    }

    public void setCents(int aCents) {
        cents = aCents;
    }

    public double getValueAsDouble() {
        return ((double) cents) / 100;
    }

    public String toString() {
        return format(getValueAsDouble());
    }

    private String format(double value) {
        String str = "" + value;
        // Ensure two decimal places if any
        int index = str.indexOf('.');
        if (index == str.length() - 2) {
            str = str + "0";
        }
        return str;
    }
    
    public boolean equals(Object other) {
        if (!(other instanceof AusDollar)) {
            return false;
        }
        AusDollar d = (AusDollar) other;
        return cents == d.cents;
    }

    public int hashCode() {
        return cents;
    }

    public String getFormattedString() {
        int sign = getCents() < 0 ? -1 : 1;
        String formatted = (sign < 0 ? "-$" : "$") + format(sign * getValueAsDouble());
        return formatted;
    }

	@Override
	public int compareTo(AusDollar o) {
		int c1 = getCents();
		int c2 = o.getCents();
		if (c1 == c2) {
			return 0;
		} else {
			return c1 < c2 ? -1 : 1;
		}
	}
	
	public AusDollar negate() {
	    return AusDollar.new_cents(-cents);
	}

}
