package com.example.utils.page;

import lombok.Data;

@Data
public class Page {
    private int pageNumber; //每页的条数
    private int offset; //数据库查询索引
  //get，set省略
}
