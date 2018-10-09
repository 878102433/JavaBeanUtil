package application.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnConstraint;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLNotNullConstraint;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OraclePrimaryKey;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;

import application.model.DbColumn;
import application.model.JavaDB;
import application.model.TableFile;

public class FormatSql {
	private TableFile tableFile;
	public List<DbColumn> dbColumns;
	private StringBuilder lostDB;
	public String dbType;
	public static final String DB_TYPE_ORACLE = "Oracle";
	public static final String DB_TYPE_MYSQL = "MySQL";

	public FormatSql(TableFile tableFile, String dbType) {
		this.tableFile = null;
		if (tableFile != null) {
			this.tableFile = tableFile.clone();
		}
		this.dbType = dbType;
	}

	/**
	 * 格式sql语句
	 *
	 * @param sql
	 * @return
	 * @return
	 */
	public static String format(String sql) {
		return SQLUtils.formatMySql(sql);
	}

	public void createTableFile(String formatSql) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<JavaDB> javaDBs = tableFile.getJavaDBs();
		javaDBs.sort((c1, c2) -> {
			// 类型正序
			int compare = c1.getDbColumn().getDataType().compareTo(c2.getDbColumn().getDataType());
			if (compare == 0) {
				// 参数倒序
				if (c2.getDbColumn().getArguments() != null) {
					return c2.getDbColumn().getArguments().compareTo(c1.getDbColumn().getArguments());
				} else if (c1.getDbColumn().getArguments() != null) {
					return -c1.getDbColumn().getArguments().compareTo(c2.getDbColumn().getArguments());
				} else {
					return 0;
				}
			} else {
				return compare;
			}

		});
		dbColumns = new ArrayList<DbColumn>();
		lostDB = new StringBuilder();
		List<JavaDB> javaDBsClone = new ArrayList<JavaDB>();
		if (formatSql != null && formatSql.length() > 0) {
			String[] sentences = formatSql.split(";\n");
			for (String sentence : sentences) {
				SQLStatement statement = null;
				switch (dbType) {
				case FormatSql.DB_TYPE_ORACLE: {
					OracleStatementParser parser = new OracleStatementParser(sentence);
					statement = parser.parseStatement();
					break;
				}
				case FormatSql.DB_TYPE_MYSQL: {
					MySqlStatementParser parser = new MySqlStatementParser(sentence);
					statement = parser.parseStatement();
					break;
				}
				default: {
					break;
				}
				}
				if (statement != null) {
					if (statement instanceof SQLCreateTableStatement) {
						executeSQLCreateTableStatement((SQLCreateTableStatement) statement);
					} else if (statement instanceof SQLAlterTableStatement) {
						executeSQLAlterTableStatement((SQLAlterTableStatement) statement);
					} else if (statement instanceof SQLCommentStatement) {
						executeSQLCommentStatement((SQLCommentStatement) statement);
					}

				}
			}
		}

		// 匹配类型对应关系
		for (DbColumn dbColumn : dbColumns) {
			Optional<JavaDB> optJavaDB = javaDBs.stream().filter(x -> equalsDB(x, dbColumn)).findFirst();
			if (optJavaDB.isPresent()) {
				JavaDB javaDB = optJavaDB.get().clone();
				PropertyUtils.copyProperties(javaDB.getDbColumn(), dbColumn);
				javaDB.getJavaColumn().setColumnName(StringUtil.underline2Camel(dbColumn.getColumnName()));
				javaDBsClone.add(javaDB);

			} else {
				lostDB.append(dbColumn.getColumnName() + "\r\n");
			}

		}
		tableFile.setJavaDBs(javaDBsClone);
	}

	/**
	 * Create Table
	 *
	 * @param statement
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public void executeSQLCreateTableStatement(SQLCreateTableStatement statement) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		tableFile.setTableName(statement.getName().getSimpleName());
		tableFile.setFileName(StringUtil.underline2Pascal(tableFile.getTableName()));

		List<SQLTableElement> tableElements = statement.getTableElementList();
		for (SQLTableElement tableElement : tableElements) {

			if (tableElement instanceof OraclePrimaryKey) {
				OraclePrimaryKey primaryKey = (OraclePrimaryKey) tableElement;
				List<SQLExpr> columns = primaryKey.getColumns();

				for (SQLExpr expr : columns) {
					String columnName = expr.toString();
					Optional<DbColumn> optDbColumn = dbColumns.stream().filter(x -> equalsColumnName(x, columnName)).findFirst();
					if (optDbColumn.isPresent()) {
						optDbColumn.get().setPrimaryKey(true);
					}
				}
			}
			if (tableElement instanceof MySqlPrimaryKey) {
				MySqlPrimaryKey primaryKey = (MySqlPrimaryKey) tableElement;
				List<SQLExpr> columns = primaryKey.getColumns();

				for (SQLExpr expr : columns) {
					String columnName = expr.toString();
					Optional<DbColumn> optDbColumn = dbColumns.stream().filter(x -> equalsColumnName(x, columnName)).findFirst();
					if (optDbColumn.isPresent()) {
						optDbColumn.get().setPrimaryKey(true);
					}
				}
			} else if (tableElement instanceof SQLColumnDefinition) {
				SQLColumnDefinition columnDefinition = (SQLColumnDefinition) tableElement;
				DbColumn column = new DbColumn();
				column.setColumnName(columnDefinition.getName().getSimpleName());
				column.setDataType(columnDefinition.getDataType().getName());
				column.setArguments(StringUtils.join(columnDefinition.getDataType().getArguments().toArray(), ","));
				column.setComment(formatComment(StringUtil.toString(columnDefinition.getComment())));
				List<SQLColumnConstraint> constraints = columnDefinition.getConstraints();
				for (SQLColumnConstraint constraint : constraints) {
					if (constraint instanceof SQLColumnPrimaryKey) {
						column.setPrimaryKey(true);
					} else if (constraint instanceof SQLNotNullConstraint) {
						column.setNullEnable(true);
					}

				}
				dbColumns.add(column);
			}
		}
	};

	/**
	 * Alter Table
	 *
	 * @param statement
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public void executeSQLAlterTableStatement(SQLAlterTableStatement statement) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<SQLAlterTableItem> items = statement.getItems();
		if (statement.getTableSource().toString().equals(tableFile.getTableName())) {
			for (SQLAlterTableItem item : items) {
				if (item instanceof SQLAlterTableAddConstraint) {
					SQLAlterTableAddConstraint addConstraint = (SQLAlterTableAddConstraint) item;
					if (addConstraint.getConstraint() instanceof OraclePrimaryKey) {
						OraclePrimaryKey primaryKey = (OraclePrimaryKey) addConstraint.getConstraint();
						List<SQLExpr> columns = primaryKey.getColumns();

						for (SQLExpr expr : columns) {
							String columnName = expr.toString();
							Optional<DbColumn> optDbColumn = dbColumns.stream().filter(x -> equalsColumnName(x, columnName)).findFirst();
							if (optDbColumn.isPresent()) {
								optDbColumn.get().setPrimaryKey(true);
							}
						}
					} else if (addConstraint.getConstraint() instanceof MySqlPrimaryKey) {
						MySqlPrimaryKey primaryKey = (MySqlPrimaryKey) addConstraint.getConstraint();
						List<SQLExpr> columns = primaryKey.getColumns();

						for (SQLExpr expr : columns) {
							String columnName = expr.toString();
							Optional<DbColumn> optDbColumn = dbColumns.stream().filter(x -> equalsColumnName(x, columnName)).findFirst();
							if (optDbColumn.isPresent()) {
								optDbColumn.get().setPrimaryKey(true);
							}
						}
					}

				} else if (item instanceof SQLAlterTableAddColumn) {
					SQLAlterTableAddColumn addColumn = (SQLAlterTableAddColumn) item;
					List<SQLColumnDefinition> columnDefinitions = addColumn.getColumns();
					for (SQLColumnDefinition columnDefinition : columnDefinitions) {
						DbColumn column = new DbColumn();
						column.setColumnName(columnDefinition.getName().getSimpleName());

						column.setDataType(columnDefinition.getDataType().getName());

						column.setArguments(StringUtils.join(columnDefinition.getDataType().getArguments().toArray(), ","));

						column.setComment(formatComment(StringUtil.toString(columnDefinition.getComment())));
						List<SQLColumnConstraint> constraints = columnDefinition.getConstraints();
						for (SQLColumnConstraint constraint : constraints) {
							if (constraint instanceof SQLColumnPrimaryKey) {
								column.setPrimaryKey(true);
							} else if (constraint instanceof SQLNotNullConstraint) {
								column.setNullEnable(true);
							}

						}
						dbColumns.add(column);
					}
				}
			}
		}
	};

	/**
	 * Comment
	 *
	 * @param statement
	 */
	public void executeSQLCommentStatement(SQLCommentStatement statement) {
		if ("COLUMN".equals(statement.getType().toString()) && subStringTable(statement.getOn().toString()).equals(tableFile.getTableName())) {
			Optional<DbColumn> optDbColumn = dbColumns.stream().filter(x -> equalsColumnName(x, subStringColumn(statement.getOn().toString()))).findFirst();
			if (optDbColumn.isPresent()) {
				optDbColumn.get().setComment(formatComment(statement.getComment().toString()));
			}
		} else if (statement.getOn().toString().equals(tableFile.getTableName())) {
			tableFile.setComment(formatComment(statement.getComment().toString()));
		}
	};

	public TableFile getTableFile() {
		return tableFile;
	}

	public void setTableFile(TableFile tableFile) {
		this.tableFile = null;
		if (tableFile != null) {
			this.tableFile = tableFile.clone();
		}
	}

	public List<DbColumn> getDbColumns() {
		return dbColumns;
	}

	public void setDbColumns(List<DbColumn> dbColumns) {
		this.dbColumns = dbColumns;
	}

	public StringBuilder getLostDB() {
		return lostDB;
	}

	public void setLostDB(StringBuilder lostDB) {
		this.lostDB = lostDB;
	}

	public static boolean equalsDB(JavaDB javaDB, DbColumn column) {
		if (javaDB.getDbColumn() != null) {
			DbColumn dbColumn = javaDB.getDbColumn();
			if (equalsDataType(dbColumn, column) && equalsArguments(dbColumn, column) && equalsPrimaryKey(dbColumn, column) && equalsNullEnable(dbColumn, column)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * 验证名称是否一致
	 *
	 * @param dbColumn
	 * @param column
	 * @return
	 */
	public static boolean equalsColumnName(DbColumn dbColumn, String columnName) {
		if (columnName.equalsIgnoreCase(dbColumn.getColumnName())) {
			return true;
		}
		return false;

	}

	/**
	 * 验证数据类型是否一致
	 *
	 * @param dbColumn
	 * @param column
	 * @return
	 */
	public static boolean equalsDataType(DbColumn dbColumn, DbColumn column) {
		if (column.getDataType().equalsIgnoreCase(dbColumn.getDataType())) {
			return true;
		}
		return false;

	}

	/**
	 * 验证数据参数是否一致
	 *
	 * @param dbColumn
	 * @param column
	 * @return
	 */
	public static boolean equalsArguments(DbColumn dbColumn, DbColumn column) {
		if (!StringUtil.isEmptyOrNull(dbColumn.getArguments()) && dbColumn.getArguments().equals(column.getArguments())) {
			column.setArguments(StringUtil.empty2Null(column.getArguments()));
			return true;
		}
		return false;

	}

	/**
	 * 验证是否为主键一致性
	 *
	 * @param dbColumn
	 * @param column
	 * @return
	 */
	public static boolean equalsPrimaryKey(DbColumn dbColumn, DbColumn column) {
		if (StringUtil.isBooleanTrue(dbColumn.getPrimaryKey()) == StringUtil.isBooleanTrue(column.getPrimaryKey())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证数据能否为空一致性
	 *
	 * @param dbColumn
	 * @param column
	 * @return
	 */
	public static boolean equalsNullEnable(DbColumn dbColumn, DbColumn column) {
		if (StringUtil.isBooleanTrue(dbColumn.getNullEnable()) == StringUtil.isBooleanTrue(column.getNullEnable())) {
			return true;
		} else {
			return false;
		}
	}

	private String subStringTable(String on) {
		if (on.indexOf(".") > 0) {
			return on.substring(0, on.indexOf("."));
		}
		return null;
	}

	private String subStringColumn(String on) {
		if (on.indexOf(".") > 0) {
			return on.substring(on.indexOf(".") + 1);
		}
		return null;
	}

	private String formatComment(String comment) {
		if (comment != null && comment.startsWith("'") && comment.endsWith("'")) {
			return comment.substring(1, comment.length() - 1);
		}
		return comment;

	}

}