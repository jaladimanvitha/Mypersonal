package com.ge.capital.dms.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.dao.UpdateDAO;

@Component
public class UpdateService {

	@Autowired
	UpdateDAO updateDAO;

	public void updateMetadata(String docId, Map<String, String> inputMetadataMap) {
		System.out.println("Entering updateMetadata in Service...");
		updateDAO.updateMetadata(docId, inputMetadataMap);
	}

	public void updateDocumentMetadata(String docType, Map<String, String> documentDetails) throws Exception {
		System.out.println("Entering deepLinkUpload in Service...");
		updateDAO.updateDocumentMetadata(docType, documentDetails);
	}

	public void updateDocumentTypeMetadata(String docType, String tableName, Map<String, String> updateParams) throws Exception {
		System.out.println("Entering deepLinkUpload in Service...");
		updateDAO.updateDocumentTypeMetadata(docType, tableName, updateParams);
	}
	
	public void updateManifestDoc(String docType, Map<String, String> updateParams) throws Exception {
		System.out.println("Entering Manifest Document Update in Service...");
		updateDAO.updateManifestDoc(docType, updateParams);
	}
	public int updateCustomerName(String partyNumber, String partyName) {

		return updateDAO.updateCustomerName(partyNumber, partyName);
	}

	public int locUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {

		return updateDAO.integFour(partyNumber, partyName, creditNumber, opportunityID);
	}

	public int loanUpdate(String creditNumber, String sequenceNumber, Date commencementDate, String metadata, String messageType) {
		try {
			return updateDAO.loanUpdate(creditNumber, sequenceNumber, commencementDate, metadata, messageType);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
