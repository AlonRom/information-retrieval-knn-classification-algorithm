package Lucene;

import java.util.HashMap;
import java.util.List;

import static org.apache.lucene.util.ArrayUtil.swap;

public abstract class KnnClassificator {
    private Integer k;
    private List<ClassificationDocument> trainDocList;
    private SparseVector[] trainTfIdfVectorArray;
    private SparseVector[] testTfIdfVectorArray;
    private Integer numberOfCatagory;
    private Integer numberOfTerms;

    public KnnClassificator(SparseVector[] trainTfIdfVectorArray, SparseVector[] testTfIdfVectorArray
            , List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCatagory, int numberOfTerms ){
        this.trainTfIdfVectorArray = trainTfIdfVectorArray;
        this.testTfIdfVectorArray = testTfIdfVectorArray;
        this.trainDocList = trainDocList;
        this.k = k;
        this.numberOfCatagory = numberOfCatagory;
        this.numberOfTerms = numberOfTerms;
    }

    public Integer classify(Integer numOfDocToClassify){
        SparseVector test = testTfIdfVectorArray[numOfDocToClassify];

        Neighbor[] distanceArray = new Neighbor[trainTfIdfVectorArray.length];
        for (int i=0;i<trainTfIdfVectorArray.length;i++){
            if (trainTfIdfVectorArray[i] == null){
                SparseVector emptyVector = new SparseVector(numberOfTerms);
                distanceArray[i] = new Neighbor(i, vectorDistance(emptyVector, test));
            }
            else {
                distanceArray[i] = new Neighbor(i, vectorDistance(trainTfIdfVectorArray[i], test));
            }
        }
        //Arrays.sort(distanceArray);
        sortKValuesInArray(distanceArray,k);
        Integer result =  voteFromKNearestNeighbors(distanceArray);
        distanceArray = null;
        return result;
    }

    public abstract Float vectorDistance(SparseVector train,SparseVector test);

    private void sortKValuesInArray(Neighbor[] arr, int k){
        int p=0;
        while (p<k){
            Float min = arr[p].distance;
            int index = arr[p].id;
            for (int i=p+1;i<arr.length;i++){
                if (arr[i].distance.compareTo(min)<0){
                    min = arr[i].distance;
                    index = arr[i].id;
                }
            }
            swap(arr,p,index);
            p++;
        }

    }

    private Integer voteFromKNearestNeighbors(Neighbor [] kNearestNEighbors){
        int[] category = new int[numberOfCatagory];
        for (int i=0;i<numberOfCatagory;i++){
            category[i] = 0;
        }
        for (int i=0; i<k;i++){
            int docId = kNearestNEighbors[i].id;
            int docCatagory = trainDocList.get(docId).getClassID();
            category[docCatagory-1] = category[docCatagory-1] + 1 ;
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
        return index+1;
    }

    public Integer[] getDocsClassification(){
        Integer[] classifier = new Integer[testTfIdfVectorArray.length];
        for (int i=0;i<testTfIdfVectorArray.length;i++){
            classifier[i] = classify(i);
            System.out.println("Doc " + i + " classified");
        }
        return classifier;
    }





}


