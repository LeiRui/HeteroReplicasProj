# 实验步骤
1. 在cassandra创建keyspace和table
2. 生成csv数据的java jar包上传到cassandra结点，并执行该jar包生成csv数据文件
3. 用cassandra-loader导入生成的csv数据，然后flush刷入磁盘
4. 查询sqls文件上传到cassandra结点
5. 命令行执行sqls查询，记录相关信息并返回结果用于分析

# 实验脚本例子
## 1. 在cassandra创建keyspace和table
```
create keyspace tpch with replication={'class':'SimpleStrategy','replication_factor':1};
use tpch;
create table defaulttable(
pkey int,
c1 int,
c2 int,
c3 TEXT,
c4 double,
c5 TEXT,
c6 TEXT,
c7 TEXT,
c8 int,
c9 TEXT,
primary key(pkey,c2,c5,c7)
) with dclocal_read_repair_chance=0;

create table sameopt(
pkey int,
c1 int,
c2 int,
c3 TEXT,
c4 double,
c5 TEXT,
c6 TEXT,
c7 TEXT,
c8 int,
c9 TEXT,
primary key(pkey,c5,c7,c2)
) with dclocal_read_repair_chance=0;

create table df1(
pkey int,
c1 int,
c2 int,
c3 TEXT,
c4 double,
c5 TEXT,
c6 TEXT,
c7 TEXT,
c8 int,
c9 TEXT,
primary key(pkey,c7,c5,c2)
) with dclocal_read_repair_chance=0;

create table df2(
pkey int,
c1 int,
c2 int,
c3 TEXT,
c4 double,
c5 TEXT,
c6 TEXT,
c7 TEXT,
c8 int,
c9 TEXT,
primary key(pkey,c7,c2,c5)
) with dclocal_read_repair_chance=0;

create table df3(
pkey int,
c1 int,
c2 int,
c3 TEXT,
c4 double,
c5 TEXT,
c6 TEXT,
c7 TEXT,
c8 int,
c9 TEXT,
primary key(pkey,c2,c5,c7)
) with dclocal_read_repair_chance=0;
```

注意：nodetool getendpoints指令可以用来查询特定的keyspace&table&partition_key所在的结点ip，例如
```
nodetool getendpoints tpch df1 1
```

## 2. 生成csv数据的jar包上传到cassandra结点，并执行
```
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;

public class exp1 {

  public static void main(String[] args) throws IOException {
    int num = 410000;
    PrintWriter pw1 = new PrintWriter(
        new OutputStreamWriter(new FileOutputStream("data" + num + "_pkey_1.csv")));
    PrintWriter pw2 = new PrintWriter(
        new OutputStreamWriter(new FileOutputStream("data" + num + "_pkey_2.csv")));
    PrintWriter pw3 = new PrintWriter(
        new OutputStreamWriter(new FileOutputStream("data" + num + "_pkey_3.csv")));
    pw1.println("pkey,ck1,ck2,ck3,ck4,ck5,ck6,ck7,ck8,ck9,ck10,value");
    pw2.println("pkey,ck1,ck2,ck3,ck4,ck5,ck6,ck7,ck8,ck9,ck10,value");
    pw3.println("pkey,ck1,ck2,ck3,ck4,ck5,ck6,ck7,ck8,ck9,ck10,value");
    int cnt = 0;
    Random random = new Random();
    for (int ck1 = 1; ck1 <= 10; ck1++) {
      for (int ck2 = 1; ck2 <= 10; ck2++) {
        for (int ck3 = 1; ck3 <= 10; ck3++) {
          for (int ck4 = 1; ck4 <= 10; ck4++) {
            for (int ck5 = 1; ck5 <= 10; ck5++) {
              for (int ck6 = 1; ck6 <= 10; ck6++) {
                for (int ck7 = 1; ck7 <= 10; ck7++) {
                  for (int ck8 = 1; ck8 <= 10; ck8++) {
                    for (int ck9 = 1; ck9 <= 10; ck9++) {
                      for (int ck10 = 1; ck10 <= 10; ck10++) {
                        pw1.println("1," + ck1 + "," + ck2 + "," + ck3 + "," + ck4 + "," + ck5 + ","
                            + ck6 + "," + ck7 + "," + ck8 + "," + ck9 + "," + ck10 + "," + random
                            .nextInt(100));
                        pw2.println("2," + ck1 + "," + ck2 + "," + ck3 + "," + ck4 + "," + ck5 + ","
                            + ck6 + "," + ck7 + "," + ck8 + "," + ck9 + "," + ck10 + "," + random
                            .nextInt(100));
                        pw3.println("3," + ck1 + "," + ck2 + "," + ck3 + "," + ck4 + "," + ck5 + ","
                            + ck6 + "," + ck7 + "," + ck8 + "," + ck9 + "," + ck10 + "," + random
                            .nextInt(100));
                        cnt++;
                        if (cnt >= num) {
                          break;
                        }
                      }
                      if (cnt >= num) {
                        break;
                      }
                    }
                    if (cnt >= num) {
                      break;
                    }
                  }
                  if (cnt >= num) {
                    break;
                  }
                }
                if (cnt >= num) {
                  break;
                }
              }
              if (cnt >= num) {
                break;
              }
            }
            if (cnt >= num) {
              break;
            }
          }
          if (cnt >= num) {
            break;
          }
        }
        if (cnt >= num) {
          break;
        }
      }
      if (cnt >= num) {
        break;
      }
    }
    pw1.close();
    pw2.close();
    pw3.close();
  }
}
```
生成的csv文件内容：
```
pkey,ck1,ck2,ck3,ck4,ck5,ck6,ck7,ck8,ck9,ck10,value
1,1,1,1,1,1,1,1,1,1,1,49
1,1,1,1,1,1,1,1,1,1,2,58
1,1,1,1,1,1,1,1,1,1,3,34
1,1,1,1,1,1,1,1,1,1,4,87
1,1,1,1,1,1,1,1,1,1,5,99
...
```

## 3. 用cassandra-loader导入生成的csv数据，然后flush刷入磁盘
```
./cassandra-loader -f data410000_pkey_1.csv -host localhost -schema "tpch.df1(pkey,c1,c2,c3,c4,c5,c6,c7,c8,c9)" -delim ","
./cassandra-loader -f data410000_pkey_2.csv -host localhost -schema "tpch.df2(pkey,c1,c2,c3,c4,c5,c6,c7,c8,c9)" -delim ","
./cassandra-loader -f data410000_pkey_3.csv -host localhost -schema "tpch.df3(pkey,c1,c2,c3,c4,c5,c6,c7,c8,c9)" -delim ","
./cassandra-loader -f data410000_pkey_1.csv -host localhost -schema "tpch.sameopt(pkey,c1,c2,c3,c4,c5,c6,c7,c8,c9)" -delim ","
./cassandra-loader -f data410000_pkey_1.csv -host localhost -schema "tpch.defaulttable(pkey,c1,c2,c3,c4,c5,c6,c7,c8,c9)" -delim ","
```
注意：一定要执行flush命令。否则后续查询得到的耗时是数据在内存中的情况。
cassandra nodeltool flush指令自己查一下。

## 4. 查询sqls文件（和sample过的用于warmup的sqls文件）上传到cassandra结点
sqls文件来自算法优化输出。
sample需要自己写个简单的程序，按照例如0.8的概率取样。

## 5. 命令行执行sqls查询（先warmup再正式计时），记录相关信息并返回结果用于分析
```
script finale_ds2.log

./cqlsh --request-timeout=20000000 -f tpch_df1_R1_52.74_sqls_sample.txt

time ./cqlsh --request-timeout=20000000 -f tpch_df1_R1_52.74_sqls.txt
```
注意：request-timeout设置得比较大，用于防止命令行查询时间过长断开。