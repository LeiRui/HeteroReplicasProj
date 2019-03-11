package query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import common.Constant;
import model.Column;
/*
 查询集的描述参数类
 */
public class QueryPicture {

  // 排序键个数，也即单列范围查询不同列数量
  public int ckn;

  // 范围查询起点的直方图分布 10%间隔直方图
  public double[][] starts; // [ckn][10]

  // 范围查询长度的直方图分布 10%间隔直方图
  public double[][] lengths; // [ckn][10]

  // 点查询查询值的分布是在相应列上的均匀分布

  // 范围查询列之间的占比
  // 同一批次内，对第i列进行范围查询的查询个数:对第(i+1)列进行范围查询的查询个数 = qpernum[i-1]:qpernum[i]
  public int[] qpernum; // [ckn]


  public int totalQueryBatchNum;
  public int totalQueryNum;

  public QueryPicture(double[][] starts, double[][] lengths, int[] qpernum,
      int totalQueryBatchNum) {
    this.starts = starts;
    this.lengths = lengths;
    this.qpernum = qpernum;
    this.totalQueryBatchNum = totalQueryBatchNum;
    ckn = qpernum.length;
    int sum = 0;
    for (int i = 0; i < qpernum.length; i++) {
      sum += qpernum[i];
    }
    totalQueryNum = sum * totalQueryBatchNum;
  }

  // 输入各列真实分布参数、总查询批次，输出符合描述参数分布的模拟确定查询参数集合
  public List<RangeQuery[]> getDefinite(List<Column> CKdist) {
    List<RangeQuery[]> res = new ArrayList<>();
    for (int i = 0; i < ckn; i++) {
      RangeQuery[] res_ = new RangeQuery[totalQueryBatchNum * qpernum[i]];
      res.add(res_);
    }

    for (int i = 0; i < ckn; i++) {
      Column columnIan = CKdist.get(i);
      double[] dist_start = starts[i];
      double[] dist_length = lengths[i];
      RangeQuery[] res_ = res.get(i);
      int singleSum = res_.length;
      for (int j = 0; j < singleSum; j++) {
        // 确定这个batch内第i个ck列的单范围查询的描述参数数值
        double qck_r1_abs = (int) Math
            .round(getFromDist(dist_start) * (columnIan.xmax_ - columnIan.xmin_) + columnIan.xmin_);
        double qck_r2_abs = (int) Math
            .round(qck_r1_abs + getFromDist(dist_length) * (columnIan.xmax_ - columnIan.xmin_));
        // System.out.println("r1="+qck_r1_abs+" r2="+qck_r2_abs);

        // NOTE 这里有可能qck_r1_abs~qck_r2_abs超出列取值范围

        double[] qck_p_abs = new double[ckn];
        for (int z = 0; z < ckn; z++) { // 点查列从相应列的取值范围中均匀取值
          // 注意尽量保证点查询值在实际数据中存在且数据类型对应，否则查询时直接检查不存在返回，无法验证查询代价公式
          Random random = new Random();
          int totalStep = (int) ((CKdist.get(z).xmax_ - CKdist.get(z).xmin_) / CKdist.get(z).step_);
          int posStep = random.nextInt(totalStep);
          if (posStep == 0) {
            posStep = 1;
          }
          qck_p_abs[z] = CKdist.get(z).xmin_ + posStep * CKdist.get(z).step_;
        }
        res_[j] = new RangeQuery(i + 1, qck_r1_abs, qck_r2_abs, true, true, qck_p_abs);
      }
    }
    return res;
  }

  // 产生相应的Cassandra sql语句
  public static String getSql(String ks, String cf, int pkey, List<Column> CKdist, RangeQuery q) {
    String q_format = "select count(*) from " + ks + "." + cf + " where pkey=" + pkey;
    int ckn = CKdist.size();
    for (int k = 0; k < ckn; k++) {
      // 范围查询列
      if (k == q.qckn - 1) { // 注意qckn从1开始，所以这里要减1
        // int类型
        if (Constant.isInt[k]) {// 控制sql数据类型，避免数据类型是int的时候查询>2.0这样的浮点数形式导致直接检查不存在返回
          if (!Constant.isTranform) {
            String tmp = " and %s>=%d and %s<=%d";
            q_format += String
                .format(tmp, Constant.ckname[k], (int) q.qck_r1_abs, Constant.ckname[k],
                    (int) q.qck_r2_abs);
          } else { // 专门为tpch数据集的c5和c7的从数字转换到真实的查询准备的
            // c2
            if (Constant.ckname[k].equals("c2")) {
              String tmp = " and %s>=%d and %s<=%d";
              q_format += String
                  .format(tmp, Constant.ckname[k], (int) q.qck_r1_abs, Constant.ckname[k],
                      (int) q.qck_r2_abs);
            }
            // c5
            else if (Constant.ckname[k].equals("c5")) { // 转换成时间
              long val1 = (long) q.qck_r1_abs * 24 * 3600 * 1000;
              Date date1 = new Date(val1);
              SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
              String dateText1 = df1.format(date1);

              long val2 = (long) q.qck_r2_abs * 24 * 3600 * 1000;
              Date date2 = new Date(val2);
              SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
              String dateText2 = df2.format(date2);

              String tmp = " and %s>='%s' and %s<='%s'";
              q_format += String
                  .format(tmp, Constant.ckname[k], dateText1, Constant.ckname[k], dateText2);
            }
            // c7
            else { //转换成Clerk#...共15-byte
              String number = Integer.toString((int) q.qck_r1_abs);
              int zeroNum = 15 - 6 - number.length();
              String str = "Clerk#";
              for (int i = 0; i < zeroNum; i++) {
                str += "0";
              }
              str += number;

              String number2 = Integer.toString((int) q.qck_r2_abs);
              int zeroNum2 = 15 - 6 - number2.length();
              String str2 = "Clerk#";
              for (int i = 0; i < zeroNum2; i++) {
                str2 += "0";
              }
              str2 += number2;

              String tmp = " and %s>='%s' and %s<='%s'";
              q_format += String.format(tmp, Constant.ckname[k], str, Constant.ckname[k], str2);
            }
          }
        }
        // float类型
        else {
          String tmp = " and %s>=%.2f and %s<=%.2f";
          q_format += String
              .format(tmp, Constant.ckname[k], q.qck_r1_abs, Constant.ckname[k], q.qck_r2_abs);
        }
      }
      // 点查询列
      else {
        // int类型
        if (Constant.isInt[k]) {
          if (!Constant.isTranform) {
            q_format += " and " + Constant.ckname[k] + "=" + (int) q.qck_p_abs[k];
          } else { // 专门为tpch数据集的c5和c7的从数字转换到真实的查询准备的
            // c2
            if (Constant.ckname[k].equals("c2")) {
              String tmp = " and %s=%d";
              q_format += String.format(tmp, Constant.ckname[k], (int) q.qck_p_abs[k]);
              // c5
            } else if (Constant.ckname[k].equals("c5")) { // 转换成时间
              long val1 = (long) q.qck_p_abs[k] * 24 * 3600 * 1000;
              Date date1 = new Date(val1);
              SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
              String dateText1 = df1.format(date1);

              String tmp = " and %s='%s'";
              q_format += String.format(tmp, Constant.ckname[k], dateText1);
            }
            // c7
            else { // 转换成Clerk#...共15-byte
              String number = Integer.toString((int) q.qck_p_abs[k]);
              int zeroNum = 15 - 6 - number.length();
              String str = "Clerk#";
              for (int i = 0; i < zeroNum; i++) {
                str += "0";
              }
              str += number;

              String tmp = " and %s='%s'";
              q_format += String.format(tmp, Constant.ckname[k], str);
            }
          }
        }
        // float类型
        else {
          String tmp = " and %s=%.2f";
          q_format += String.format(tmp, Constant.ckname[k], q.qck_p_abs[k]);
        }
      }
    }
    q_format += " allow filtering;";
    return q_format;
  }

  /**
   * @param dist 直方统计形式 10%间隔的概率分布 dist[i]代表落在i*10%~(i+1)*10%上的概率
   * @return 符合分布概率的一个值
   */
  public double getFromDist(double[] dist) {
    double res = 0;
    double random = Math.random();
    double sum = 0;
    for (int i = 0; i < dist.length; i++) {
      sum += dist[i];
      if (random <= sum) { // i*10%~(i+1)*10%
        res = (double) (i + Math.random()) / 10;
        break;
      }
    }
    return res;
  }
}
