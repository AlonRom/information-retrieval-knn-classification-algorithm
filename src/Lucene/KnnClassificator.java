package Lucene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.lucene.util.ArrayUtil.swap;

public abstract class KnnClassificator {
    private Integer k;
    private List<ClassificationDocument> trainDocList;
    private HashMap<Integer,Float>[] trainTfIdfVectorArray;
    private HashMap<Integer,Float>[] trainTFIdfVectorArraySquare;
    private HashMap<Integer,Float>[] testTfIdfVectorArray;
    private HashMap<Integer,Float>[] testTfIdfVectorArraySquare;
    private Integer numberOfCatagory;
    private int numberOfTerms;

    public KnnClassificator(HashMap<Integer,Float>[] trainTfIdfVectorArray, HashMap<Integer,Float>[] testTfIdfVectorArray
            , List<ClassificationDocument> trainDocList , Integer k, Integer numberOfCatagory, int numberOfTerms ){
        this.trainTfIdfVectorArray = trainTfIdfVectorArray;
        this.testTfIdfVectorArray = testTfIdfVectorArray;
        this.trainDocList = trainDocList;
        this.k = k;
        this.numberOfCatagory = numberOfCatagory;
        this.numberOfTerms = numberOfTerms;
    }

    public Integer classify(Integer numOfDocToClassify,Neighbor[] distanceArray){
        HashMap<Integer,Float> test = testTfIdfVectorArray[numOfDocToClassify];
        HashMap testSquare = testTfIdfVectorArraySquare[numOfDocToClassify];

        for (int i=0;i<trainTfIdfVectorArray.length;i++){
            if (trainTfIdfVectorArray[i] == null){
                HashMap<Integer,Float> emptyVector = new HashMap<>();
                distanceArray[i].putIdAndDistnace(i, vectorDistance(emptyVector, emptyVector, test, testSquare));
            }
            else {
                distanceArray[i].putIdAndDistnace(i, vectorDistance(trainTfIdfVectorArray[i],trainTFIdfVectorArraySquare[i],test,testSquare));
            }
        }
        //Arrays.sort(distanceArray);
        sortKValuesInArray(distanceArray,k);
        Integer result =  voteFromKNearestNeighbors(distanceArray);
        distanceArray = null;
        return result;
    }

    public abstract Float vectorDistance(HashMap<Integer,Float> train,HashMap<Integer,Float> trainSqaure,HashMap<Integer,Float> test, HashMap<Integer,Float> testSquare);

    private void sortKValuesInArray(Neighbor[] arr, int k){
        int p=0;
        while (p<k){
            Float min = arr[p].distance;
            int index = arr[p].id;
            for (int i=p+1;i<arr.length;i++){
                if (arr[i].distance.compareTo(min)<0){
                    min = arr[i].distance;
                    index = i;
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
        Neighbor[] distanceArray = new Neighbor[trainTfIdfVectorArray.length];
        for (int i=0;i<distanceArray.length;i++)
            distanceArray[i] = new Neighbor(null,null);
        trainTFIdfVectorArraySquare = calculateSquareValuesOfHashMap(trainTfIdfVectorArray);
        testTfIdfVectorArraySquare = calculateSquareValuesOfHashMap(testTfIdfVectorArray);
        for (int i=0;i<testTfIdfVectorArray.length;i++){
            long startTime = System.nanoTime();
            classifier[i] = classify(i,distanceArray);
            long endTime = System.nanoTime();
            long result = endTime - startTime;
            System.out.println("Doc " + i + " classified in " + result);
        }
        return classifier;
    }

    private HashMap<Integer , Float>[] calculateSquareValuesOfHashMap(HashMap<Integer,Float>[] mapArray){
        HashMap<Integer,Float>[] mapArraySqaure = new HashMap[mapArray.length];
        for (int i=0;i<mapArray.length;i++){
            mapArraySqaure[i] = new HashMap();
            for (Map.Entry<Integer,Float> entry : mapArray[i].entrySet()){
                mapArraySqaure[i].put(entry.getKey(),entry.getValue()*entry.getValue());
            }
        }
        return mapArraySqaure;
    }





}


