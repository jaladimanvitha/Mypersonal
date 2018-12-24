package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.entity.UserDetails;
import com.ge.capital.dms.model.FileMetadata;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.AkanaToken;
import com.ge.capital.dms.utility.CommonConstants;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;

/**
 * MultiFileUploadController is defined as REST Service which is used to upload
 * the content i.e single or multiple documents of any type into Ge-box server.
 * Along with the file content it updates the metadata of those uploaded
 * documents into the database.
 *
 * @author PadmaKiran Vajjala
 * @version 1.0
 * @since 2018-11-11
 */

@SessionAttributes("UserDetails")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class MultiFileUploadController {

	@Value("${upload.path}")
	private String UPLD_DIR;

	@Value("${upload.max.file.count}")
	private String MAX_FILE_CNT;

	@Value("${upload.rootFolderId}")
	private String UPLD_RT_FLDR_ID;

	@Autowired
	private Environment env;

	@Autowired
	UpdateService updateService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	DocumentServiceDAO documentServiceDAO;

	@RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
	public Object handleFileUpload(HttpSession session, @FormDataParam("files") MultipartFile[] files,
			@FormDataParam("docType") String docType, @FormDataParam("fileMetadata") FileMetadata fileMetadata) {

		// boolean respflg = false;
		String boxId;
		String respMsg = "";
		String Jsonstr = null;
		List<String> boxFileExistsList = new ArrayList<String>();
		List<String> failureFilesList = new ArrayList<String>();

		try {
			UserDetails userDetails = (UserDetails) session.getAttribute("UserDetails");
			 //String username = userDetails.getUsername();
			 //System.out.println(username);

			String updateMetadataStrArray[] = fileMetadata.getUploadMetadata().split("},");
			Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);

			if (files.length != 0) {
				AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
				JSONObject boxTokenJSON = dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
				String boxToken = boxTokenJSON.get("accessToken").toString();

				if (!boxToken.isEmpty()) {
					// box connectivity
					BoxAPIConnection api = new BoxAPIConnection(boxToken);
					java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
							new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
					api.setProxy(proxy);

					FileInputStream stream = null;
					BoxFolder rootFolder = new BoxFolder(api, UPLD_RT_FLDR_ID);

					// box folder hierachy
					String folderPath = null;
					folderPath = env.getProperty("upload.folderPath." + docType);
					String path[] = folderPath.split("\\\\");
					String fid = rootFolder.getID();
					for (String p : path) {
						BoxFolder folder = new BoxFolder(api, fid);
						for (BoxItem.Info itemInfo : folder) {
							if (itemInfo instanceof BoxFolder.Info && itemInfo.getName().equals(p)) {
								fid = itemInfo.getID();
								break;
							}
						}
					}

					int indx = 0;
					String actualFolderId = fid;
					for (MultipartFile multipartFile : files) {
						boxId = null;
						File file = null;
						try {
							// Folder Nesting..
							int subFolderNum = 0;
							boolean flag = false;
							boolean subFolderExists = false;
							String subFolderId = null;
							BoxFolder fldr = new BoxFolder(api, actualFolderId);

							for (BoxItem.Info itemInfo : fldr) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										subFolderExists = true;
										subFolderNum = Integer.parseInt(itemInfo.getName());
										BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
										// Do something with the folder.
										BoxFolder folder = new BoxFolder(api, folderInfo.getID());
										int count = Iterators.size(folder.iterator());
										System.out.println("MAX_FILE_CNT: " + MAX_FILE_CNT);
										System.out.println("No of files: " + count);
										if (count >= Integer.parseInt(MAX_FILE_CNT)) {
											flag = false;
											subFolderExists = false;
										} else {
											subFolderId = folder.getID();
											flag = true;
											break;
										}

									}
								}
							}

							if (subFolderExists == false) {
								BoxFolder parentFolder = new BoxFolder(api, fldr.getID());
								subFolderNum += 1;
								BoxFolder.Info childFolderInfo = parentFolder
										.createFolder(Integer.toString(subFolderNum));
								subFolderId = childFolderInfo.getID();
							}

							BoxFolder uploadFolder = new BoxFolder(api, subFolderId);
							System.out.println(
									multipartFile.getOriginalFilename() + ": " + multipartFile.getContentType());
							file = new File(UPLD_DIR + "\\" + multipartFile.getOriginalFilename());

							multipartFile.transferTo(file);
							stream = new FileInputStream(file);
							BoxFile.Info newFileInfo = uploadFolder.uploadFile(stream,
									multipartFile.getOriginalFilename());
							boxId = newFileInfo.getID();
							System.out.println(newFileInfo.getName() + ":uploaded to box successfully...");

							Map<String, String> documentDetails = new HashMap<String, String>();
							// update specific docType metadata in commonDoc
							Map<String, String> metadataMap = new HashMap<String, String>();
							ObjectMapper mapperMetadata = new ObjectMapper();
							metadataMap = mapperMetadata.readValue(updateMetadataStrArray[indx].concat("}"),
									new TypeReference<HashMap<String, String>>() {
									});

							documentDetails.put("docId", newFileInfo.getID());
							documentDetails.put("docVersionId", newFileInfo.getVersion().getVersionID());
							documentDetails.put("docName", newFileInfo.getName());
							documentDetails.put("docTitle", newFileInfo.getName());

							if (docType.equals("lockbox.cashmedia") || docType.equals("lockbox.wireslb")
									|| docType.equals("lockbox.pnc")) {
								documentDetails.put("docType", "lockbox");
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", "");
								documentDetails.put("retentionDate", "");
							} else if (docType.equals("reports.cash") || docType.equals("reports.check")) {
								documentDetails.put("docType", "reports");
								documentDetails.put("isMigrated",
										metadataMap.get(docIdprops.getProperty(docType + ".isMigrated")));
								documentDetails.put("docSource",
										metadataMap.get(docIdprops.getProperty(docType + ".docSource")));
								documentDetails.put("retentionDate", metadataMap
										.get(docIdprops.getProperty(docType + ".retentionDate")).replace('T', ' '));
							} else {
								documentDetails.put("docType", docType);
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", "");
								documentDetails.put("retentionDate", "");
							}

							documentDetails.put("mimeType", multipartFile.getContentType());
							documentDetails.put("permName", "");
							documentDetails.put("realmName", "");
							documentDetails.put("ownerName", newFileInfo.getOwnedBy().getName());
							documentDetails.put("createDate",
									(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getCreatedAt())));

							if (userDetails != null)
								documentDetails.put("creator", userDetails.getUsername());
							else
								documentDetails.put("creator", newFileInfo.getOwnedBy().getName());

							documentDetails.put("modifyDate",
									(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getModifiedAt())));

							if (userDetails != null)
								documentDetails.put("modifier", userDetails.getUsername());
							else
								documentDetails.put("modifier", newFileInfo.getModifiedBy().getName());

							documentDetails.put("isDeleted", "");
							documentDetails.put("isCurrent", "");
							documentDetails.put("versionNum", newFileInfo.getVersionNumber());
							documentDetails.put("docState", newFileInfo.getItemStatus());
							documentDetails.put("contentRef", "");
							documentDetails.put("isLocked", "");
							documentDetails.put("folderRef", "");

							updateService.updateDocumentMetadata(docType, documentDetails);

							// metadata update for Party doc upload
							if (docType.equals(docIdprops.getProperty(docType + ".multi.upload"))) {
								String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
								Map<String, String> updateParams = new HashMap<String, String>();
								ObjectMapper mapper = new ObjectMapper();
								
									// convert JSON string to Map									
									updateParams = mapper.readValue(updateMetadataStrArray[indx].concat("}"),
											new TypeReference<HashMap<String, String>>() {
											});
									updateParams.put("documentId", newFileInfo.getID());
									updateParams.entrySet()
											.removeIf(entry -> "fileName".equalsIgnoreCase(entry.getKey()));

									// setting physicalStorageNotSent
									if (docType.equals("dealDoc")) {
										if (updateParams.get("physicalStorageStatus").equals("1"))
											updateParams.put("physicalStorageNotSent", "1");
										else if (updateParams.get("physicalStorageStatus").equals("0"))
											updateParams.put("physicalStorageNotSent", "0");
									}

									if (docType.equals("reports.cash") || docType.equals("reports.check")) {
										updateParams.put("gecap_reportRunDate",
												updateParams.get("gecap_reportRunDate").replace('T', ' '));
										updateParams.put("gecap_reportDate",
												updateParams.get("gecap_reportDate").replace('T', ' '));
										updateParams.entrySet().removeIf(entry -> docIdprops
												.getProperty(docType + ".isMigrated").equalsIgnoreCase(entry.getKey()));
										updateParams.entrySet().removeIf(entry -> docIdprops
												.getProperty(docType + ".docSource").equalsIgnoreCase(entry.getKey()));
										updateParams.entrySet()
												.removeIf(entry -> docIdprops.getProperty(docType + ".retentionDate")
														.equalsIgnoreCase(entry.getKey()));
									}							

								updateService.updateDocumentTypeMetadata(docType, tableName, updateParams);
								indx += 1;
							}

						} catch (BoxAPIException boxAPIException) {
							if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS)
								boxFileExistsList.add(multipartFile.getOriginalFilename());
							else
								failureFilesList.add(multipartFile.getOriginalFilename());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							failureFilesList.add(multipartFile.getOriginalFilename());
							BoxFile boxFile = new BoxFile(api, boxId);
							boxFile.delete();
							documentServiceDAO.deleteDoc(boxId, docType);
							e.printStackTrace();
						} finally {
							try {
								if (stream != null)
									stream.close();
								if (file != null)
									file.delete();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

				}

			}

		} catch (Exception ex) {
			// TODO: handle exception
			failureFilesList.add(files[0].getOriginalFilename());
			ex.printStackTrace();
		}

		if (failureFilesList.isEmpty() && boxFileExistsList.isEmpty())
			respMsg = "All files are uploaded to box successfully";

		if (!failureFilesList.isEmpty()) {
			respMsg = "Something went wrong could not upload the files: ";
			for (String fileFailed : failureFilesList)
				respMsg += "'" + fileFailed + "', ";
		}

		if (!boxFileExistsList.isEmpty()) {
			respMsg += "Files ";
			for (String bfeL : boxFileExistsList)
				respMsg += "'" + bfeL + "', ";
			respMsg += "have already been uploaded to this location.";
		}

		Gson g = new Gson();
		Jsonstr = g.toJson(respMsg);
		System.out.println(Jsonstr);
		return Jsonstr;

	}
}
