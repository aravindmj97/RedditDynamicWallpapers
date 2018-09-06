package com.a.redditfetch;

import com.orm.SugarRecord;

public class ImagePojo extends SugarRecord<ImagePojo> {

    String url;
    String key;

    public ImagePojo() {
    }

    public ImagePojo(String url, String key) {
        this.url = url;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
