package cecs429.weight;

import java.util.HashMap;

public class WeightModeFactory {
    private static HashMap<String, Strategy> modeMap = new HashMap<>();

    public WeightModeFactory(){
        modeMap.put("1", new Default());
        modeMap.put("2", new TfIdf());
        modeMap.put("3", new OkapiBM25());
        modeMap.put("4", new Wacky());
    }

    public static Strategy getMode(String mode) {

        Strategy strategy = modeMap.get(mode.toString());
        if (strategy != null) {
            return strategy;
        }
        return new Default();
    }
}
