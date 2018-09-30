package org.conan.myhadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.net.URI;


public class hdfsClient {
    public static FileSystem getFileSystem(String hdfsUri, Configuration conf) {
        FileSystem fs = null;
        if (hdfsUri == null) {
            fs = getFileSystem(conf);
        } else {
            try {
               fs = FileSystem.get(URI.create(hdfsUri),conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fs;
    }

    public static FileSystem getFileSystem(Configuration conf) {
        FileSystem fs = null;
        try {
           fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fs;
    }

    public static Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.addResource(new Path(HadoopProperties.CORESITE));
        conf.addResource(new Path(HadoopProperties.HDFSSITE));
        conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        return conf;
    }

    public static void makeDirs(FileSystem fs,String dir) {
        try {
            fs.mkdirs(new Path(dir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyLocalFileToHdfs(FileSystem fs, String localfile, String hdfsfile) {
        try {
            fs.copyFromLocalFile(new Path(localfile), new Path(hdfsfile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("no hdfs config file path");
            return;
        }
        Configuration conf = getConfiguration();
        FileSystem fs = getFileSystem(conf);
        makeDirs(fs,"/var/slb");
        copyLocalFileToHdfs(fs, "/opt/entrypoint.sh", "/var/slb");
    }
}
