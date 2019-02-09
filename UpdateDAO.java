package com.ge.capital.dms.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.CommonDoc;
import com.ge.capital.dms.entity.CorptaxDoc;
import com.ge.capital.dms.entity.DealDoc;
import com.ge.capital.dms.entity.GEReportsDoc;
import com.ge.capital.dms.entity.Lockbox;
import com.ge.capital.dms.entity.ManifestReportDoc;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@Configuration
@EnableTransactionManagement
public class UpdateDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Autowired
	private DmsUtilityService dmsUtilityService;

	@Transactional
	@SuppressWarnings("unchecked")
	public void updateMetadata(String docId, Map<String, String> inputMetadataMap) {
		System.out.println("Entering updateMetadata() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);

			Set<String> inputKeys = inputMetadataMap.keySet();

			String prefix = "";
			String updateKey = "";

			if (inputMetadataMap.containsKey("docMetaKey") || inputMetadataMap.containsKey("docMetaVal")) {
				updateKey = "document.update2.clause";
				prefix = "meta.";
			} else {
				updateKey = "document.update1.clause";
				prefix = "document.";
			}

			String whereKey = "document.where.clause";

			String updateQuery = "";

			updateQuery = props.getProperty(updateKey);
			// filters the required input attributes for metadata search
			for (String key : inputKeys) {
				updateQuery = updateQuery + " " + props.getProperty(prefix + key) + "=:" + key + " ,";
			}

			updateQuery = updateQuery.substring(0, updateQuery.length() - 1);

			updateQuery = updateQuery + " " + props.getProperty(whereKey) + " AND "
					+ props.getProperty(prefix + "docId") + "='" + docId + "'";

			if (prefix.equals("meta."))
				updateQuery = updateQuery + " AND " + props.getProperty(prefix + "docMetaKey") + "=:"
						+ props.getProperty(prefix + "docMetaKey");

			System.out.println("updateQuery......." + updateQuery);

			Query invoicesearchQuery = em.createQuery(updateQuery);

			// set the named query values for the metadata inputs
			for (final Iterator iter = inputMetadataMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Entry) iter.next();
				final String key = (String) entry.getKey();
				try {
					if (key.contains("Date")) {
						String oldDate = inputMetadataMap.get(key);
						Date date = df.parse(oldDate);
						invoicesearchQuery.setParameter(key, date, TemporalType.TIMESTAMP);
					} else
						invoicesearchQuery.setParameter(key, inputMetadataMap.get(key));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			em.joinTransaction();
			System.out.println("invoicesearchQuery......." + updateQuery);
			invoicesearchQuery.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			em.close();
		}
	}
	@Transactional
	public void updateManifestDoc(String docType,Map<String, String> updateParams) {
		EntityManager em = emf.createEntityManager();
		try {
			em.joinTransaction();
			Session session = em.unwrap(org.hibernate.Session.class);
			ManifestReportDoc manifestdoc = new ManifestReportDoc();
			System.out.println(updateParams.get("docID")+"Inside DAO");
			
			manifestdoc.setMftBusinessLoc(updateParams.get("businessLocation"));
			manifestdoc.setMftSequenceNo(updateParams.get("manifestSeqNumber"));
			manifestdoc.setMftStorerNo(updateParams.get("storerNumber"));
			manifestdoc.setMftSenderName(updateParams.get("sendersName"));
			manifestdoc.setMftCreatorSearched(updateParams.get("mft_creater_searched"));
			manifestdoc.setDocId(updateParams.get("mft_modifier_searched"));
			manifestdoc.setDocId(updateParams.get("docID"));
			if(! updateParams.get("creation_frm_dt_searched").isEmpty())
				manifestdoc.setMftCreationFromDtSearched((java.sql.Date) new SimpleDateFormat("MM/dd/yyyy").parse(updateParams.get("creation_frm_dt_searched")));
			
			if(! updateParams.get("creation_to_dt_searched").isEmpty())
				manifestdoc.setMftCreationToDtSearched((java.sql.Date) new SimpleDateFormat("MM/dd/yyyy").parse(updateParams.get("creation_to_dt_searched")));
			CommonDoc commonDoc = new CommonDoc();
			commonDoc.setDocId(updateParams.get("docID"));
			commonDoc.setDocName(updateParams.get("docName"));
			//commonDoc.setDocVersionId(documentDetails.get("docVersionId"));
			//commonDoc.setDocName(documentDetails.get("docName"));
			commonDoc.setDocTitle(updateParams.get("docName"));
			commonDoc.setDocType("Manifest_Report");
			//commonDoc.setDocSource(documentDetails.get("docSource"));
			//commonDoc.setMimeType(documentDetails.get("mimeType"));
			//commonDoc.setIsMigrated(documentDetails.get("isMigrated"));
			//commonDoc.setPermName(documentDetails.get("permName"));
			//commonDoc.setRealmName(documentDetails.get("realmName"));
			//commonDoc.setOwnerName(documentDetails.get("ownerName"));
			
			//if(!documentDetails.get("createDate").isEmpty())
			commonDoc.setCreateDate(new Timestamp(new Date().getTime()));
			//if(! documentDetails.get("retentionDate").isEmpty())
			//commonDoc.setRetentionDate(java.sql.Timestamp.valueOf(documentDetails.get("retentionDate")));
			//commonDoc.setCreator(documentDetails.get("creator"));
			//if(!documentDetails.get("modifyDate").isEmpty())
			//commonDoc.setModifyDate(java.sql.Timestamp.valueOf(documentDetails.get("modifyDate")));
			//commonDoc.setModifier(documentDetails.get("modifier"));
			//commonDoc.setIsDeleted(documentDetails.get("isDeleted"));
			//commonDoc.setIsCurrent(documentDetails.get("isCurrent"));		
			//if (documentDetails.get("versionNum") != null)
				//commonDoc.setVersionNum(Long.parseLong(documentDetails.get("versionNum")));
			commonDoc.setDocState("INTERIM");
			//commonDoc.setContentRef(documentDetails.get("contentRef"));
			//commonDoc.setIsLocked(documentDetails.get("isLocked"));
			//commonDoc.setFolderRef(documentDetails.get("folderRef"));
			
			em.joinTransaction();
			em.persist(commonDoc);
			em.persist(manifestdoc);
			session.save(manifestdoc);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	@SuppressFBWarnings(value = { "REC_CATCH_EXCEPTION"}, justification = "let me just make the build pass")
	public void updateDocumentTypeMetadata(String docType, String tableName, Map<String, String> updateParams) throws Exception {
		System.out.println("Entering updateDocumentTypeMetadata() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {			
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String documentType = docType;
			String updateKey = documentType + ".update." + tableName + ".clause";
			String whereKey = documentType + ".where." + tableName + ".clause";

			// for (Map<String, String> updateParam : updateParams) {
			String updateQuery = props.getProperty(updateKey);
			Set<String> updateKeys = updateParams.keySet();
			Iterator<String> itr = updateKeys.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				updateQuery = updateQuery + props.getProperty(documentType + "." + key) + "=:" + key + ",";
			}

			StringBuilder finalUpdateQuery = new StringBuilder(updateQuery);
			int index = finalUpdateQuery.lastIndexOf(",");
			finalUpdateQuery.replace(index, updateQuery.length() + index, "");
			String finalQry = finalUpdateQuery.toString() + " " + props.getProperty(whereKey);
			System.out.println("Query:  " + finalQry);
			Query updateMetadataQuery = em.createQuery(finalQry);			
			for (Map.Entry<String, String> entry : updateParams.entrySet()) {

				if (entry.getKey().contains("Date")
						&& (docType.equals("corptax") || docType.equals("hfs") || docType.equals("invoices")
								|| docType.equals("manifest") || docType.equals("originations")  
								|| docType.equals("reports.cash") || docType.equals("reports.check"))) {
					String oldDate = updateParams.get(entry.getKey());
					Date date;
					try {
						date = new SimpleDateFormat("yyyy-MM-dd").parse(oldDate);
						updateMetadataQuery.setParameter(entry.getKey(), date, TemporalType.TIMESTAMP);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						throw new Exception();
					}
				} else if (entry.getKey().contains("date_loaded")) {
					String oldDate = entry.getValue();
					Date date;
					try {
						date = new SimpleDateFormat("MM-dd-yyyy").parse(oldDate);
						updateMetadataQuery.setParameter(entry.getKey(), date, TemporalType.TIMESTAMP);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						throw new Exception();
					}

				} else
					updateMetadataQuery.setParameter(entry.getKey(), entry.getValue());
			}

			em.joinTransaction();
			int result = updateMetadataQuery.executeUpdate();
			System.out.println("update successfully: " + result);
			updateQuery = "";
			finalUpdateQuery = null;
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception();
		} finally {
			em.close();
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@SuppressFBWarnings(value = { "REC_CATCH_EXCEPTION"}, justification = "let me just make the build pass")
	public void updateDocumentMetadata(String docType, Map<String, String> documentDetails) throws Exception {
		System.out.println("Entering updateDocumentMetadata() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {			
			CommonDoc commonDoc = new CommonDoc();
			commonDoc.setDocId(documentDetails.get("docId"));
			commonDoc.setDocVersionId(documentDetails.get("docVersionId"));
			commonDoc.setDocName(documentDetails.get("docName"));
			commonDoc.setDocTitle(documentDetails.get("docTitle"));
			commonDoc.setDocType(documentDetails.get("docType"));
			commonDoc.setDocSource(documentDetails.get("docSource"));
			commonDoc.setMimeType(documentDetails.get("mimeType"));
			commonDoc.setIsMigrated(documentDetails.get("isMigrated"));
			commonDoc.setPermName(documentDetails.get("permName"));
			commonDoc.setRealmName(documentDetails.get("realmName"));
			commonDoc.setOwnerName(documentDetails.get("ownerName"));
			
			if(!documentDetails.get("createDate").isEmpty())
			commonDoc.setCreateDate(java.sql.Timestamp.valueOf(documentDetails.get("createDate")));
			if(! documentDetails.get("retentionDate").isEmpty())
			commonDoc.setRetentionDate(java.sql.Timestamp.valueOf(documentDetails.get("retentionDate")));
			commonDoc.setCreator(documentDetails.get("creator"));
			if(!documentDetails.get("modifyDate").isEmpty())
			commonDoc.setModifyDate(java.sql.Timestamp.valueOf(documentDetails.get("modifyDate")));
			commonDoc.setModifier(documentDetails.get("modifier"));
			commonDoc.setIsDeleted(documentDetails.get("isDeleted"));
			commonDoc.setIsCurrent(documentDetails.get("isCurrent"));		
			if (documentDetails.get("versionNum") != null)
				commonDoc.setVersionNum(Long.parseLong(documentDetails.get("versionNum")));
			commonDoc.setDocState(documentDetails.get("docState"));
			commonDoc.setContentRef(documentDetails.get("contentRef"));
			commonDoc.setIsLocked(documentDetails.get("isLocked"));
			commonDoc.setFolderRef(documentDetails.get("folderRef"));
			em.joinTransaction();
			em.persist(commonDoc);

			Session session = em.unwrap(org.hibernate.Session.class);

			if (docType.equals("dealDoc")) {
				DealDoc dealDoc = new DealDoc();
				dealDoc.setDeal_doc_id(documentDetails.get("docId"));
				session.save(dealDoc);
			} else if (docType.equals("corptax")) {
				CorptaxDoc corptaxDoc = new CorptaxDoc();
				corptaxDoc.setCorptaxDocId(documentDetails.get("docId"));
				corptaxDoc.setCtScanDate(java.sql.Timestamp.valueOf(documentDetails.get("createDate")));
				session.save(corptaxDoc);
			} else if (docType.equals("lockbox.cashmedia")) {
				Lockbox lockbox = new Lockbox();
				lockbox.setLockboxDocId(documentDetails.get("docId"));
				session.save(lockbox);
			} else if (docType.equals("reports.cash")) {
				GEReportsDoc reports = new GEReportsDoc();
				reports.setReports_Doc_Id(documentDetails.get("docId"));
				session.save(reports);
			}else if (docType.equals("reports.check")) {
				GEReportsDoc reports = new GEReportsDoc();
				reports.setReports_Doc_Id(documentDetails.get("docId"));
				session.save(reports);
			}

			System.out.println("Document details created successfully...");
		} catch (Exception e) {
			// TODO: handle exception			
			throw new Exception();
		} finally {
			em.close();
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public int updateCustomerName(String partyNumber, String partyName) {
		System.out.println("Entering updateCustomerName() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "deal.update.clause";
			String valueKey = "deal.value.clause";
			String whereKey = "deal.where.clause";

			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey);

			try {
				Query result = em.createQuery(updateQuery);
				result.setParameter("partyName", partyName);
				result.setParameter("partyNumber", partyNumber);

				em.getTransaction().begin();
				int count = result.executeUpdate();
				System.out.println("count......." + count);
				em.getTransaction().commit();
				return count;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			em.close();
		}
		return 0;
	}

	public int integFour(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		
		int a = locUpdate(partyNumber, partyName, creditNumber, opportunityID);
		int b = permUpdate(creditNumber, opportunityID);
		int c = hfsUpdate(partyNumber, partyName, creditNumber, opportunityID);
		int d = origUpdate(partyNumber, partyName, creditNumber, opportunityID);
		int e = accountUpdate(partyNumber, partyName, creditNumber, opportunityID);
		return a + b + c + d + e;
	}
	public int accountUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		int count = 0;
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "account.update.clause";
		String valueKey = "account.value.clause";
		String whereKey = "account.where.clause";
		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);
		System.out.println(updateQuery);
		try {
			String account_Id = getAccountId(opportunityID);
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("lw_entity_type", "Account");
			result.setParameter("account_Id", account_Id);
			/** 
			 * SELECT DISTINCT deal1.account_id FROM DealDoc deal1 WHERE deal1.opportunity_id=:opportunity_id AND lw_entity_type=:lw_entity_type1
			 * */
			
			em.getTransaction().begin();
			count = result.executeUpdate();
			System.out.println("count......." + count);
			em.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	public String getAccountId(String opportunityID) {
		String account_Id = "";
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String selectKey = "account1.select.clause";
		String valueKey = "account1.value.clause";
		String whereKey = "account1.where.clause";
		String selectQuery = props.getProperty(selectKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);
		System.out.println(selectQuery);
		try {
			Query result = em.createQuery(selectQuery);
			result.setParameter("opportunity_id", opportunityID);
			result.setParameter("lw_entity_type", "Opportunity");
			account_Id = (String) result.getSingleResult();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return account_Id;
		
	}
	public int permUpdate(String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		int count = 0;
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "perm.update.clause";
		String valueKey = "perm.value.clause";
		String whereKey = "perm.where.clause";
		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);
		System.out.println(updateQuery);
		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("permName", "Capital_ALF_HEF_FINAL_MANAGER");
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);
			em.getTransaction().begin();
			count = result.executeUpdate();
			System.out.println("count......." + count);
			em.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return 0;
		return count;
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public int locUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		System.out.println("Entering locUpdate() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "deal1.update.clause";
			String valueKey = "deal1.value.clause";
			String whereKey = "deal1.where.clause";
			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey);
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			System.out.println("count......." + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
		return 0;

	}

	@SuppressWarnings("unchecked")
	@Transactional
	public int hfsUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "hfs.update.clause";
		String valueKey = "hfs.value.clause";
		String whereKey = "hfs.where.clause";

		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			System.out.println("count......." + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			System.out.println("Inside Catch Block");
			//e.getMessage();			
			e.printStackTrace();
		}
		return 0;

	}

	@SuppressWarnings("unchecked")
	@Transactional
	public int origUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "origin.update.clause";
		String valueKey = "origin.value.clause";
		String whereKey = "origin.where.clause";

		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			System.out.println("count......." + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	@Transactional
	@SuppressWarnings("unchecked")
	public int loanUpdate(String creditNumber, String sequenceNumber, Date commencementDate, String metadata, String messageType) {
		System.out.println("Entering loanUpdate() in DAO...");
		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "common.update.clause";
			String valueKey = "common.value.clause";
			String whereKey = "common.where.clause";
			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey);
			System.out.println(updateQuery);
			Query result = em.createQuery(updateQuery);
			result.setParameter("retentionDate", commencementDate);
			result.setParameter("docState", "FINAL");
			result.setParameter("permName", "Capital_ALF_HEF_FINAL_MANAGER");
			result.setParameter("creditNumber", creditNumber);
			result.setParameter("sequenceNumber", sequenceNumber);
			System.out.println(result);
			em.getTransaction().begin();
			int count = result.executeUpdate();
			updateCornerstoneMessage(sequenceNumber, metadata,messageType);
			System.out.println("count......." + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
		return 0;
	}
	
	public void updateCornerstoneMessage(String sequenceNumber,String metadata, String messageType) {
		EntityManager em = emf.createEntityManager();
		try {
			System.out.println(metadata);			
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "message.update.clause";
			String valueKey = "message.value.clause";
			String whereKey = "message.where.clause";
			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey)+" "+"like concat('%'," + sequenceNumber + ",'%') AND message_type="+messageType;
			System.out.println(updateQuery);
			Query result = em.createQuery(updateQuery);
			result.setParameter("entity_id", sequenceNumber);
			em.getTransaction().begin();
			result.executeUpdate();
			em.getTransaction().commit();
			//result.setParameter("message_json", metadata);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
	}

}
