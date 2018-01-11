package com.maxcloud.bluetoothsdkdemov2;

import java.util.ArrayList;

/**
 * Created by longqungao on 2018/1/11.
 */

public class Test {
    public static void main(String args[]){
        ArrayList<Integer> items = new ArrayList<Integer>();
        items.add(1);
        items.add(3);
        items.add(56);
        items.add(77);
        items.add(4);
        items.add(2);
        items.add(277);
        items.add(277);


        //然后取最大值:
        int a = 0;

        for(int i=0;i<items.size();i++)
        {
            if(i==0)
            {
                a = items.get(i);
            }
            else
            {
                a = Math.max(a,items.get(i));
            }
        }

        if(items.size() > 0)
        {
            System.out.println("最大值:" + a);
        }
        else
        {
            System.out.println("当前list为空");
        }

//        int i,min,max;
//        int A[]={99,48,30,17,195}; // 声明整数数组A,并赋初值
//        max=A[0];
//        System.out.print("数组A的元素包括：");
//        int j =0;
//        int n =0 ;
//        for(i=0;i<A.length;i++)
//        {
//            System.out.print(A[i]+" ");
//            if(A[i]>max) {// 判断最大值
//                j=i;
//                max=A[i];
//            }
//
//        }
//        System.out.println("\n数组的最大值是："+max+".数组的位置是："+(j+1)); // 输出最大值和最大值的位置
        //System.out.println("数组的最小值是："+min+".数组的位置是："+(n+1)); // 输出最小值
    }
}
