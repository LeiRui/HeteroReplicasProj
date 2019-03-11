package model;

import java.util.List;

/*
  列分布统计信息 直方图形式
 */
public class Column {
  public double step_;
  private List<Double> x_;
  private List<Double> y_;
  public double xmin_;
  public double xmax_;
  private int sum_;


  /**
   * 输入直方频数统计信息
   * @param x  note: x比y多一项，因为y是每段的值，x是标点，x最后一个是取不到的终止位
   * @param y
   */
  public Column(double step, List<Double> x, List<Double> y) {
    this.step_ = step;
    this.x_ = x;
    this.y_ = y;
    this.xmin_= x.get(0);
    this.xmax_ = x.get(x.size()-1); // TODO 这个x的结束位真是有些些麻烦
    sum_=0;
    for(double num: y_) {
      sum_+=num;
    }
  }

  // TODO 当测试点查询一个不存在的数值时，耗时会非常小，这个要注意，因为代价公式默认假设查询值存在
  // TODO 注意step设置得非常小的时候可能会出现的问题

  public double getPoint(double p) {
    int index = 0;
    for(; index < x_.size()-1; index++) {
      if(p>=x_.get(index) && p<x_.get(index + 1)) {
        break;
      }
    }
    int number = (int)Math.round((x_.get(index+1)-x_.get(index))/step_);
    return (double)(y_.get(index))/sum_/number;
  }

  public enum rangeType {
    LcRc,LcRo,LoRc,LoRo
  }

  public double getBetween(double r1, double r2, rangeType type) {
//        double res = 0;
//
//        for(int i = 0; i < x_.size()-1; i++) {
//            int number = (int)Math.round((x_.get(i+1)-x_.get(i))/step_);
//            double pos = x_.get(i);
//            while(pos < x_.get(i+1)) {
//                if(isIn(pos, r1, r2, type)) {
//                    res += (double)(y_.get(i))/(sum_*number);
//                }
//                pos += step_;
//            }
//        }
//        return res;

    double res = 0;
    //先找出[r1,r2]覆盖的范围段的左右两端
    int x1=0,x2=0;
    boolean isx1=false;
    int i = 0;
    int left=0;
    for (; i < x_.size()-1; i++) {
      if(x_.get(i)<r1) {
        left = i;
      }

      if(isIn(x_.get(i),r1,r2,type)) {
        if(!isx1) { // 第一个落到的
          x1=i;
          isx1=true;
        }
        res+=(double)(y_.get(i))/sum_;
      }
      else if(isx1) { // 已经开始落到，所以这是末尾out
        x2=i;
        break;
      }
    }
    if(!isx1) { // 没有分割点落在这个range，换句话说这个range之落到一个interval内
//            int number = (int)Math.round((x_.get(left+1)-x_.get(left))/step_);
//            double tmpStep = (double)(y_.get(left))/(sum_*number);
      res = (y_.get(left))/sum_*(r2-r1)/(x_.get(left+1)-x_.get(left));
      return res;
    }

    if(i==x_.size()-1) {
      if(!isIn(x_.get(i),r1,r2,type)){
        x2=i;
      }
    }
    //添加r1到x_.get(x1)之间的
    if(x1>0) {
//            double tmp = (double)(y_.get(x1-1))/sum_*(double)(r1-x_.get(x1))/(x_.get(x1)-x_.get(x1-1));
      long n = (long)Math.floor((x_.get(x1)-r1)/step_);
      long number = (long)Math.round((x_.get(x1)-x_.get(x1-1))/step_);
      double tmp = (double)(y_.get(x1-1))/sum_/number*n;
      res+=tmp;
    }
    //减去x2到r2之间的
    if(x2>0) {
//            double tmp = (double)(y_.get(x2-1))/sum_*(double)(x_.get(x2)-r2)/(x_.get(x2)-x_.get(x2-1));
      int n = (int)Math.floor((x_.get(x2)-r2)/step_);
      //不要全部减去了 至少r2的一个step留下
      if(n>0) {
        n--;
      }
      int number = (int)Math.round((x_.get(x2)-x_.get(x2-1))/step_);
      double tmp = (double)(y_.get(x2-1))/sum_/number*n;
      res-=tmp;
    }
    return res;
  }

  private boolean isIn(double pos, double r1, double r2, rangeType type) {
    switch (type) {
      case LcRc:
        if(pos >= r1 && pos <= r2)
          return true;
        else
          return false;
      case LcRo:
        if(pos >= r1 && pos < r2)
          return true;
        else
          return false;
      case LoRc:
        if(pos > r1 && pos <= r2)
          return true;
        else
          return false;
      case LoRo:
        if(pos > r1 && pos < r2)
          return true;
        else
          return false;
      default:
        return false;
    }
  }


}
