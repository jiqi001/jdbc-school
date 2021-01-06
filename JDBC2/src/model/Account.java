package model;

public class Account {
	private String personnr;
	private String studentPK;
	
	public Account(String personnr) {
	this.personnr=personnr;
	}

	public Account(String personnr, String studentPK) {
		this.studentPK=studentPK;
		}
	
	public String getPersonnr() {
		return personnr;
	}
	public String getStudentPK() {
		return studentPK;
	}

}
