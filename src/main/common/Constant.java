package common;

public class Constant {

  // 如果是tpch数据则true: 专门为tpch的c5和c7的从数字转换到真实的查询准备的
  // 一般情况下设置成false不用管这一项
  public static boolean isTranform = false;

  public static String dataNum = "1500000";
//    public static String dataNum = "3000000";
//    public static String dataNum = "4500000";
//    public static String dataNum = "6000000";
//    public static String dataNum = "7500000";

  // true：记录最后优化输出解对应的Cassandra sqls用于后续实验
  public static boolean isGetSqls = true;

  // keyspace name
  public static String ks = "keyspace1";
  //  public static String ks = "tpch_ds1_loose";
//    public static String ks = "tpch_ds2";
//    public static String ks = "tpch_ds3";
//    public static String ks = "tpch_ds4";
//    public static String ks = "tpch_ds5";

  // column family name 列族
  public static String[] cf = new String[]{"df1", "df2", "df3"};
  public static int[] pkey = new int[]{1, 1, 1};

  // clustering key 排序键
  public static String[] ckname = new String[]{"c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8"};
  public static boolean[] isInt = new boolean[]{true, true, true, true, true, true, true, true};
  //  public static String[] ckname = new String[]{"c2","c5","c7"};
  //  public static boolean[] isInt = new boolean[]{true, true, true};

  // 副本数
  public static int RNum = 3;

  // 是否异构
  public static boolean isDiffReplica = true;

  public static boolean isAccelerate = true;

  public static boolean isPrint = true;

  public static boolean isRecordProcess = true;
  public static String SArecord = "SA_process_record.csv";

  // 数据存储参数
//  public static int rowSize = 100;

  //单查询代价模型参数
  public static int fetchRowCnt = 100;
  public static double costModel_k = 2.024;
  public static double costModel_b = 16862.946;
  public static double cost_session_around = 242.793;
  public static double cost_request_around = 808.423;
}
