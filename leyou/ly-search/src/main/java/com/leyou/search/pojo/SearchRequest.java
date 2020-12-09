package com.leyou.search.pojo;

import java.util.Map;

public class SearchRequest {
    private String key;//搜索字符
    private Integer page;//当前页
    private Map<String, String> filter;

    private static final int DEFAULT_SIZE=20;
    private static final int DEFAULT_PAGE=1;//默认页

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Integer getPage() {
        if(page==null){
           return DEFAULT_PAGE;
        }
        //对获取页进行校验，不能小于1
        return Math.max(DEFAULT_PAGE,page);
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    public int getSize(){
        return DEFAULT_SIZE;
    }
    public Map<String, String> getFilter() { return filter; }
    public void setFilter(Map<String, String> filter) { this.filter = filter; }
}
