package com.ge.capital.dms.fr.sle.controllers.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ge.capital.dms.model.ExportManifestIVO;
import com.ge.capital.dms.model.FileMetadata;

import  java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import  org.apache.poi.hssf.usermodel.HSSFSheet;
import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.util.IOUtils;
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
	
	@Value("${download.path}")
	private String DIRECTORY;
	
	@SuppressWarnings("deprecation")
	@RequestMapping(value = "/exportManifest")
	@ResponseBody
	public ResponseEntity<HSSFWorkbook> exportToExcel(@RequestBody ExportManifestIVO request) {
		System.out.println(request);
		try {
			String filename = null;
			String sendersName = request.getSenderName();
			String storerNumber = request.getStorerNum();
			String manifestSeqNumber = request.getManifestSeqNum();
			String businessLocation = request.getBusinessLocation();
			String trackingNumber = request.getTrackingNum();
			String sendersBusiness = request.getSenderBusiness();
			String shippingMethod = request.getShippingMethod();
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
            FileOutputStream fileOut = new FileOutputStream(filename1);
            //File f = new File(filename1);
            workbook.write(fileOut);
            System.out.println("Your excel file has been generated!");
            HashMap<String, String> metdata = new HashMap<String, String>();
            return ResponseEntity.ok()
    				// Content-Disposition
    				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;").header("fileName", filename)
    				.body(workbook);
           // fileOut.close();
            //workbook.close();
            
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		   
    }
}
