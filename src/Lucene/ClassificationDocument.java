package Lucene;

public class ClassificationDocument {
    private Integer docId=0;
    private String title=null;
    private String content=null;
    private Integer classID=0;

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
