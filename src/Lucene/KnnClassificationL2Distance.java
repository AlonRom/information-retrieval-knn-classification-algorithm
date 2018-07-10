package Lucene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnnClassificationL2Distance extends KnnClassificator {

    public KnnClassificationL2Distance(HashMap<Integer,Float>[] trainTfIdfVectorArray, HashMap<Integer,Float>[] testTfIdfVectorArray
            , List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCategory ){

        super(trainTfIdfVectorArray,testTfIdfVectorArray,trainDocList,k,numberOfCategory);
    }


    @Override
    public Float vectorDistance(HashMap<Integer, Float> train, HashMap<Integer, Float> test) {
        float sum = 0;
        Float trainValue,testValue;
        for (Map.Entry<Integer , Float> entry : train.entrySet()){
            trainValue = entry.getValue();
            testValue = test.get(entry.getKey());
            if (testValue != null){
                sum = sum + (trainValue - testValue) * (trainValue - testValue);
            }
            else{
                sum = sum + (float) Math.pow(trainValue,2);
            }
        }
        for (Map.Entry<Integer , Float> entry : test.entrySet()){
            testValue = entry.getValue();
            trainValue = train.get(entry.getKey());
            if (trainValue==null){
                sum = sum + (float) Math.pow(testValue,2);
            }
        }
        return (float) Math.sqrt(sum);
    }
}
