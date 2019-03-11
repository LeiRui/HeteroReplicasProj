package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/*
  改变数据存储结构对应查询代价建模

  单查询代价计算模型
 */
public class CostModel {
  public BigDecimal totalRowNumber;

  private int ckn; //排序键的数量

  private int[] ackSeq; // 记录一下
  private List<Column> ACKdist;//按照ACK顺序映射后的排序列的分布参数

  private int qackn;//按照ACK顺序映射后的范围查询的列，从0开始

  private double qck_r1;//范围查询的左端点在该列分布的位置百分比
  private double qck_r2;//范围查询的右端点在该列分布的位置百分比
  private Column.rangeType type;

  private double[] qack_p;//按照ACK顺序映射后,其余ckn-1个点查询值在该列分布的位置百分比，有占位

  public double resP; // 最后的总概率

  /**
   *
   * @param ks
   * @param cf
   * @return
   */
  // TODO double int sql
  public String getSql(String ks, String cf) {
    String q_format = "select * from "+ks+"."+cf+" where pkey=%d";
    for(int i=0;i<ckn;i++) {
      if(i==qackn) {
        switch (type) {
          case LcRc:
            q_format += " and ck" + ackSeq[i] + ">=" + (int)qck_r1 + " and ck" + ackSeq[i] + "<=" + (int)qck_r2;
            break;
          case LcRo:
            q_format += " and ck" + ackSeq[i] + ">=" + (int)qck_r1 + " and ck" + ackSeq[i] + "<" + (int)qck_r2;
            break;
          case LoRc:
            q_format += " and ck" + ackSeq[i] + ">" + (int)qck_r1 + " and ck" + ackSeq[i] + "<=" + (int)qck_r2;
            break;
          case LoRo:
            q_format += " and ck" + ackSeq[i] + ">" + (int)qck_r1 + " and ck" + ackSeq[i] + "<" + (int)qck_r2;
            break;
          default:
            break;
        }
      }
      else {
        q_format+=" and ck"+ackSeq[i]+"="+(int)qack_p[i];
      }
    }
    q_format+=" allow filtering;";

    return q_format;
  }

  public CostModel(BigDecimal totalRowNumber, int ckn, List<Column> CKdist,
      int qckn,
      double qck_r1_abs, double qck_r2_abs, // TODO 关于范围查询区间的开闭暂时不考虑进代价模型，都作闭考虑
      double[] qck_p_abs,
      int[] ackSeq){
    this.ackSeq = ackSeq;
    this.totalRowNumber = totalRowNumber;
    this.ckn = ckn;
    this.qck_r1=qck_r1_abs;
    this.qck_r2=qck_r2_abs;
    this.type = Column.rangeType.LcRc;
    ACKdist = new ArrayList<Column>();
    qack_p = new double[ckn];
    for(int i = 0; i < ckn; i++) { //按照ACK顺序映射
      int ackindex = ackSeq[i]-1;
      Column pqColumn = CKdist.get(ackindex);
      ACKdist.add(pqColumn);
      qack_p[i]=qck_p_abs[ackindex];
      if(ackSeq[i] == qckn) {
        qackn = i; // qackn start from 0 while qckn start from 1
      }
    }
  }

  /**
   * @return HR
   */
  @Deprecated
  public BigDecimal calculate() {
    resP = 1;
    for (int i = 0; i < qackn; i++) { // qackn是从0开始的，这里刚好表示qckn-1个
      resP *= ACKdist.get(i).getPoint(qack_p[i]);
    }
    switch (type) {
      case LcRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRc);
        break;
      case LcRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRo);
        break;
      case LoRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRc);
        break;
      case LoRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRo);
        break;
    }
    return totalRowNumber.multiply(new BigDecimal(resP));
    // 这里不能保留2位小数什么的，因为这里如果totalRowNumber给小了，结果HR就是0.02这种，再一保留两位小数就都一样了
  }


  /**
   * 新的单查询代价模型
   * 尽量精确估计
   *
   * @param fetchRowSize
   * @param costModel_k
   * @param costModel_b
   * @param cost_session_around
   * @param cost_request_around
   * @return 估计的单查询查询时间代价 单位us
   */
  public double calculate(int fetchRowSize, double costModel_k, double costModel_b, double cost_session_around, double cost_request_around) {
    resP = 1;
//        for(int i=0;i<ckn;i++) {
//            System.out.println(qack_p[i]);
//        }
    for (int i = 0; i < qackn; i++) { // qackn是从0开始的，这里刚好表示qckn-1个
      //System.out.println(qack_p[i]);
      resP *= ACKdist.get(i).getPoint(qack_p[i]);
    }
    switch (type) {
      case LcRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRc);
        break;
      case LcRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRo);
        break;
      case LoRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRc);
        break;
      case LoRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRo);
        break;
    }
//        System.out.println("resP："+resP);
    BigDecimal candidate_rows_cnt = totalRowNumber.multiply(new BigDecimal(resP)); // 候选集行数估计

    for (int i = qackn+1; i < ckn; i++) {
      resP *= ACKdist.get(i).getPoint(qack_p[i]);
    }
    BigDecimal result_rows_cnt = totalRowNumber.multiply(new BigDecimal(resP)); // 结果行数估计

        /*
          代价模型：
          result_row_cnt = fetchRowCnt*n+m
          Cost = cost_session_around+(n+1)*(cost_request_around+b)+k*candidate_rows_cnt;
         */
    BigDecimal n = result_rows_cnt.divide(new BigDecimal(fetchRowSize)).setScale(0, RoundingMode.FLOOR);
    BigDecimal nplus1 = n.add(new BigDecimal("1"));
    BigDecimal cost_part = nplus1.multiply(new BigDecimal(cost_request_around+costModel_b));

    BigDecimal res = new BigDecimal(cost_session_around).add(cost_part).add(candidate_rows_cnt.multiply(new BigDecimal(costModel_k)));
    res = res.divide(new BigDecimal("1000000")); // unit:s
//        System.out.println("res: "+res.doubleValue());
    return res.doubleValue();
  }

  /**
   * @param rowSize
   * @return HB
   */
  @Deprecated
  public BigDecimal calculate(int rowSize, int blockSize) {
    resP = 1;
    for (int i = 0; i < qackn; i++) { // qackn是从0开始的，这里刚好表示qckn-1个
      resP *= ACKdist.get(i).getPoint(qack_p[i]);
    }
    switch (type) {
      case LcRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRc);
        break;
      case LcRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LcRo);
        break;
      case LoRc:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRc);
        break;
      case LoRo:
        resP *= ACKdist.get(qackn).getBetween(qck_r1, qck_r2, Column.rangeType.LoRo);
        break;
    }
    return totalRowNumber.multiply(new BigDecimal(resP)).multiply(new BigDecimal(rowSize))
        .divide(new BigDecimal(blockSize)).setScale(0, RoundingMode.CEILING); // TODO blockSize default 64KB
  }
}
