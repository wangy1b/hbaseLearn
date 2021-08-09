package com.wyb;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class TestMR extends Configured implements Tool {



    public static class TemplateMapper extends Mapper<Object, Text, Text, Text> {
        private Text v1 = new Text();
        private Text k1 = new Text();
        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            System.out.println("keyin: " + key + " valuein: " + value);
            String lineValue = value.toString();
            String[] arrs = lineValue.split(",");


            k1.set(arrs[0]);
            v1.set(arrs[1]);
            System.out.println(k1.toString() + " -> " + v1.toString() );
            context.write(k1, v1);
        }
    }

    /**
     * reduce
     */
    public static class TemplateReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
        }
    }

    /**
     * @param strings
     * @return
     * @throws Exception
     */
    @Override
    public int run(String[] strings) throws Exception {
        final String InputFile = "src/main/resources/input.txt";
        final String OutputFile = "C:\\Users\\32006\\Desktop\\test\\hbase\\output";

        // driver
        // 1.get conf
        Configuration conf = this.getConf();

        // 判断路径是不是存在，存在就删除
        Path fileOutPath = new Path(OutputFile);
        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(fileOutPath)) {
            fileSystem.delete(fileOutPath, true);
        }
        // 2.create job
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(TestMR.class);
        // 3.1. map
        job.setMapperClass(TemplateMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // 1.分区
        // job.setPartitionerClass();
        // 2.排序
        // job.setSortComparatorClass();

        // 3.combiner 其实就是map端的reduce
        // job.setCombinerClass();

        // 4.compress
        // conf.set("mapreduce.map.output.compress","true");
        // conf.set("mapreduce.map.output.compress.codec","org.apache.hadoop.io.compress.SnappyCodec");

        // 5.group
        // job.setGroupingComparatorClass();


        // 3.2. reduce
        // job.setReducerClass(TemplateReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);


        // 设置reduce数量
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(InputFile));
        FileOutputFormat.setOutputPath(job, new Path(OutputFile));
        int status = job.waitForCompletion(true) ? 0 : 1;
        return status;
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        int status = ToolRunner.run(conf, new TestMR(), args);
        System.exit(status);
    }
}
