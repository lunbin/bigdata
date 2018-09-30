import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.io.compress.Compression;

import java.io.IOException;

public class hbaseExample {
    private static final String TABLE_NAME = "MY_TABLE_NAME_TOO";
    private static final String CF_DEFAULT = "DEFAULT_COLUMN_FAMILY";

    public static void createOrOverwrite(Admin admin, HTableDescriptor table) throws IOException {
        if (admin.tableExists(table.getTableName())) {
            admin.disableTable(table.getTableName());
            admin.deleteTable(table.getTableName());
        }
        System.out.println("start create table");
        admin.createTable(table);
    }
    public static void createSchemaTables(Configuration config) {
        Connection connection = null;
        Admin admin = null;
        try {
            connection = ConnectionFactory.createConnection(config);
            admin = connection.getAdmin();
            HTableDescriptor table = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
            table.addFamily(new HColumnDescriptor(CF_DEFAULT).setCompressionType(Compression.Algorithm.NONE));

            System.out.print("Creating table. ");
            createOrOverwrite(admin, table);
            System.out.println(" Done.");
            admin.close();
            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void modifySchema (Configuration config) throws IOException {
        Connection connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin();
        TableName tableName = TableName.valueOf(TABLE_NAME);
        if (!admin.tableExists(tableName)) {
            System.out.println("Table does not exist.");
            System.exit(-1);
        }

        HTableDescriptor table = admin.getTableDescriptor(tableName);

        // Update existing table
        HColumnDescriptor newColumn = new HColumnDescriptor("NEWCF");
        newColumn.setCompactionCompressionType(Compression.Algorithm.GZ);
        newColumn.setMaxVersions(HConstants.ALL_VERSIONS);
        admin.addColumn(tableName, newColumn);

        // Update existing column family
        HColumnDescriptor existingColumn = new HColumnDescriptor(CF_DEFAULT);
        existingColumn.setCompactionCompressionType(Compression.Algorithm.GZ);
        existingColumn.setMaxVersions(HConstants.ALL_VERSIONS);
        table.modifyFamily(existingColumn);
        admin.modifyTable(tableName, table);

        // Disable an existing table
        admin.disableTable(tableName);

        // Delete an existing column family
        admin.deleteColumn(tableName, CF_DEFAULT.getBytes("UTF-8"));

        // Delete a table (Need to be disabled first)
        admin.deleteTable(tableName);

        admin.close();
        connection.close();
    }
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.rootdir","hdfs://10.19.248.200:30380/hbase");
        config.set("hbase.zookeeper.quorum","pre1-zookeeper1:2181,pre1-zookeeper2:2181,pre1-zookeeper3:2181");
//
//        config.set("hbase.rootdir","hdfs://enncloud-hadoop/hbase");
//        config.set("hbase.zookeeper.quorum","10.19.248.200:31587,10.19.248.200:29152,10.19.248.200:30463/");
        createSchemaTables(config);
    }

}
