package org.cysoft.carovignobot.model;

/**
 * Created by NS293854 on 02/06/2016.
 */
public class CyFile implements Comparable<CyFile>{

    @Override
    public int compareTo(CyFile another) {
        if (this.id==another.id)
            return 0;
        else
            return 1;
    }

    private final String FILE_TYPE_PHOTO="Photo";

    public long id;
    public String name;
    public long length;
    public String contentType;
    public String fileType;
    public String note;
    public long entityId;

    public boolean isPhoto(){
        if (fileType.equalsIgnoreCase(FILE_TYPE_PHOTO) || contentType.startsWith("image") ||
            name.endsWith(".jpg") || name.endsWith(".png"))
            return true;
        else
            return false;
    }


    @Override
    public String toString() {
        return "CyFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", length=" + length +
                ", contentType='" + contentType + '\'' +
                ", fileType='" + fileType + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
