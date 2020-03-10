package introclassJava;

class IntObj {
    public int value;
    public IntObj () {
    } public IntObj (int i) {
        value = i;
    }
}

class FloatObj {
    public float value;
    public FloatObj () {
    } public FloatObj (float i) {
        value = i;
    }
}

class LongObj {
    public long value;
    public LongObj () {
    } public LongObj (long i) {
        value = i;
    }
}

class DoubleObj {
    public double value;
    public DoubleObj () {
    } public DoubleObj (double i) {
        value = i;
    }
}

class CharObj {
    public char value;
    public CharObj () {
    } public CharObj (char i) {
        value = i;
    }
}

public class smallest_ea67b841_003 {
    public java.util.Scanner scanner;
    public String output = "";

    public static void main (String[]args) throws Exception {
        smallest_ea67b841_003 mainClass = new smallest_ea67b841_003 ();
        String output;
        if (args.length > 0) {
            mainClass.scanner = new java.util.Scanner (args[0]);
        } else {
            mainClass.scanner = new java.util.Scanner (System.in);
        }
        mainClass.exec ();
        System.out.println (mainClass.output);
    }

    public void exec () throws Exception {
        IntObj a = new IntObj (), b = new IntObj (), c = new IntObj (), d =
            new IntObj (), SN = new IntObj ();
        output +=
            (String.format ("Please enter 4 numbers separated by spaces > "));
        a.value = scanner.nextInt ();
        b.value = scanner.nextInt ();
        c.value = scanner.nextInt ();
        d.value = scanner.nextInt ();
        if ((a.value < b.value) && (a.value < c.value) && (a.value < d.value)) {
            SN.value = a.value;
        } else if ((b.value < a.value) && (b.value < c.value)
                   && (b.value < d.value)) {
            SN.value = b.value;
        } else if ((c.value < a.value) && (c.value < b.value)
                   && (c.value < d.value)) {
            SN.value = c.value;
        } else {
            SN.value = d.value;
        }
        output += (String.format ("%d is the smallest\n", SN.value));
        if (true)
            return;;
    }
}
