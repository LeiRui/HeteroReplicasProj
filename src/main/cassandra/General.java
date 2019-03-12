package cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import java.util.List;

public class General {
  public static String node = "127.0.0.1";

  //实证查询代价
  public double Cost;

  public List<String> Xsqls;

  public General(List<String> sqls) {
    Xsqls = sqls;
  }

  /**
   * 实证测试代价
   */
  public double getFactCost() {
    Cluster cluster = Cluster.builder().addContactPoint(node).build();
    Session session = cluster.connect();

    int qnum = Xsqls.size();

    // warm up
    for (int z = 0; z < qnum; z++) {
      double r = Math.random();
      if (r < 0.8) { // 以0.8的概率warmup
        ResultSet rs = session.execute(Xsqls.get(z));
        int tmp = rs.all().size(); // 起到一个遍历全部结果的作用}
      }
    }

    // 正式计时查询
    long elapsed = System.nanoTime();
    for (int j = 0; j < qnum; j++) {
      String sql = Xsqls.get(j);
      ResultSet rs = session.execute(sql);
      int tmp = rs.all().size(); // 起到一个遍历全部结果的作用
    }
    elapsed = System.nanoTime() - elapsed;
    Cost = elapsed / (double) Math.pow(10, 6); // unit: ms
    System.out.printf("Cost=%fms\n", Cost);

    session.close();
    cluster.close();

    return Cost;
  }
}
