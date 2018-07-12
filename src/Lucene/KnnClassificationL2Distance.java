package Lucene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnnClassificationL2Distance extends KnnClassificator {

    public KnnClassificationL2Distance(HashMap<Integer,Float>[] trainTfIdfVectorArray, HashMap<Integer,Float>[] testTfIdfVectorArray
            , List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCategory, Integer numberOfTerms ){

        super(trainTfIdfVectorArray,testTfIdfVectorArray,trainDocList,k,numberOfCategory,numberOfTerms);
    }


    @Override
    public Float vectorDistance(HashMap<Integer,Float> train, HashMap <Integer,Float> trainSquare , HashMap<Integer,Float> test , HashMap<Integer,Float> testSquare) {
        double sum = 0.0;

        float trainValue,trainValueSquare,testValueSquare,difference;
        for (Map.Entry<Integer , Float> entry : trainSquare.entrySet()){
            if (test.containsKey(entry.getKey())){
                trainValue = train.get(entry.getKey());
                difference = trainValue - test.get(entry.getKey());
                sum = sum + difference * difference;
            }

            else{
                trainValueSquare = entry.getValue();
                sum = sum + trainValueSquare;
            }

        }
        for (Map.Entry<Integer , Float> entry : testSquare.entrySet()){
            testValueSquare = entry.getValue();
            if (!train.containsKey(entry.getKey())){
                sum = sum + testValueSquare;
            }
        }
        return (float) Math.sqrt(sum);
    }
}
