import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Customer;
import model.Order;

public class CustomerOrderDeadlock {
	
	public ArrayList<Order> updateCustomerOrderStatus(int customerId, String orderStatus) throws SQLException {
				
		Connection conn = Database.getConnection(Connection.TRANSACTION_SERIALIZABLE);
		
		PreparedStatement updateCustomer = null;
		PreparedStatement selectOrders = null;		
		
		String updateCustomerOrderStatusSql = 
				"UPDATE CUSTOMERS "
				+ "SET LatestOrderStatus = ? "
				+ "WHERE CustomerId = ? ";
		
		String selectOrdersSql = 
				"SELECT * "
				+ "FROM ORDERS "
				+ "WHERE CustomerId = ? ";
		
		try {
			conn.setAutoCommit(false);
			
			Database.printSessionInfo(conn);
						
			System.out.println(Thread.currentThread().getName() + " request X lock on CUSTOMERS");
			
			updateCustomer = conn.prepareStatement(updateCustomerOrderStatusSql);
			updateCustomer.setString(1, orderStatus);
			updateCustomer.setInt(2, customerId);
			updateCustomer.executeUpdate();			
			
			System.out.println(Thread.currentThread().getName() + " request S lock on CUSTOMERS");
			
			selectOrders = conn.prepareStatement(selectOrdersSql);
			selectOrders.setInt(1, customerId);
			ResultSet rows = selectOrders.executeQuery();
			
			ArrayList<Order> orders = new ArrayList<Order>();
			
			while(rows.next()) {
				orders.add(new Order(
						rows.getInt(1), // ORDER_ID
						rows.getInt(2), // CUSTOMER_ID
						rows.getDate(3), // DATE
						rows.getFloat(4), // TOTAL
						rows.getString(5) // STATUS
						));
			}
			
			conn.commit();
			
			return orders;
			
		} catch (SQLException ex) {
			
			conn.rollback();
			throw ex;
			
		} finally {			
			conn.setAutoCommit(true);
		}			
	}
	
	
	public Customer addOrder(int customerId, float orderTotal) throws SQLException {

		Connection conn = Database.getConnection(Connection.TRANSACTION_SERIALIZABLE);

		PreparedStatement insertOrder = null;
		PreparedStatement selectCustomer = null;
		PreparedStatement updateCustomer = null;
				
		String insertOrderSql = 
				"INSERT INTO ORDERS (CustomerId, Date, Total, Status) "
				+ "VALUES (?, GETDATE(), ?, 'A')";
		
		String selectCustomerSql = 
				"SELECT CustomerId, Name, 'A' as LatestOrderStatus "
				+ "FROM CUSTOMERS "
				+ "WHERE CustomerId = ?";
		
		String updateCustomerSql = 
				"UPDATE CUSTOMERS "
				+ "SET LatestOrderStatus = 'A' "
				+ "WHERE CustomerId = ? ";
		
		try {
			conn.setAutoCommit(false);
						
			Database.printSessionInfo(conn);
			
			System.out.println(Thread.currentThread().getName() + " request X lock on ORDERS");
			
			insertOrder = conn.prepareStatement(insertOrderSql);
			insertOrder.setInt(1, customerId);
			insertOrder.setFloat(2, orderTotal);
			insertOrder.executeUpdate();

			System.out.println(Thread.currentThread().getName() + " request S lock on CUSTOMERS");
			
			selectCustomer = conn.prepareStatement(selectCustomerSql);
			selectCustomer.setInt(1, customerId);
			ResultSet rows = selectCustomer.executeQuery();
			
			Customer customer = null;

			if(rows.next()) {
				customer = new Customer(
						rows.getInt(1), 
						rows.getString(2),
						rows.getString(3)
						);
			}

			System.out.println(Thread.currentThread().getName() + " request X lock on CUSTOMERS");
			
			updateCustomer = conn.prepareStatement(updateCustomerSql);
			updateCustomer.setInt(1, customerId);
			updateCustomer.executeUpdate();
			
			conn.commit();
			
			return customer;
			
		} catch (SQLException ex) {
			
			conn.rollback();
			throw ex;
			
		} finally {
			
			conn.setAutoCommit(true);
		}		
	}
}
