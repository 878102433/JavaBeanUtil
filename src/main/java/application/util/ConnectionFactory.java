package application.util;

import java.sql.Connection;

import com.alibaba.druid.pool.DruidDataSource;

public class ConnectionFactory {

	private static DruidDataSource dataSource = null;

	public static boolean init(String url, String userName, String password) {
		try {
			dataSource = new DruidDataSource();
			dataSource.setUrl(url);
			dataSource.setUsername(userName);
			dataSource.setPassword(password);
			dataSource.setInitialSize(5);
			dataSource.setMinIdle(1);
			dataSource.setMaxActive(10);
			dataSource.setPoolPreparedStatements(false);
			// 获取Oracle元数据 REMARKS信息
			dataSource.getConnectProperties().put("remarksReporting", "true");
			// 获取MySQL元数据 REMARKS信息
			dataSource.getConnectProperties().put("useInformationSchema", "true");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static synchronized Connection getConnection() {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (Exception e) {
			DialogUtil.showException("连接数据库", "连接数据库失败，以下是具体异常：", e);
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e1) {
					e.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return conn;
	}

}