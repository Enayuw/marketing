package com.br.marketing.check.utils;

import org.apache.commons.collections4.ListUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：： List拆分工具嘞
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName TpListUtils
 * @author: it-yml
 * @create: 2023-07-10 21:15
 * @Version 1.0
 * --------------------------------------
 **/
public class TpListUtils {
    public static <T> List<List<T>> partition(final List<T> list, final int size) {
        if (list == null) {
            throw new NullPointerException("List must not be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        return new Partition<>(list, size);
    }
    private static class Partition<T> extends AbstractList<List<T>> {
        private final List<T> list;
        private final int size;

        private Partition(final List<T> list, final int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(final int index) {
            final int listSize = size();
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index " + index + " must not be negative");
            }
            if (index >= listSize) {
                throw new IndexOutOfBoundsException("Index " + index + " must be less than size " +
                        listSize);
            }
            final int start = index * size;
            final int end = Math.min(start + size, list.size());
            List<T> list1 = new ArrayList<>();
            list1 = list.subList(start, end);
            return list1;
        }

        @Override
        public int size() {
            return (int) Math.ceil((double) list.size() / (double) size);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }
    }
    public static void main(String[] args) {
        ArrayList<String> objects = new ArrayList<>();
        objects.add("123");
        objects.add("123");
        objects.add("343");
        objects.add("434");
        objects.add("4343");
        objects.add("123");
        objects.add("eee");
        objects.add("dfd");
        objects.add("123");
        objects.add("dfd");
        objects.add("123");
        objects.add("123");
        objects.add("fdfd");
        objects.add("dfdfd");
        objects.add("fdfd");
        objects.add("nmjjj");

        List<List<String>> partition = ListUtils.partition(objects, 2);
        partition.forEach(t -> {
            System.out.println("list对象：" + System.identityHashCode(t));
        });

        partition.forEach(t -> {
            List<String> list = new ArrayList<>();
            list.addAll(t);
            new Thread(() -> {
                try {
                    list.removeIf(m -> "123".equals(m));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }).start();
        });
    }

}
