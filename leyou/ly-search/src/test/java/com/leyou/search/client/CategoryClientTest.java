package com.leyou.search.client;

import com.leyou.item.pojo.Category;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {
    @Autowired
    public CategoryClient categoryClient;

    @Test
    public void queryCategoryListByListIds() {
        List<Category> categories = categoryClient.queryCategoryListByListIds(Arrays.asList(1L, 2L, 3L));
        Assert.assertEquals(3,categories.size());//断言结果为3
        for (Category c: categories) {
            System.err.println("category"+c);
        }
    }

}