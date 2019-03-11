import algorithm.SA;
import common.Constant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Column;
import query.QueryPicture;
import replica.AckSeq;

/*
控制变量：副本数X
 */
public class Test2 {

  public static void main(String[] args) {
    // 数据分布参数
    BigDecimal totalRowNumber = new BigDecimal(Constant.dataNum);

    List<Column> CKdist = new ArrayList<Column>();

    // 直方图描述数据分布
    double step1 = 1;
    List<Double> x1 = new ArrayList<Double>();
    for (int i = 1; i <= 11; i++) {
      x1.add((double) i);
    }
    List<Double> y1 = new ArrayList<Double>();
    for (int i = 1; i <= 10; i++) {
      y1.add(1.0);
    }

    for (int i = 0; i < 8; i++) {
      Column ck1 = new Column(step1, x1, y1);
      CKdist.add(ck1);
    }
    int ckn = CKdist.size();

    // 查询参数

    //列内分布参数 谨慎修改
    double[][] starts = new double[ckn][];
    double[][] lengths = new double[ckn][];
    for (int i = 0; i < ckn; i++) {
      starts[i] = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
      lengths[i] = new double[]{0.08, 0.2, 0.28, 0.16, 0.12, 0.04, 0.04, 0.04, 0.04, 0};
    }

    int[] qpernum = new int[]{10, 10, 10, 10, 10, 1, 1, 1};
//        int[] qpernum = new int[]{38,6,6,6,6,6,6,6};
//        int[] qpernum = new int[]{37, 37, 1, 1, 1, 1, 1, 1};
//        int[] qpernum = new int[]{73,1,1,1,1,1,1,1};
//        int[] qpernum = new int[]{2,1,1,1,1,1,1,1};

    QueryPicture queryPicture = new QueryPicture(starts, lengths, qpernum, 10);

    //控制变量
    int X = Constant.RNum;

    SA unify = new SA(totalRowNumber,
        ckn, CKdist,
        Constant.fetchRowCnt, Constant.costModel_k, Constant.costModel_b,
        Constant.cost_session_around, Constant.cost_request_around,
        queryPicture,
        X);

    // 设置一些参数，也可以到Constant里统一修改
    Constant.isTranform = false;
    Constant.dataNum = "1500000000";
    Constant.isGetSqls = true;
    Constant.ks = "keyspace1";
    Constant.cf = new String[]{"df1", "df2", "df3"};
    Constant.pkey = new int[]{1, 1, 1};
    Constant.ckname = new String[]{"c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8"};
    Constant.isInt = new boolean[]{true, true, true, true, true, true, true, true};
    Constant.RNum = 3;
    Constant.isDiffReplica = true;
    Constant.isAccelerate = true;
    Constant.isPrint = true;
    Constant.isRecordProcess = true;
    Constant.SArecord = "SA_process_record.csv";


//    unify.calculate_unit(new AckSeq[]{new AckSeq(new int[]{8,2,3,4,5,6,7,1}),
//        new AckSeq(new int[]{1,2,3,4,5,6,7,8}),
//        new AckSeq(new int[]{1,2,3,4,5,6,7,8})
//    });

    unify.isDiffReplicated = Constant.isDiffReplica;
    unify.combine();

  }

}
