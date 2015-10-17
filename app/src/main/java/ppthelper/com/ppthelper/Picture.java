package ppthelper.com.ppthelper;

/**
 * Created by Chen Qu on 2015/9/22.
 */
public class Picture {
    public int _id;
    public String tag;
    public String fileName;

    public Picture() {
    }

    public Picture(String tag, String fileName) {
        this.tag = tag;
        this.fileName = fileName;
    }
}
