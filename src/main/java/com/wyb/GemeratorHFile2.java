package com.wyb;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

public class GemeratorHFile2 {
    static class HFileImportMapper2 extends Mapper<LongWritable, Text, ImmutableBytesWritable, KeyValue> {

        protected final String CF_KQ = "info";

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // String line = value.toString();
            // System.out.println("line : " + line);
            // String[] datas = line.split(" ");
            // String row = new Date().getTime() + "_" + datas[1];
            // ImmutableBytesWritable rowkey = new ImmutableBytesWritable(Bytes.toBytes(row));
            // KeyValue kv = new KeyValue(Bytes.toBytes(row), this.CF_KQ.getBytes(), datas[1].getBytes(), datas[2].getBytes());
            // context.write(rowkey, kv);


            //切分导入的数据
            String Values = value.toString();
            String[] Lines = Values.split(",");
            String Rowkey = Lines[0];
            String ColumnFamily = Lines[1].split(":")[0];
            String Qualifier = Lines[1].split(":")[1];
            String ColValue = Lines[2];

            String str = Rowkey + " -> " + ColumnFamily + " -> " + Qualifier + " -> " + ColValue;
            System.out.println(str);
            //拼装rowkey和put
            ImmutableBytesWritable PutRowkey = new ImmutableBytesWritable(Rowkey.getBytes());
            // Put put = new Put(Rowkey.getBytes());
            // put.addColumn(ColumnFamily.getBytes(), Qualifier.getBytes(), ColValue.getBytes());

            KeyValue kv = new KeyValue(Bytes.toBytes(Rowkey),ColumnFamily.getBytes(),
                    Qualifier.getBytes(),ColValue.getBytes());

            System.out.println(kv);
            context.write(PutRowkey, kv);


        }
    }

    public static void main(String[] args) {

        Configuration conf = new Configuration();
        conf.addResource(new Path("./src/main/resources/hbase-site.xml"));
        conf.set("hbase.fs.tmp.dir", "partitions_" + UUID.randomUUID());
        String tableName = "bulkload";
        String input = "./src/main/resources/input.txt";;
        String output = "C:\\Users\\32006\\Desktop\\test\\hbase\\output";
        System.out.println("table : " + tableName);
        HTable table;
        try {
            try {
                FileSystem fs = FileSystem.get(conf);
                fs.delete(new Path(output), true);
                fs.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Connection conn = ConnectionFactory.createConnection(conf);
            table = (HTable) conn.getTable(TableName.valueOf(tableName));
            Job job = Job.getInstance(conf);
            job.setJobName("Generate HFile");

            job.setJarByClass(GemeratorHFile2.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapperClass(HFileImportMapper2.class);

            job.setMapOutputKeyClass(ImmutableBytesWritable.class);
            job.setMapOutputValueClass(KeyValue.class);

            FileInputFormat.setInputPaths(job, input);
            FileOutputFormat.setOutputPath(job, new Path(output));

            HFileOutputFormat2.configureIncrementalLoad(job, table,table.getRegionLocator());
            try {
                job.waitForCompletion(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}