package Lucene;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class KnnClassificator {
    private HashMap<Integer,Integer> termDictionary;
    private Integer k;
    private List<ClassificationDocument> trainDocList;
    private HashMap<Integer,Float>[] trainTfIdfVectorArray;
    private HashMap<Integer,Float>[] testTfIdfVectorArray;
    private Integer numberOfCatagory;

    public KnnClassificator(HashMap<Integer,Float>[] trainTfIdfVectorArray, HashMap<Integer,Float>[] testTfIdfVectorArray
            ,HashMap<Integer,Integer> termDictionary, List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCatagory ){
        this.trainTfIdfVectorArray = trainTfIdfVectorArray;
        this.testTfIdfVectorArray = testTfIdfVectorArray;
        this.termDictionary = termDictionary;
        this.trainDocList = trainDocList;
        this.k = k;
        this.numberOfCatagory = numberOfCatagory;
    }

    public Integer Classify(Integer numOfDocToClassify){
        HashMap<Integer,Float> test = testTfIdfVectorArray[numOfDocToClassify];

        Neighbor[] distanceArray = new Neighbor[trainTfIdfVectorArray.length];
        for (int i=0;i<trainTfIdfVectorArray.length;i++){
            distanceArray[i] = new Neighbor(i,vectorDistance(trainTfIdfVectorArray[i],test));
        }

        Integer result =  voteFromKNearestNeighbors(distanceArray);
        distanceArray = null;
        return result;
    }

    public abstract Float vectorDistance(HashMap<Integer,Float> train,HashMap<Integer,Float> test);

    private Integer voteFromKNearestNeighbors(Neighbor [] kNearestNEighbors){
        int[] category = new int[numberOfCatagory];
        for (int i=0;i<numberOfCatagory;i++){
            category[i] = 0;
        }
        for (int i=0; i<k;i++){
            int docId = kNearestNEighbors[i].id;
            int docCatagory = trainDocList.get(docId).getClassID();
            category[docCatagory] = category[docCatagory] + 1 ;
        }
        return findMax(category);
    }

    private int findMax(int [] arr){
        int index = 0;
        int max = arr[0];
        for (int i=0;i<arr.length;i++){
            if (arr[i]>max){
                max = arr[i];
                index = i;
            }
        }
        return index;
    }





}


