/*
 * Created on 13-5-29
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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Description of this file.
 *
 * @author XiongNeng
 * @version 1.0
 * @since 13-5-29
 */
public class ExampleJDBCTest {

    @BeforeClass
    public static void init() {
        JdbcUtil.initDataSourcePool();
    }

    @Test
    public void testGetBeanListData() throws Exception {
        ExampleJDBC.getBeanListData();
    }

    @Test
    public void testGetMapListData() throws Exception {
        ExampleJDBC.getMapListData();
    }

    @Test
    public void testInsertAndUpdateData() throws Exception {
        ExampleJDBC.insertAndUpdateData();
    }

    @Test
    public void testFindUseSqlNullCheckedResultSet() throws Exception {
        ExampleJDBC.findUseSqlNullCheckedResultSet();
    }
}
