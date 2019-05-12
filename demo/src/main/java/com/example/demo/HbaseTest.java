package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTest {

  public static HBaseAdmin admin;
  public static Configuration conf;
  public static Connection connection;
  public static Properties properties;

  public HbaseTest() {
    System.out.println("Hbase Test");
  }


  public static void loadConfigFile(String configFile) {
    properties = new Properties();
    try {
      FileInputStream inputStream = new FileInputStream(new File(configFile));
      properties.load(inputStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  /**
   * Init hbase connection and admin
   */
  static {
    conf = HBaseConfiguration.create();
//    loadConfigFile("properties.conf");
    conf.set(
        "hbase.zookeeper.quorum", "10.19.248.200:31127,10.19.248.200:30761,10.19.248.200:29776");
    conf.set("zookeeper.znode.parent", "/pre1-hbase");
    try {
      connection = ConnectionFactory.createConnection(conf);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      admin = (HBaseAdmin) connection.getAdmin();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void exist(String htable, String family, String qulified) {
    try {
      HTable table = (HTable) connection.getTable(TableName.valueOf(htable));
      Get get = new Get(Bytes.toBytes("1"));
      get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qulified));

      if (table.exists(get)) {
        System.out.println("already exist...");
      } else {
        System.out.println("not exist...");
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * updata hbase table , same as putData2HTable
   */
  public void updateHTable() {
//    putData2HTable();
  }

  public void truncateHTable(String htable) {
    try {
      admin.disableTable(TableName.valueOf(htable));
      admin.deleteTable(TableName.valueOf(htable));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   *  count a table rows
   * @param htable
   */
  public void countHTable(String htable) {
    int count = 0;
    try {
      HTable table = (HTable) connection.getTable(TableName.valueOf(htable));
      Scan scan = new Scan();
      ResultScanner scanner = table.getScanner(scan);

      for (Result result = scanner.next(); result != null; result = scanner.next()) {
        count++;
      }
      System.out.println("count: " + count);
      scanner.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * scan hbase table by table name
   * @param htable
   */
  public void scanHTable(String htable) {
    try {
      HTable table = (HTable) connection.getTable(TableName.valueOf(htable));
      Scan scan = new Scan();
      ResultScanner scanner = table.getScanner(scan);
      for (Result result = scanner.next(); result != null; result = scanner.next() ) {

        System.out.println("find row: " + result);

        byte[] name = result.getValue(Bytes.toBytes("personal data"),Bytes.toBytes("name"));
        byte[] city = result.getValue(Bytes.toBytes("personal data"),Bytes.toBytes("city"));
        System.out.println("presonal data:name -> " + Bytes.toString(name));
        System.out.println("presonal data:city -> " + Bytes.toString(city));
      }
      scanner.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * get data from hbase table by row key
   * @param htable
   * @param rowKey
   */
  public void getDataFromHTableByRowKey(String htable, String rowKey) {
    try {
      HTable table = (HTable) connection.getTable(TableName.valueOf(htable));
//      HTableDescriptor descriptor = table.getTableDescriptor();
//      System.out.println("========================");
//      System.out.println(descriptor.getNameAsString());
//      System.out.println("========================");

      Get get = new Get(Bytes.toBytes(rowKey));
//      get.addColumn(Bytes.toBytes("personal data"), Bytes.toBytes("name"));
//      get.addColumn(Bytes.toBytes("personal data"), Bytes.toBytes("city"));
//      get.addColumn(Bytes.toBytes("professional data"), Bytes.toBytes("designation"));
//      get.addColumn(Bytes.toBytes("professional data"), Bytes.toBytes("salary"));

      Result result = table.get(get);

      byte[] name = result.getValue(Bytes.toBytes("personal data"), Bytes.toBytes("name"));
      byte[] city = result.getValue(Bytes.toBytes("personal data"), Bytes.toBytes("city"));
      byte[] designation =
          result.getValue(Bytes.toBytes("professional data"), Bytes.toBytes("designation"));
      byte[] salary = result.getValue(Bytes.toBytes("professional data"), Bytes.toBytes("salary"));
      System.out.println(
          "name: "
              + Bytes.toString(name) + ","
              + " city: "
              + Bytes.toString(city) + ","
              + " designation: "
              + Bytes.toString(designation) + ","
              + " salary: "
              + Bytes.toString(salary));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * delete data from hbase table by row key
   * @param htable
   * @param rowKey
   */
  public void deleteDataFromHTableByRowKey(String htable, String rowKey) {
    try {
      HTable  table = (HTable) connection.getTable(TableName.valueOf(htable));
      Delete delete = new Delete(Bytes.toBytes(rowKey));

      table.delete(delete);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * delete data form hbase table by row key and column(family + quarified)
   * @param htable
   * @param rowKey
   * @param family
   * @param quarified
   */
  public void deleteDataFromHTableByRowKeyAndColumn(String htable, String rowKey, String family, String quarified) {
    try {
      HTable  table = (HTable) connection.getTable(TableName.valueOf(htable));
      Delete delete = new Delete(Bytes.toBytes(rowKey));
      delete.addColumn(Bytes.toBytes(family),Bytes.toBytes(quarified));
      table.delete(delete);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void putData2HTableByMutator(String htable) {

    ArrayList<Put> puts = new ArrayList<>();

    Put put1 = new Put(Bytes.toBytes("6"));
    put1.addColumn(
        Bytes.toBytes("personal data"), Bytes.toBytes("name"), Bytes.toBytes("lbsheng"));
    put1.addColumn(
        Bytes.toBytes("personal data"), Bytes.toBytes("city"), Bytes.toBytes("shanghai"));
    put1.addColumn(
        Bytes.toBytes("professional data"),
        Bytes.toBytes("designation"),
        Bytes.toBytes("sr.engineer"));
    put1.addColumn(
        Bytes.toBytes("professional data"), Bytes.toBytes("salary"), Bytes.toBytes("30000"));

    Put put2 = new Put(Bytes.toBytes("7"));
    put2.addColumn(
        Bytes.toBytes("personal data"), Bytes.toBytes("name"), Bytes.toBytes("lbsheng"));
    put2.addColumn(
        Bytes.toBytes("personal data"), Bytes.toBytes("city"), Bytes.toBytes("shanghai"));
    put2.addColumn(
        Bytes.toBytes("professional data"),
        Bytes.toBytes("designation"),
        Bytes.toBytes("sr.engineer"));
    put2.addColumn(
        Bytes.toBytes("professional data"), Bytes.toBytes("salary"), Bytes.toBytes("30000"));

    puts.add(put1);
    puts.add(put2);

    BufferedMutator.ExceptionListener listener = new BufferedMutator.ExceptionListener() {
      @Override
      public void onException(RetriesExhaustedWithDetailsException exception, BufferedMutator mutator) throws RetriesExhaustedWithDetailsException {
        return;
      }
    };
    BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(htable)).listener(listener);

    try {
      BufferedMutator mutator = connection.getBufferedMutator(params);
      mutator.mutate(puts);
      mutator.flush();
      mutator.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  /**
   * put data into hbase table
   * @param htable
   * @param rowKey
   */
  public void putData2HTable(String htable, String rowKey) {
    System.out.println("start insert data into htable");
    try {

      HTable table = (HTable) connection.getTable(TableName.valueOf(htable));
      Put put = new Put(Bytes.toBytes(rowKey));
      put.addColumn(
          Bytes.toBytes("personal data"), Bytes.toBytes("name"), Bytes.toBytes("lbsheng"));
      put.addColumn(
          Bytes.toBytes("personal data"), Bytes.toBytes("city"), Bytes.toBytes("shanghai"));
      put.addColumn(
          Bytes.toBytes("professional data"),
          Bytes.toBytes("designation"),
          Bytes.toBytes("sr.engineer"));
      put.addColumn(
          Bytes.toBytes("professional data"), Bytes.toBytes("salary"), Bytes.toBytes("30000"));
      table.put(put);
      table.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * delet a column family from hbase table
   * @param htable
   * @param hcolumn
   */
  public void deleteColumnFamilyForTable(String htable, String hcolumn) {
    try {
      admin.deleteColumn(TableName.valueOf(htable), hcolumn.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * disable hbase table
   * @param table
   */
  public void disableTalbe(String table) {
    try {
      if (admin.isTableDisabled(TableName.valueOf(table))) {
        System.out.println("table: " + table + " already disabled");
      } else {
        admin.disableTable(TableName.valueOf(table));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void listHbaseTables() {
    try {
      HTableDescriptor[] tableDescriptors = admin.listTables();
      for (HTableDescriptor tableDescriptor : tableDescriptors) {
        System.out.println(tableDescriptor.getNameAsString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * create hbase table
   * @param htable
   * @param colunmFamilyList
   */
  public void createTable(String htable, String colunmFamilyList) {
    try {
      if (admin.tableExists(TableName.valueOf(htable))) {
        System.out.println("table: " + htable + " is already exist...");
        return;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(htable));

    String[] families = colunmFamilyList.split(",");
    for (String family : families) {
      tableDescriptor.addFamily(new HColumnDescriptor(family));
    }
    try {
      admin.createTable(tableDescriptor);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("create table " + htable + " success!");
  }

  /**
   * add column family to hbase table
   * @param htable
   * @param column
   */
  public void addColumnFamilyForTable(String htable, String column) {
    try {
      admin.addColumn(TableName.valueOf(htable), new HColumnDescriptor(column));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
