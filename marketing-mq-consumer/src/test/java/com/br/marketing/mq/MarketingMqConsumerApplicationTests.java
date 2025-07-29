package com.br.marketing.mq;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class MarketingMqConsumerApplicationTests {

    @Test
    public void testFilterMaintainsOrder() {
        // 准备测试数据
        List<String> originalList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
        List<String> filterSet = Arrays.asList("A", "C", "E", "G"); // 要保留的元素

        // 执行筛选操作（模拟你的代码逻辑）
        List<String> filteredList = originalList.stream()
                .filter(item -> filterSet.contains(item))
                .collect(Collectors.toList());

        // 验证筛选后的顺序
        List<String> expectedOrder = Arrays.asList("A", "C", "E", "G");
        Assert.assertEquals("筛选后应该保持原有顺序", expectedOrder, filteredList);

        // 验证筛选后的元素顺序与原始列表中对应元素的顺序一致
        int expectedIndex = 0;
        for (String item : filteredList) {
            int originalIndex = originalList.indexOf(item);
            Assert.assertTrue("筛选后的元素顺序应该与原始顺序一致",
                    originalIndex >= expectedIndex);
            expectedIndex = originalIndex;
        }
    }

    @Test
    public void testFilterMaintainsOrderWithDuplicates() {
        // 测试包含重复元素的情况
        List<String> originalList = Arrays.asList("A", "B", "A", "C", "B", "D", "A");
        List<String> filterSet = Arrays.asList("A", "C"); // 只保留A和C

        List<String> filteredList = originalList.stream()
                .filter(item -> filterSet.contains(item))
                .collect(Collectors.toList());

        // 验证筛选后的顺序
        List<String> expectedOrder = Arrays.asList("A", "A", "C", "A");
        Assert.assertEquals("筛选后应该保持原有顺序，包括重复元素", expectedOrder, filteredList);
    }

    @Test
    public void testFilterMaintainsOrderWithEmptyResult() {
        // 测试筛选后为空的情况
        List<String> originalList = Arrays.asList("A", "B", "C", "D");
        List<String> filterSet = Arrays.asList("X", "Y", "Z"); // 没有匹配的元素

        List<String> filteredList = originalList.stream()
                .filter(item -> filterSet.contains(item))
                .collect(Collectors.toList());

        Assert.assertTrue("筛选后应该为空", filteredList.isEmpty());
    }

    @Test
    public void testFilterMaintainsOrderWithAllElements() {
        // 测试保留所有元素的情况
        List<String> originalList = Arrays.asList("A", "B", "C", "D");
        List<String> filterSet = Arrays.asList("A", "B", "C", "D"); // 保留所有元素

        List<String> filteredList = originalList.stream()
                .filter(item -> filterSet.contains(item))
                .collect(Collectors.toList());

        Assert.assertEquals("筛选后应该保持原有顺序", originalList, filteredList);
    }

    @Test
    public void testFilterMaintainsOrderWithComplexObjects() {
        // 测试复杂对象的情况
        List<TestObject> originalList = Arrays.asList(
                new TestObject("A", 1),
                new TestObject("B", 2),
                new TestObject("C", 3),
                new TestObject("D", 4),
                new TestObject("E", 5)
        );

        List<String> filterSet = Arrays.asList("A", "C", "E");

        List<TestObject> filteredList = originalList.stream()
                .filter(obj -> filterSet.contains(obj.getName()))
                .collect(Collectors.toList());

        // 验证筛选后的顺序
        Assert.assertEquals("筛选后应该保持原有顺序", "A", filteredList.get(0).getName());
        Assert.assertEquals("筛选后应该保持原有顺序", "C", filteredList.get(1).getName());
        Assert.assertEquals("筛选后应该保持原有顺序", "E", filteredList.get(2).getName());
    }

    /**
     * 测试用的简单对象
     */
    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', value=" + value + "}";
        }
    }

}
