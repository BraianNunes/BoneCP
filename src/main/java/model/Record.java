package model;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-7-27
 * Time: 下午2:13
 * To change this template use File | Settings | File Templates
 */
public class Record {
    private String description;
    private String content;

    public Record(String description, String content) {
        this.description = description;
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
