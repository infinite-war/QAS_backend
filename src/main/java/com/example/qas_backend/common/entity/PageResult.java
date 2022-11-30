package com.example.qas_backend.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageResult<T> {

    //列表
    private List<T> records;

    //数量
    private Integer total;

    public void createRecords(){
        this.records=new ArrayList<T>();
    }
    public void add(T item){
        this.records.add(item);
    }

    public void clear(){
        this.records.clear();
    }
}


