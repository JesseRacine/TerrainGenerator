package application;

import javafx.util.StringConverter;

/**
 * Created by Jesse on 4/30/2016.
 */
public class SliderLabeler extends StringConverter<Double> {

    public String toString(Double n) {
        if (n < 1.00) return "Bright";
        if (n > 0.9 && n < 199.9) return "";
        else return "Dark";


    }

    @Override
    public Double fromString(String s) {
        switch (s) {
            case "Dark":
                return 200d;
            case "Bright":
                return 0d;

            default:
                return 0d;
        }
    }


}
