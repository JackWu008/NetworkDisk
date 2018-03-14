package net.lzzy.networkdisk.models;


public enum FileType {
    picture(0, new String[]{"jpg", "png", "jpeg"}), music(1, new String[]{"mp3", "wav", "m4a"}), document(2, new String[]{"txt", "docx", "doc", "xls", "xlsx", "ppt", "pptx"}), video(3, new String[]{"mp4", "3gp", "avi"});
    public int key;
    public String[] val;

    FileType(int key, String[] val) {
        this.key = key;
        this.val = val;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String[] getVal() {
        return val;
    }

    public void setVal(String[] val) {
        this.val = val;
    }

    public static String[] getTypeByKey(int key) {
        for (FileType f : FileType.values()) {
            if (f.key == (key))
                return f.getVal();
        }
        return new String[]{};
    }

    public static int getKeyByType(String type) {
        for (FileType f : FileType.values()) {
            for (String s : f.getVal()) {
                if (type.equals(s))
                    return f.getKey();
            }
        }
        return -1;
    }
}
