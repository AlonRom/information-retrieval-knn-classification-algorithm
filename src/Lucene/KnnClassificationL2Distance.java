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
    public Float vectorDistance(HashMap<Integer,Float> train, HashMap.Entry<Integer,Float>[] trainArraySquare , HashMap<Integer,Float> test , HashMap.Entry<Integer,Float>[] testArraySquare) {
        double sum = 0.0;

        float trainValue,testValue,difference,testValueSquare,trainValueSquare;
        if (train==null){
            for (int i=0;i<testArraySquare.length;i++){
                testValue = testArraySquare[i].getValue();
                sum = sum + testValue*testValue;
            }
        }
        for (int i=0;i<trainArraySquare.length;i++){
            if (test.containsKey(trainArraySquare[i].getKey())){
                trainValue = train.get(trainArraySquare[i].getKey());
                difference = trainValue - test.get(trainArraySquare[i].getKey());
                sum = sum + difference * difference;
            }

            else{
                trainValueSquare = trainArraySquare[i].getValue();
                sum = sum + trainValueSquare;
            }

        }
        for (int i=0;i<testArraySquare.length;i++){
            testValueSquare = testArraySquare[i].getValue();
            if (train != null && !train.containsKey(testArraySquare[i].getKey())){
                sum = sum + testValueSquare;
            }
        }
        return (float) Math.sqrt(sum);
    }
}
