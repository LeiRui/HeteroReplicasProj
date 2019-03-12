package cassandra;

import java.util.ArrayList;
import java.util.List;

/*
 用Unify算法的结果作为这里的输入参数，实证代价
 */
public class GeneralTest {

  public static void main(String[] args) {
    List<String> sqls = new ArrayList<String>();
    sqls.add(
        "select * from %s.%s where pkey=%d and ck1>=1 and ck1<=101 and ck2=51 and ck3=51 allow filtering;");
    sqls.add(
        "select * from %s.%s where pkey=%d and ck1=51 and ck2>=1 and ck2<=101 and ck3=51 allow filtering;");
    sqls.add(
        "select * from %s.%s where pkey=%d and ck1=51 and ck2=51 and ck3>=1 and ck3<=101 allow filtering;");

    List<String> sqls_formatted = new ArrayList<>();
    for (int i = 0; i < sqls.size(); i++) {
      sqls_formatted.add(String.format(sqls.get(i), "ks1", "cf1", 1));
      System.out.println(sqls_formatted.get(i));
    }

    General general = new General(sqls);

    double cost = general.getFactCost();

  }
}
