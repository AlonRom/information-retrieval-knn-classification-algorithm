package Lucene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnnClassificationL2Distance extends KnnClassificator {

    public KnnClassificationL2Distance(SparseVector[] trainTfIdfVectorArray, SparseVector[] testTfIdfVectorArray
            , List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCategory, Integer numberOfTerms ){

        super(trainTfIdfVectorArray,testTfIdfVectorArray,trainDocList,k,numberOfCategory,numberOfTerms);
    }


    @Override
    public Float vectorDistance(SparseVector train, SparseVector test) {
        SparseVector diff = train.minus(test);
        Double distance = Math.sqrt(diff.getSquareSum());
        return new Float(distance);
    }
}
