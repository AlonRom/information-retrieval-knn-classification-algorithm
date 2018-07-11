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
    public Float vectorDistance(HashMap<Integer,Float> train, HashMap<Integer,Float> test) {
        double sum = 0.0;

        float trainValue,testValue;
        for (Map.Entry<Integer , Float> entry : train.entrySet()){
            trainValue = entry.getValue();
            if (test.containsKey(entry.getKey())){
                testValue = test.get(entry.getKey());
                sum = sum + (trainValue - testValue) * (trainValue - testValue);
            }

            else{
                sum = sum + trainValue*trainValue;
            }

        }
        for (Map.Entry<Integer , Float> entry : test.entrySet()){
            testValue = entry.getValue();
            if (!train.containsKey(entry.getKey())){
                sum = sum + (testValue*testValue);
            }
        }
        return (float) Math.sqrt(sum);
    }
}
