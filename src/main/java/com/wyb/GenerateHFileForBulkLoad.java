package com.wyb;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class GenerateHFileForBulkLoad {

    /**
     * 需求：用BulkLoad的方法导入数据
     *
     * @DataFormat 1       info:www.baidu.com      BaiDu
     */
    public static class GenerateHFile extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

        @Override
        protected void map(LongWritable Key, Text Value,
                           Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context context)
                throws IOException, InterruptedException {

            //切分导入的数据
            String Values = Value.toString();
            String[] Lines = Values.split(",");
            String Rowkey = Lines[0];
            String ColumnFamily = Lines[1].split(":")[0];
            String Qualifier = Lines[1].split(":")[1];
            String ColValue = Lines[2];

            String str = Rowkey + " -> " + ColumnFamily + " -> " + Qualifier + " -> " + ColValue;
            System.out.println(str);
            //拼装rowkey和put
            ImmutableBytesWritable PutRowkey = new ImmutableBytesWritable(Rowkey.getBytes());
            Put put = new Put(Rowkey.getBytes());
            put.addColumn(ColumnFamily.getBytes(), Qualifier.getBytes(), ColValue.getBytes());

            System.out.println(put.toJSON());
            context.write(PutRowkey, put);
        }

    }


    public static void main(String[] args) throws Exception {

        /**
         * 获取Hbase配置，创建连接到目标表，表在Shell中已经创建好，建表语句create 'BulkLoad','Info'，这里注意HBase对大小写很敏感
         */
        Configuration conf = HBaseUtils.configuration;
        Connection conn = HBaseUtils.conn;
        String target_hbase_table = "bulkload";
        String target_hbase_table_column_family = "info";
        HBaseUtils.createTable(target_hbase_table, target_hbase_table_column_family);
        Admin admin = HBaseUtils.admin;

        final String InputFile = "./src/main/resources/input.txt";
        final String OutputFile = "C:\\Users\\32006\\Desktop\\test\\hbase\\output";
        final Path OutputPath = new Path(OutputFile);

        // 判断路径是不是存在，存在就删除
        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(OutputPath)) {
            fileSystem.delete(OutputPath, true);
        }

        //设置相关类名
        Job job = Job.getInstance(conf, "BulkLoad");
        job.setJarByClass(GenerateHFileForBulkLoad.class);
        job.setMapperClass(GenerateHFile.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);

        //设置文件的输入路径和输出路径
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(HFileOutputFormat2.class);
        FileInputFormat.setInputPaths(job, InputFile);
        FileOutputFormat.setOutputPath(job, OutputPath);

        //配置MapReduce作业，以执行增量加载到给定表中。
        Table table = conn.getTable(TableName.valueOf(target_hbase_table));
        RegionLocator regionLocator = table.getRegionLocator();
        HFileOutputFormat2.configureIncrementalLoad(job, table, regionLocator);

        job.waitForCompletion(true);

        // hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /output bulkload
        //MapReduce作业完成，告知RegionServers在哪里找到这些文件,将文件加载到HBase中
        if (job.waitForCompletion(true)) {
            System.out.println("Finished generateHFile");
            LoadIncrementalHFiles Loader = new LoadIncrementalHFiles(conf);
            Loader.doBulkLoad(OutputPath, admin, table, regionLocator);
        }
    }
}
