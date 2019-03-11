import algorithm.SA;
import common.Constant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Column;
import query.QueryPicture;
import replica.AckSeq;

public class TpchTest1 {

  public static void main(String[] args) {
    // 数据分布参数
    BigDecimal totalRowNumber = new BigDecimal(Constant.dataNum);

    List<Column> CKdist = new ArrayList<Column>();
    double step1 = 1;
    List<Double> x1 = new ArrayList<Double>();
    for (int i = 1; i <= 150001; i = i + 1000) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 300001; i=i+1000) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 450001; i=i+1000) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 600001; i=i+1000) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 750001; i = i + 1000) { //TODO:这个不同datasize要改的！！！！
      x1.add((double) i);
    }
    List<Double> y1 = new ArrayList<Double>();
    for (int i = 1; i < 150001; i = i + 1000) {
//        for (int i = 1; i < 300001; i=i+1000) {
//        for (int i = 1; i < 450001; i=i+1000) {
//        for (int i = 1; i < 600001; i=i+1000) {
//        for (int i = 1; i < 750001; i = i + 1000) {
      y1.add(1.0);
    }
    Column ck1 = new Column(step1, x1, y1);
    CKdist.add(ck1);

        /*
           Data: [1500000×1 int32]
           Values: [134208 155018 155115 156089 156308 156414 155841 156230 156245 118532]
          NumBins: 10
         BinEdges: [8000 8250 8500 8750 9000 9250 9500 9750 10000 10250 10500]
         BinWidth: 250
        BinLimits: [8000 10500]
    Normalization: 'count'
        FaceColor: 'auto'
        EdgeColor: [0 0 0]
         */
    double step2 = 1;
    List<Double> x2 = new ArrayList<Double>();
    for (int i = 8000; i <= 10500; i = i + 250) {
      x2.add((double) i);
    }
    List<Double> y2 = new ArrayList<Double>();
    y2.add(134.208);
    y2.add(155.018);
    y2.add(155.115);
    y2.add(156.089);
    y2.add(156.308);
    y2.add(156.414);
    y2.add(155.841);
    y2.add(156.230);
    y2.add(156.245);
    y2.add(118.532);
    Column ck2 = new Column(step2, x2, y2);
    CKdist.add(ck2);

    double step3 = 1;
    List<Double> x3 = new ArrayList<Double>();
    for (int i = 1; i <= 1001; i = i + 10) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 2001; i=i+10) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 3001; i=i+10) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 4001; i=i+10) { //TODO:这个不同datasize要改的！！！！
//        for (int i = 1; i <= 5001; i = i + 10) { //TODO:这个不同datasize要改的！！！！
      x3.add((double) i);
    }
    List<Double> y3 = new ArrayList<Double>();
    for (int i = 0; i < 100; i++) {
//        for (int i = 0; i < 200; i++) {
//        for (int i = 0; i < 300; i++) {
//        for (int i = 0; i < 400; i++) {
//        for (int i = 0; i < 500; i++) {
      y3.add(1.0);
    }
    Column ck3 = new Column(step3, x3, y3);
    CKdist.add(ck3);

    int ckn = CKdist.size();

    // 查询参数
    double[][] starts = new double[ckn][];
    double[][] lengths = new double[ckn][];
    int[] qpernum = new int[]{0, 1, 0};

    //列内分布参数
    starts[0] = new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    lengths[0] = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}; // c2顾客全范围查询

    starts[1] = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
    lengths[1] = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}; // c2顾客全范围查询

    starts[2] = new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    lengths[2] = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}; // c7员工全范围查询

    QueryPicture queryPicture = new QueryPicture(starts, lengths, qpernum, 167);//167*3=501个

    int X = 3;

    SA unify = new SA(totalRowNumber,
        ckn, CKdist,
        Constant.fetchRowCnt, 2.04979, 17242.52183, Constant.cost_session_around,
        Constant.cost_request_around,
        queryPicture,
        X);

    // 设置一些参数，也可以到Constant里统一修改
    Constant.isTranform = true;
    Constant.dataNum = "1500000";
//    public static String dataNum = "3000000";
//    public static String dataNum = "4500000";
//    public static String dataNum = "6000000";
//    public static String dataNum = "7500000";
    Constant.isGetSqls = true;
    Constant.ks = "keyspace2";
    Constant.cf = new String[]{"df1", "df2", "df3"};
    Constant.pkey = new int[]{2, 2, 2};
    Constant.ckname = new String[]{"c2", "c5", "c7"};
    Constant.isInt = new boolean[]{true, true, true};
    Constant.RNum = 3;
    Constant.isDiffReplica = true;
    Constant.isAccelerate = true;
    Constant.isPrint = true;
    Constant.isRecordProcess = true;
    Constant.SArecord = "SA_process_record.csv";

//    unify.isDiffReplicated = false;
//    unify.calculate_unit(new AckSeq[]{new AckSeq(new int[]{2, 3, 1}),
//        new AckSeq(new int[]{2, 3, 1}),
//        new AckSeq(new int[]{2, 3, 1})
//    });

    unify.isDiffReplicated = Constant.isDiffReplica;
    unify.combine();
  }
}
