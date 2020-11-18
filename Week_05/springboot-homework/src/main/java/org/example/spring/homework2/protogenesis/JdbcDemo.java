package org.example.spring.homework2.protogenesis;

import java.sql.*;

/**
 * ClassName:JdbcDemo
 * Package:org.example.spring.homework2.protogenesis
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 23:39
 */
public class JdbcDemo {

    public static void main(String[] args) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            //1.注册驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2.获得连接
            String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
            conn = DriverManager.getConnection(url, "root", "root");

            // 增
            String insetSql = "insert into t_student(name, age) values(?, ?)";
            ps = conn.prepareStatement(insetSql);
            ps.setString(1,"金晨");
            ps.setInt(2, 30);
            int count = ps.executeUpdate();
            System.out.println("增加个数："+count);
            // 删
            String delSql = "delete from t_student where id = ?";
            ps = conn.prepareStatement(delSql);
            ps.setInt(1, 2);
            count = ps.executeUpdate();
            System.out.println("删除个数："+count);
            // 改
            String updateSql = "update t_student set name = ? where id = ?";
            ps = conn.prepareStatement(updateSql);
            ps.setString(1, "程潇");
            ps.setInt(2, 1);
            count = ps.executeUpdate();
            System.out.println("修改个数："+count);
            // 查
            String sql = "select id,name,age from t_student where id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, 1);
            //4. ResultSet
            rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("id")
                        + rs.getString("name")
                        + rs.getInt("age"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            // 5.关闭资源
            closeRes(conn, rs, ps);

        }
    }

    private static void closeRes(Connection conn, ResultSet rs, PreparedStatement ps) {
        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(ps != null){
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
