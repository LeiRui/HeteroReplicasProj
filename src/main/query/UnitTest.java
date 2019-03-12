package query;

import model.Column;

import java.util.ArrayList;
import java.util.List;

public class UnitTest {
  public static void main(String[] args) {
    int ckn = 3;

    int[] qpernum = new int[]{1,1,10};

    double[][] starts = new double[ckn][];
    double[][] lengths = new double[ckn][];
    for(int i=0;i<ckn;i++){
      starts[i] = new double[]{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1};
      lengths[i] = new double[]{0,0,0.154,0.308,0.385,0.077,0.077,0,0,0};
    }

    QueryPicture queryPicture = new QueryPicture(starts,lengths,qpernum,10);
    //queryPicture.getFromDist(lengths[1]);

    List<Column> CKdist = new ArrayList<Column>();
    double step = 1;
    List<Double> x = new ArrayList<Double>();
    for(int i = 1; i<=101; i++) {
      x.add((double)i);
    }
    List<Double> y = new ArrayList<Double>();
    for(int i = 1; i<=100; i++) {
      y.add(1.0);
    }
    Column ck1 = new Column(step, x, y);
    Column ck2 = new Column(step, x, y);
    Column ck3 = new Column(step, x, y);
    CKdist.add(ck1);
    CKdist.add(ck2);
    CKdist.add(ck3);

    // 输出符合描述参数分布的模拟确定查询参数集合
    List<RangeQuery[]> rangeQueries = queryPicture.getDefinite(CKdist);

    String sql = QueryPicture.getSql("cestbon","tb2",1,ckn,rangeQueries.get(0)[0]);
    System.out.println(sql);

  }
}
