package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//import org.joda.time.DateTime;

@Entity
@Table(name = "manifest_report_doc")
public class ManifestReportDoc implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	private String docId;
	private String mft_creator_searched;
	private Date mft_creation_from_dt_searched;
	private Date mft_creation_to_dt_searched;
	private String mft_sequence_no;
	private String mft_storer_no;
	private String mft_business_loc;
	private String mft_sender_name;
	
	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public String getMft_creator_searched() {
		return mft_creator_searched;
	}
	
	public void setMft_creator_searched(String mft_creator_searched) {
		this.mft_creator_searched = mft_creator_searched;
	}
	
	public Date getMft_creation_from_dt_searched() {
		return new Date(mft_creation_from_dt_searched.getTime());
	}
	
	public void setMft_creation_from_dt_searched(Date mft_creation_from_dt_searched) {
		this.mft_creation_from_dt_searched = new Date(mft_creation_from_dt_searched.getTime());
	}
	
	public Date getMft_creation_to_dt_searched() {
		return new Date(mft_creation_to_dt_searched.getTime());
	}
	
	public void setMft_creation_to_dt_searched(Date mft_creation_to_dt_searched) {
		this.mft_creation_to_dt_searched = new Date(mft_creation_to_dt_searched.getTime());
	}
	
	public String getMft_sequence_no() {
		return mft_sequence_no;
	}
	
	public void setMft_sequence_no(String mft_sequence_no) {
		this.mft_sequence_no = mft_sequence_no;
	}
	
	public String getMft_storer_no() {
		return mft_storer_no;
	}
	
	public void setMft_storer_no(String mft_storer_no) {
		this.mft_storer_no = mft_storer_no;
	}
	
	public String getMft_business_loc() {
		return mft_business_loc;
	}
	
	public void setMft_business_loc(String mft_business_loc) {
		this.mft_business_loc = mft_business_loc;
	}
	
	public String getMft_sender_name() {
		return mft_sender_name;
	}
	
	public void setMft_sender_name(String mft_sender_name) {
		this.mft_sender_name = mft_sender_name;
	}
	
}
