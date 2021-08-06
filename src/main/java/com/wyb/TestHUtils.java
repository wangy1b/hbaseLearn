package com.wyb;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class TestHUtils {
    // 获取的结果位Result怎么取值
    public static void testResult(String[] args) {
        try {
            String rk1 = "rk1";
            String tab1 = "ns1:test1";
            String fml1 = "info";
            HBaseUtils.createTable(tab1,fml1);
            for (int i = 0; i < 10; i++) {
                String col1= "id"+i;
                String col2= "name"+i;
                HBaseUtils.insertOne(tab1,rk1,fml1,col1,String.valueOf(i));
                HBaseUtils.insertOne(tab1,rk1,fml1,col2,"value"+i);
            }

            Result result = HBaseUtils.select(tab1, rk1);
            System.out.println(result.size());
            result.getRow();

            // Reading values from Result class object
            for (int i = 0; i < 10; i++) {
                String col1= "id"+i;
                String col2= "name"+i;

                byte [] val_id = result.getValue(Bytes.toBytes(fml1),Bytes.toBytes(col1));
                byte [] val_name = result.getValue(Bytes.toBytes(fml1),Bytes.toBytes(col2));

                // Printing the values
                String id = Bytes.toString(val_id);
                String name = Bytes.toString(val_name);

                System.out.println("id: " + id + " name: " + name);
            }


            HBaseUtils.disableTable(tab1);
            HBaseUtils.dropTable(tab1);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtils.close();
        }
    }

    public static void testTableInfo(String[] args) {
        try {
            List<TableDescriptor>  res =HBaseUtils.listTables();
            for (int i=0; i<res.size();i++ ){
                System.out.println( "tableName : "+ res.get(i).getTableName().toString());
                System.out.println( "has "+res.get(i).getColumnFamilyCount() + " columnFamily");
                for (byte[] x : res.get(i).getColumnFamilyNames()) {
                    String str = new String(x, "utf8");
                    System.out.println( "columnFamily: " + str);
                }
                System.out.println("===============");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtils.close();
        }
    }

    public static void testResultScanner(String[] args) {
        try {
            String rk1 = "rk1";
            String tab1 = "ns1:test1";
            String fml1 = "info";
            HBaseUtils.createTable(tab1,fml1);

            Map colVal = new HashMap<String,String>();
            colVal.put("1","a");
            colVal.put("2","b");
            HBaseUtils.insertOne(tab1,rk1,fml1,colVal);

            ResultScanner resultScanner = HBaseUtils.scan(tab1, fml1);
            Iterator<Result> resultIterator =  resultScanner.iterator();
            while (resultIterator.hasNext()) {
                Result result = resultIterator.next();
                byte [] val_id = result.getValue(Bytes.toBytes(fml1),Bytes.toBytes("1"));
                byte [] val_name = result.getValue(Bytes.toBytes(fml1),Bytes.toBytes("2"));

                // Printing the values
                String colVal1 = Bytes.toString(val_id);
                String colVal2 = Bytes.toString(val_name);
                System.out.println("1: " + colVal1 + " 2: " + colVal2);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtils.close();
        }
    }

    private static void testDelete(String[] args) {
        try {
            String rk1 = "rk1";
            String rk2 = "rk2";
            String tab1 = "ns1:test1";
            String fml1 = "info";

            Map colVal = new HashMap<String,String>();
            colVal.put("1","a");
            colVal.put("2","b");
            HBaseUtils.insertOne(tab1,rk1,fml1,colVal);
            HBaseUtils.insertOne(tab1,rk2,fml1,colVal);

            System.out.println("before delete: "+HBaseUtils.select(tab1,rk1).size());

            // HBaseUtils.delete(tab1,rk1,fml1);
            HBaseUtils.delete(tab1,new String[]{rk1,rk2});
            System.out.println("after delete: "+HBaseUtils.select(tab1,rk1).size());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtils.close();
        }
    }

    public static void testFilter(String[] args) {
        try {
            String rk1 = "rk1";
            String rk2 = "rk2";
            String tab1 = "ns1:test1";
            String fml1 = "info";
            String col_pref = "name";
            String val_pref = "value";
            HBaseUtils.truncate(tab1);

            for (int i = 0; i < 10; i++) {
                HBaseUtils.insertOne(tab1,rk1,fml1,col_pref+i,val_pref+i);
            }



            // BinaryComparator
            // Filter filter = HBaseUtils.rowFilter(CompareOperator.EQUAL,new BinaryComparator(Bytes.toBytes(rk1)));

            // BinaryPrefixComparator
            // Filter filter = HBaseUtils.rowFilter(CompareOperator.EQUAL,new BinaryPrefixComparator(Bytes.toBytes("rk")));

            // SubstringComparator
            Filter filter1 = HBaseUtils.rowFilter(CompareOperator.EQUAL,new SubstringComparator("rk"));

            // pagetFilter
            Filter filter2 = HBaseUtils.pagetFilter(5);

            FilterList filter = HBaseUtils.filterListPassAll(filter1,filter2);

            ResultScanner resultScanner = HBaseUtils.scan(tab1,filter);
            Iterator<Result> resultIterator = resultScanner.iterator();

            while (resultIterator.hasNext()) {
                Result result = resultIterator.next();

                List<Cell> list = result.getColumnCells(Bytes.toBytes(fml1),Bytes.toBytes(col_pref+"2"));
                for (int i = 0; i < list.size(); i++) {
                    Cell cell = list.get(i);
                    String get_col = new String(CellUtil.cloneQualifier(cell),"utf-8");
                    String get_val = new String(CellUtil.cloneValue(cell),"utf-8");
                    String get_rk = new String(CellUtil.cloneRow(cell),"utf-8");
                    String get_cf = new String(CellUtil.cloneFamily(cell),"utf-8");
                    System.out.println(get_rk + " : " + get_cf + " : "+ get_col + " : " + get_val);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtils.close();
        }
    }

    public static void main(String[] args) {
        // testResult(args);
        // testTableInfo(args);
        // testResultScanner(args);
        // testDelete(args);
        testFilter(args);
    }


}
