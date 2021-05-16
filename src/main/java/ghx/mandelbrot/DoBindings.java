package ghx.mandelbrot;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.util.StringConverter;

public class DoBindings {
    static void bind(StringProperty s, DoubleProperty v) {
        Bindings.bindBidirectional(
                s,
                v,
                new StringConverter<Number>() {

                    public Double fromString(String s) {
//                        System.out.println("from string " + s);
                        try {
                            return Double.parseDouble(s);
                        } catch (Throwable t) {
                            return 0.0;
                        }
                    }

                    public String toString(Number n) {
//                        System.out.println("from number " + n);
                        if (((Double) n).isInfinite())
                            return "Infinity";
                        else
                            return n.toString();
                    }
                }
                );
    }
}
