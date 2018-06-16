package Lucene;

public class ClassificationDocument {
    private Integer docId;
    private String title;
    private String content;
    private Integer classID;

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
