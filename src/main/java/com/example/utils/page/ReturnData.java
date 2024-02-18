package com.example.utils.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回数据实体类
 * @param <T>
 */
public class ReturnData <T>{
    //数据集合
    private List<T> rows = new ArrayList<T>();
    //数据总条数
    private int total;
  //...
}
