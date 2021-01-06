package model;

public class Rental {
	private String studentID = null;
	private String instrumentID = null;
	private String rentDate = null;
	private String returnDate = null;
	
	public Rental(String studentID, String instrumentID, String rentDate) {
		this.studentID = studentID;
		this.instrumentID = instrumentID;
		this.rentDate = rentDate;
	}
	
	public Rental(String studentID, String instrumentID, String rentDate, String returnDate) {
		this.studentID = studentID;
		this.instrumentID = instrumentID;
		this.rentDate = rentDate;
		this.returnDate = returnDate;
	}
	
	public String getStudentID() {
		return this.studentID;
	}
	
	public String getInstrumentID() {
		return this.instrumentID;
	}
	
	public String getRentDate() {
		return this.rentDate;
	}
	
	public String getReturnDate() {
		return this.returnDate;
	}

}
