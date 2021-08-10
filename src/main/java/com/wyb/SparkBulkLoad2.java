package com.wyb;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.spark.FamilyHFileWriteOptions;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.spark.KeyFamilyQualifier;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Serializable;
import scala.Tuple2;
import org.apache.hadoop.hbase.spark.HBaseContext;
import org.apache.spark.Logging;
// import org.apache.hadoop.hbase.regionserver.StoreFileWriter.Builder.withComparator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SparkBulkLoad2 implements Serializable {

    private static JavaSparkContext getSC() {
        SparkConf sparkConf = new SparkConf().setAppName("testSpark").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        return sc;
    }

    public static void main(String[] args) {
        SparkBulkLoad2 s = new SparkBulkLoad2();
        s.generateHFilesAndBulkLoad();
    }


    private class myFunction implements Function<String, Pair<KeyFamilyQualifier, byte[]>> {
        @Override
        public Pair<KeyFamilyQualifier, byte[]> call(String s) throws Exception {
            String[] lines = s.split(",");
            String rowkey = lines[0];
            String val = lines[2];
            String[] infos = lines[1].split(":");
            String cf = infos[0];
            String qualifier = infos[1];
            KeyFamilyQualifier keyFamilyQualifier = new KeyFamilyQualifier(Bytes.toBytes(rowkey),
                    Bytes.toBytes(cf),
                    Bytes.toBytes(qualifier));
            System.out.println(keyFamilyQualifier.toString());
            return new Pair<>(keyFamilyQualifier,Bytes.toBytes(val));
        }
    }

    private void generateHFilesAndBulkLoad(){
        String inputFile = "./src/main/resources/input.txt";
        JavaSparkContext sc = getSC();
        Configuration config = HBaseUtils.configuration;
        JavaHBaseContext hbaseContext = new JavaHBaseContext(sc, config);
        String columnFamily1 = "info";
        String tableName = "bulkload";

        JavaRDD<String> javaRDD = sc.textFile(inputFile);
        String tmpDir = "./tmp";

        try {
            FileSystem fileSystem = FileSystem.get(config);
            fileSystem.delete(new Path(tmpDir),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap familyHFileWriteOptionsMap = new HashMap<Byte[], FamilyHFileWriteOptions>();
        FamilyHFileWriteOptions f1Options =
                new FamilyHFileWriteOptions("GZ", "ROW",
                        128, "PREFIX");

        familyHFileWriteOptionsMap.put(Bytes.toBytes(columnFamily1), f1Options);

        try {
            System.out.println("clean hbase table");
            HBaseUtils.createTable("bulkload","info");
            HBaseUtils.disableTable("bulkload");
            HBaseUtils.truncate("bulkload");

            hbaseContext.bulkLoad(javaRDD, TableName.valueOf(tableName),
                    new myFunction(),tmpDir, familyHFileWriteOptionsMap
                    ,false,100);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
