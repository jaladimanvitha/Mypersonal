package com.ge.capital.dms.fr.sle.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.entity.UserDetails;
import com.ge.capital.dms.model.ExportManifestIVO;
import com.ge.capital.dms.model.FileMetadata;
import com.ge.capital.dms.service.DocumentService;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.AkanaToken;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.google.common.collect.Iterators;

import  java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import  org.apache.poi.hssf.usermodel.HSSFSheet;
import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.util.IOUtils;
import org.json.simple.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import  org.apache.poi.hssf.usermodel.HSSFRow;

/**
 * @author GB500257
 */

@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class ExportManifestController {
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
	
	@Autowired
	DocumentService documentService;
	
	@Value("${download.path}")
	private String DIRECTORY;
	
	@SuppressWarnings("deprecation")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/exportManifest")
	@ResponseBody
	public ResponseEntity<Resource> exportToExcel(HttpServletRequest httprequest, @RequestBody ExportManifestIVO request) {
		String loggedinUser=httprequest.getHeader("loggedinuser");
		
			String boxId = "";
			/* UserDetails userDetails = (UserDetails) session.getAttribute("UserDetails");
			System.out.println(session.getAttributeNames());
			System.out.println(session.getAttribute("UserDetails")); */
			String filename = null;
			String sendersName = request.getSenderName();
			String storerNumber = request.getStorerNum();
			String manifestSeqNumber = request.getManifestSeqNum();
			String businessLocation = request.getBusinessLocation();
			String trackingNumber = request.getTrackingNum();
			String sendersBusiness = request.getSenderBusiness();
			String shippingMethod = request.getShippingMethod();
			//String creation_to_dt_searched= null;
			//String creation_frm_dt_searched= null;
			//String mft_modifier_searched= null;
			//String mft_creater_searched= null;
			ArrayList<String> obj1 = (ArrayList<String>) request.getManifestInfo();
			//Map[] map = new HashMap[];
			
			
            HSSFWorkbook workbook = new HSSFWorkbook();  
            HSSFSheet sheet = workbook.createSheet("Custom_Report"); 
            HSSFRow headerRow = sheet.createRow((short)0);
            headerRow.createCell(0).setCellValue("Sender's Name");
            headerRow.createCell(1).setCellValue("Storer Number");
            headerRow.createCell(2).setCellValue("Shipping Method");
            headerRow.createCell(3).setCellValue("Tracking Number");
            headerRow.createCell(4).setCellValue("Sender Business");
            headerRow.createCell(5).setCellValue("Business Location");
            headerRow.createCell(6).setCellValue("Manifest Sequence No");
            HSSFRow row = sheet.createRow((short)1);
            row.createCell(0).setCellValue(sendersName);
            row.createCell(1).setCellValue(storerNumber);
            row.createCell(2).setCellValue(shippingMethod);
            row.createCell(3).setCellValue(trackingNumber);
            row.createCell(4).setCellValue(sendersBusiness);
            row.createCell(5).setCellValue(businessLocation);
            row.createCell(6).setCellValue(manifestSeqNumber);
			//Map<String, Object[]> data = new HashMap<String, Object[]>();
            HSSFRow row2 = sheet.createRow((short)2);
            HSSFRow row3 = sheet.createRow((short)3);
            row3.createCell(0).setCellValue("Takedown ID");
            row3.createCell(1).setCellValue("Created By");
            row3.createCell(2).setCellValue("Customer ID");
            row3.createCell(3).setCellValue("Modified By");
            row3.createCell(4).setCellValue("Document Name");
            row3.createCell(5).setCellValue("Creation Date");
            row3.createCell(6).setCellValue("Retention Date");
            row3.createCell(7).setCellValue("Customer Name");
            int n = 4;
            for(String s: obj1) {
            	HSSFRow rown = sheet.createRow((short)n);
            	
            	String[] tem = s.split(",");
            	for(int i = 0;i< tem.length;i++) {
            		System.out.println(tem[i]);
            		if(tem[i].split(":")[1].replace('"',' ').replace('}',' ').replace('{',' ').trim() != null || tem[i].split(":")[1].replace('"',' ').replace('}',' ').replace('{',' ').trim() != "null") {
            			rown.createCell(i).setCellValue(tem[i].split(":")[1].replace('"',' ').replace('}',' ').replace('{',' ').trim());
            		} else {
            			rown.createCell(i).setCellValue(' ');
            		}
            			
            	}
				n++;
			}
            
			filename = "Manifest_Reports";
			String filename1 = DIRECTORY + "\\" + filename + "_" + manifestSeqNumber + ".xls";
			Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
            AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
			JSONObject boxTokenJSON = dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
			String boxToken = boxTokenJSON.get("accessToken").toString();
			try {
            FileOutputStream fileOut = new FileOutputStream(filename1);
            File f = new File(filename1);
            workbook.write(fileOut);
            System.out.println("Your excel file has been generated!");
            
            
            
			if (!boxToken.isEmpty()) {
				BoxAPIConnection api = new BoxAPIConnection(boxToken);
				java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
						new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
				api.setProxy(proxy);
				BoxFolder rootFolder = new BoxFolder(api, UPLD_RT_FLDR_ID);
				String folderPath = null;
				folderPath = env.getProperty("upload.folderPath.exportManifest");
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
				String actualFolderId = fid;
				BoxFolder fldr = new BoxFolder(api, actualFolderId);
				FileInputStream stream = new FileInputStream(filename1);
				BoxFile.Info newFileInfo = fldr.uploadFile(stream,
						filename1);
				boxId = newFileInfo.getID();
				System.out.println(newFileInfo.getName() + ":uploaded to box successfully...");
				HashMap<String, String> metdata = new HashMap<String, String>();
				metdata.put("manifestSeqNumber", manifestSeqNumber);
				metdata.put("businessLocation", businessLocation);
				metdata.put("storerNumber", storerNumber);
				metdata.put("sendersName", sendersName);
				metdata.put("docID", newFileInfo.getID());
				metdata.put("docName", filename + "_" + manifestSeqNumber + ".xls");
				System.out.println(newFileInfo.getID());
				/*metdata.put("mft_creater_searched", mft_creater_searched);
				metdata.put("mft_modifier_searched", mft_modifier_searched);
				metdata.put("creation_frm_dt_searched", creation_frm_dt_searched);
				metdata.put("creation_to_dt_searched", creation_to_dt_searched); */
				metdata.put("mft_creater_searched", loggedinUser);
				metdata.put("mft_modifier_searched", "");
				metdata.put("creation_frm_dt_searched", "");
				metdata.put("creation_to_dt_searched", "");
				updateService.updateManifestDoc("exportManifest",metdata);
				documentService.updateAuditInfo(newFileInfo.getID(), "ExportManifest", "Manifest", loggedinUser, "SUCCESS",
						"Exported successfully");
			}
			InputStreamResource resource = null;
			resource = new InputStreamResource(new FileInputStream(filename1));
            return ResponseEntity.ok()
    				// Content-Disposition
    				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;").header("fileName", filename+"_"+manifestSeqNumber+".xls")
    				.body(resource);
           // fileOut.close();
            //workbook.close();
            
		} catch(Exception e) {
			//e.printStackTrace();
			BoxAPIConnection api = new BoxAPIConnection(boxToken);
			BoxFile boxFile = new BoxFile(api, boxId);
			documentService.updateAuditInfo(boxId, "ExportManifest", "Manifest", loggedinUser, "FAILED",
					"Export failed");
			boxFile.delete();
			documentServiceDAO.deleteDoc(boxId, "exportManifest");
			return ResponseEntity.badRequest()
    				// Content-Disposition
    				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
    				.body(new InputStreamResource(null, "File Not Found"));
		}
		   
    }
}

