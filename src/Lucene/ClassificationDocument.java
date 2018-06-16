package Lucene;

public class ClassificationDocument {
    Integer docId=0;
    String title=null;
    String content=null;
    Integer classID=0;

    ClassificationDocument(Integer docId, String title, String content, Integer classID){
        this.docId=docId;
        this.title=title;
        this.content=content;
        this.classID=classID;
    }

    public Integer getDocId(){
        return this.docId;
    }

    public String getTitle(){
        return this.title;
    }

    public String getContent(){
        return this.content;
    }

    public Integer getClassID(){
        return this.classID;
    }


}
