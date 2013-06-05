/*
 * Created on 13-6-5
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright @2013 the original author or authors.
 */
package dbutils;

import java.util.List;

/**
 * 分页实体类.
 *
 * @author XiongNeng
 * @version 1.0
 * @since 13-6-5
 */
public class PageResult<T> {
    /**
     * 当前页号，从1开始
     */
    int currentPage;
    /**
     * 每页记录数量
     */
    int pageSize;
    /**
     * 表中总记录数
     */
    int count;
    /**
     * 页面记录列表
     */
    List<T> beanList;

    /**
     * 构造函数
     * @param currentPage 当前页号
     * @param pageSize 每页记录数量
     * @param beanList 表中总记录数
     * @param count 页面记录列表
     */
    public PageResult(int currentPage, int pageSize, List<T> beanList, int count) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.count = count;
        this.beanList = beanList;
    }
}
