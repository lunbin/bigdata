public class main {
  public static void main(String[] args) {
    /**
     *  cd /opt
     *  java -cp bdexample-1.0-SNAPSHOT.jar:/opt/lib/* main [kafka | zookeeper] example.config
     *
     */
//    if (args.length != 2) {
//      System.out.println("Error: please input two character");
//      return;
//    }
//    String target = args[0];
//    String configfile = args[1];
//    BdExample bdExample = new BdExample();
//    bdExample.initProperties(configfile);
//    bdExample.executeExample(target);


    String target = "zookeeper";
    String configfile = "/Users/lbsheng/helium/bdexample/src/main/resources/example.config";
    BdExample bdExample = new BdExample();
    bdExample.initProperties(configfile);
    bdExample.executeExample(target);
  }
}
