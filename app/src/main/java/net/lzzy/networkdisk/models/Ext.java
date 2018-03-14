package net.lzzy.networkdisk.models;


import net.lzzy.networkdisk.R;

public enum Ext {
    TXT("txt", R.drawable.ic_txt), DOCX("docx", R.drawable.ic_doc), DOC("doc", R.drawable.ic_doc), XLS("xls", R.drawable.ic_xls), PPT("ppt", R.drawable.ic_ppt), XLSX("xlsx", R.drawable.ic_xls), PPTX("pptx", R.drawable.ic_ppt), PDF("pdf", R.drawable.ic_pdf), JPG("jpg", R.drawable.ic_image), PNG("png", R.drawable.ic_image), GIF("gif", R.drawable.ic_image), JPEG("jpeg", R.drawable.ic_image), MP4("mp4", R.drawable.ic_video), ThreeGP("3gp", R.drawable.ic_video), AVI("avi", R.drawable.ic_video), MP3("mp3", R.drawable.ic_music), WAV("wav", R.drawable.ic_music), M4A("m4a", R.drawable.ic_music), ZIP("zip", R.drawable.ic_compression), ISO("iso", R.drawable.ic_compression), RAR("rar", R.drawable.ic_compression), SevenZ("7z", R.drawable.ic_compression), APK("apk", R.drawable.ic_app), HTML("html", R.drawable.ic_html);
    private String name;
    private int icon;

    Ext(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public static int getIcon(String suffix) {
        int icon = R.drawable.ic_file;
        for (Ext e : Ext.values()) {
            if (e.name.equals(suffix))
               icon = e.getIcon();
        }
        return icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
