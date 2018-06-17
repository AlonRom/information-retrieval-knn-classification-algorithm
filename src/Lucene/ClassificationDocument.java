package Lucene;

public class ClassificationDocument 
{
    private Integer _docId;
    private String _title;
    private String _content;
    private Integer _classID;

    ClassificationDocument(Integer docId, String title, String content, Integer classID)
    {
        _docId = docId;
        _title = title;
        _content = content;
        _classID = classID;
    }

    public Integer getDocId()
    {
        return _docId;
    }

    public String getTitle()
    {
        return _title;
    }

    public String getContent()
    {
        return _content;
    }

    public Integer getClassID()
    {
        return _classID;
    }
}
