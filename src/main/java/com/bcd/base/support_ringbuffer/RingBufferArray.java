package com.bcd.base.support_ringbuffer;

/**
 * 固定长度的缓存队列
 * 如果长度已满、时间最早的纪录会被移除
 * @param <T>
 */
public class RingBufferArray<T> {
    private int firstIndex = -1;
    private int lastIndex = -1;
    private final int size;
    private final Object[] content;

    public RingBufferArray(int size) {
        this.size = size;
        this.content = new Object[size];
    }

    public void add(T t) {
        lastIndex = ((lastIndex + 1) % size);
        content[lastIndex] = t;

        if (firstIndex == -1) {
            firstIndex = 0;
        } else {
            if (firstIndex == lastIndex) {
                firstIndex = ((lastIndex + 1) % size);
            }
        }
    }

    public void addAll(final T[] arr) {
        int length = arr.length;
        if (length >= size) {
            System.arraycopy(arr, length - size, content, 0, size);
            firstIndex = 0;
            lastIndex = size - 1;
        } else {
            if (firstIndex == -1) {
                System.arraycopy(arr, 0, content, 0, length);
                firstIndex = 0;
                lastIndex = length - 1;
            } else {
                if (firstIndex > lastIndex) {
                    int rightLength = size - lastIndex - 1;
                    if (length > rightLength) {
                        System.arraycopy(arr, 0, content, lastIndex + 1, rightLength);
                        System.arraycopy(arr, rightLength, content, 0, length - rightLength);
                    } else {
                        System.arraycopy(arr, 0, content, lastIndex + 1, length);
                    }
                    lastIndex = ((lastIndex + length) % size);
                    firstIndex = ((lastIndex + 1) % size);
                } else {
                    int leave = size - lastIndex - firstIndex - 1;
                    if (length <= leave) {
                        System.arraycopy(arr, 0, content, lastIndex + 1, length);
                        lastIndex = lastIndex + length;
                    } else {
                        System.arraycopy(arr, 0, content, lastIndex + 1, leave);
                        System.arraycopy(arr, leave, content, 0, length - leave);
                        lastIndex = ((lastIndex + length) % size);
                        firstIndex = ((lastIndex + 1) % size);
                    }
                }

            }
        }

    }

    public T getFirst() {
        return firstIndex == -1 ? null : (T) content[firstIndex];
    }

    public T getLast() {
        return lastIndex == -1 ? null : (T) content[lastIndex];
    }

    public Object[] content() {
        if (firstIndex == -1) {
            return new Object[0];
        } else {
            Object[] temp;
            if (firstIndex > lastIndex) {
                temp = new Object[size];
                int rightLength = size - firstIndex;
                System.arraycopy(content, firstIndex, temp, 0, rightLength);
                System.arraycopy(content, 0, temp, rightLength, lastIndex + 1);
            } else {
                temp = new Object[lastIndex - firstIndex + 1];
                System.arraycopy(content, firstIndex, temp, 0, temp.length);
            }
            return temp;
        }
    }

    public static void main(String[] args) {
        RingBufferArray<Integer> ringBufferArray = new RingBufferArray<>(5);
//        for (int i = 0; i < 10; i++) {
//            ringBuffer.add(i);
//            System.out.println(ringBuffer.firstIndex + " " + ringBuffer.lastIndex);
//        }
//        Object[] list = ringBuffer.content();
//        for (Object integer : list) {
//            System.out.print(integer);
//        }
        int i=0;
        while (i++<100000000) {
            ringBufferArray.addAll(new Integer[]{1, 2, 3, 4});
            ringBufferArray.addAll(new Integer[]{5, 5});
            ringBufferArray.addAll(new Integer[]{6});
            ringBufferArray.addAll(new Integer[]{7, 8, 9});
            ringBufferArray.addAll(new Integer[]{10, 11});
            Object[] list = ringBufferArray.content();
//            for (Object integer : list) {
//                System.out.print(integer);
//            }
        }

    }
}