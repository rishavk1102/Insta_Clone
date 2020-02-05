package com.rishav.instaclone.Model;

public class HashTag {

    private String tag;
    private String postid;

    public HashTag() {
    }

    public HashTag(String tag, String postid) {
        this.tag = tag;
        this.postid = postid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }
}
