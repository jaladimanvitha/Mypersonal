package com.ge.capital.dms.fr.sle.controllers.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.service.UpdateService;

import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class LWIntegMetadataUpdateController {
	
	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public LWIntegMetadataUpdateController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}
	@Autowired
	UpdateService updateService;

	@SuppressWarnings({ "unchecked", "finally" })
	@RequestMapping(value = "/lwIntegMetadataUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> metadataUpdatePost(@ApiParam(value = "", required = true) @Valid @RequestBody JSONObject metadata) {
		Map<String, Object> statusObj = new HashMap<String, Object>();
		String message = "";
		int count = 0;
		try {
			//System.out.println(metadata);
			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap = (HashMap<String, String>) metadata.clone();
			System.out.println(metadataMap);
			if(metadataMap.get("messageType").equalsIgnoreCase("CustomerUpdate")) {
				
				System.out.println("This is inside CustomerUpdate");
				count = updateService.updateCustomerName(metadataMap.get("partyNumber"),metadataMap.get("partyName"));
			}
			if(metadataMap.get("messageType").equalsIgnoreCase("LOCUpdate")) {
				System.out.println("This is inside LOCUpdate");
				String decodedPartyName = new String(Base64.decodeBase64(metadataMap.get("partyName")), "ISO-8859-1");
				count = updateService.locUpdate(metadataMap.get("partyNumber"),decodedPartyName,metadataMap.get("creditNumber"),metadataMap.get("opportunityID"));
			}
			if((metadataMap.get("messageType").equalsIgnoreCase("LoanUpdate")) || (metadataMap.get("messageType").equalsIgnoreCase("LeaseUpdate"))) {
				System.out.println("This is inside LeaseUpdate");
				if(metadataMap.containsKey("commencementDate")) {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date date = df.parse(metadataMap.get("commencementDate"));
					Date newRetentionDate=getRetentionDate(date,10);
					count = updateService.loanUpdate(metadataMap.get("creditNumber"),metadataMap.get("sequenceNumber"),newRetentionDate,metadata.toJSONString(),metadataMap.get("messageType"));
				} else {
					Date date = new Date();
					Date newRetentionDate=getRetentionDate(date,10);
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					count = updateService.loanUpdate(metadataMap.get("creditNumber"),metadataMap.get("sequenceNumber"),newRetentionDate,metadata.toJSONString(),metadataMap.get("messageType"));
				}
				
			}
			if(count != 0) {
				statusObj.put("code","200");
				statusObj.put("name", "update-success");
				statusObj.put("description", "Metadata updated Successfully");
				message = "Metadata updated Successfully";
			   //return new ResponseEntity(statusObj, HttpStatus.OK);
			} else {
				statusObj.put("code","400");
				statusObj.put("name", "Invalid field");
				statusObj.put("description", "Required Field is null/empty in the request");
				message = "Required Field is null/empty in the request";
				//return new ResponseEntity(statusObj, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			statusObj.put("code","400");
			statusObj.put("name", "Invalid field");
			statusObj.put("description", "Required Field is null/empty in the request");
			message = "Required Field is null/empty in the request";
			//return new ResponseEntity(statusObj, HttpStatus.BAD_REQUEST);
			//e.printStackTrace();
			//throw new CustomGenericException(CommonConstants.INTERNAL_SERVER_ERROR, e.getMessage());
			//return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			Map<String, Object> model = new HashMap<>();
			model.put("message", message);
			model.put("status", statusObj);
			return model;
		}
	}
	private Date getRetentionDate(Date commenceDate, int years){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(commenceDate);
		calendar.add(Calendar.YEAR, years);
		return calendar.getTime();
	}

}