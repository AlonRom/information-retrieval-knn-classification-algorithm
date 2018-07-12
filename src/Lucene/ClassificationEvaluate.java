package Lucene;

import java.util.Arrays;
import java.util.List;

public class ClassificationEvaluate {
    public Integer[] truePositive;
    public Integer[] falsePositive;
    public Integer[] falseNegative;
    private Integer[] classifcationResults;
    private List<ClassificationDocument> doclist;
    private int numberOfCategories;
    private Double macroF1,microF1;


    ClassificationEvaluate(Integer[] classifcationResults,List<ClassificationDocument> doclist, int numberOfCategories ){
        this.numberOfCategories = numberOfCategories;
        this.classifcationResults = classifcationResults;
        this.doclist = doclist;
        this.truePositive = new Integer[numberOfCategories];
        Arrays.fill(truePositive,0);
        this.falsePositive = new Integer[numberOfCategories];
        Arrays.fill(falsePositive,0);
        this.falseNegative = new Integer[numberOfCategories];
        Arrays.fill(falseNegative,0);

    }

    public void evaluate(){
        for (int i=0;i<classifcationResults.length;i++){
            if (classifcationResults[i].equals(doclist.get(i).getClassID())){
                truePositive[classifcationResults[i]-1]++;
            }
            else{
                falsePositive[classifcationResults[i]-1]++;
                falseNegative[doclist.get(i).getClassID()-1]++;
            }
        }
        Double [] macroPercision  = new Double[numberOfCategories];
        Double [] macroRecall = new Double[numberOfCategories];
        Double [] F1ForEachClass = new Double[numberOfCategories];
        double totalFN = 0.0 , totalFP = 0.0 , totalTP = 0.0;
        Arrays.fill(F1ForEachClass,0.0);
        for (int i=0;i<numberOfCategories;i++){
            if (!truePositive[i].equals(0)){
                macroPercision[i] = (double)truePositive[i]/(double) (truePositive[i]+falsePositive[i]);
                macroRecall[i] = (double)truePositive[i]/(double)  (truePositive[i]+falseNegative[i]);
                F1ForEachClass[i]+=(2*macroPercision[i]*macroRecall[i])/(macroPercision[i]+macroRecall[i]);
                totalFN += falseNegative[i];
                totalFP += falsePositive[i];;
                totalTP += truePositive[i];
            }
            else{
                macroPercision[i] = 0.0;
                macroRecall[i] = 0.0;
                F1ForEachClass[i]=0.0;
                totalFN += falseNegative[i];
                totalFP += falsePositive[i];;
                totalTP += truePositive[i];
            }
            macroF1 = averageArray(F1ForEachClass);
            double microPrecision, microRecall;
            microPrecision = totalTP / (totalTP+totalFP);
            microRecall = totalTP / (totalTP + totalFN);
            microF1 = 2*microPrecision*microRecall/(microPrecision+microRecall);
        }
    }

    public double getMacroF1(){
        return macroF1;
    }

    public double getMicroF1(){
        return microF1;
    }

    private double averageArray(Double [] arr){
        double sum=0.0;
        for (int i=0;i<arr.length;i++){
            sum += arr[i];
        }
        return sum/(double)arr.length;
    }





}
