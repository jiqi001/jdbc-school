package model;

public class Instrument {
	private int id;
	private String instrumentType;
	private String brand;
	private String leasePrice;

	public Instrument(int id,String instrumentType,String brand,String leasePrice){
		this.id = id;
		this.instrumentType= instrumentType;
		this.brand = brand;
		this.leasePrice = leasePrice;
	}
	public String getKind() {
		return this.instrumentType;
	}
	public int getID() {
		return this.id;
	}
	public String getBrand() {
		return this.brand;
	}
	public String getPrice() {
		return this.leasePrice;
	}
	public String InstrumentToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id:");
		sb.append(" this.id");
		sb.append(" instrument : "+ this.instrumentType);
		sb.append(" brand :"+this.brand);
		sb.append(" price" +this.leasePrice);
		sb.append("\n");
		return sb.toString();
	}
	

}
