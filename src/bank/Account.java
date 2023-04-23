package bank;

public class Account {
	private double balance;
	
	public Account (double _balance) {
		balance = _balance;
	}
	
	public synchronized void deposit (double amount) {
		balance +=amount;
	}
	
	public void withdraw (double amount) {
		balance -=amount;
	}
	
	public double getBalance () {
		return balance;
	}
	
	public void setBalance (double _balance) {
		balance = _balance;
	}
}